package structures.jointree;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.util.ProbabilityTable;
import javafx.util.Pair;

import java.util.*;

public class Jointree {



    private Set<Cluster> clusters;
    private BayesianNetwork bn;

    private HashMap<RandomVariable,Cluster> varAssignment;

    public Jointree(BayesianNetwork bn) throws Exception {
        this.bn = bn;
        this.clusters = new HashSet<>();
        this.varAssignment = new HashMap<>();



        CNode [] cNodesArray =  init();

        controlBN(bn);


        fillEdge(cNodesArray);

        createJointree(cNodesArray);
        neighboursControl(new ArrayList<>(clusters));
        separatorControl();
        factorControl();

    }

    // PUBLIC METHODS

    public Set<Cluster> getClusters() {
        return clusters;
    }

    public HashMap<RandomVariable, Cluster> getVarAssignment() {
        return varAssignment;
    }

    public BayesianNetwork getBn() {
        return bn;
    }

    // PRIVATE METHODS

    /**
     * Initialization of jointree
     * @return array sorted of node
     */
    private CNode [] init(){
        Set<CNode> cNodes = new HashSet<>();
        CNode c;
        for (RandomVariable r : bn.getVariablesInTopologicalOrder()){
            c = (CNode)  bn.getNode(r);
            cNodes.add(c);
            c.createConnection();
            c.moralize();
        }
        for(CNode cNode : cNodes)
            cNode.updateFillInEdge();

        CNode[] cNodesArray =  cNodes.toArray(new CNode[0]);
        Arrays.sort(cNodesArray);
        return cNodesArray;
    }

    /**
     * fill-in-edge method
     * @param cNodesArray sorted array of node
     */
    private void fillEdge(CNode [] cNodesArray){
        Set<CNode> eliminatedCNode = new HashSet<>();

        for(CNode c : cNodesArray){
            eliminatedCNode.add(c);
            c.updateFillInEdge();
            for(Pair<CNode,CNode> p : c.getFillInEdge())
                if(!eliminatedCNode.contains(p.getKey()) && !eliminatedCNode.contains(p.getValue())){
                    p.getKey().getConnections().add(p.getValue());
                    p.getValue().getConnections().add(p.getKey());
                }
        }
    }

    /**
     * Creation of jointree
     * @param cNodesArray sorted array of node
     * @throws Exception
     */
    private void createJointree(CNode [] cNodesArray) throws Exception {

        boolean alreadyInCluster;

        List<Cluster> clusters = new LinkedList<>();

        Set<CNode> deletedNode = new HashSet<>();
        Set<RandomVariable> allVarForCluster = new HashSet<>();

        for (int i = 0; i < cNodesArray.length; i++) {
            alreadyInCluster = false;
            deletedNode.add(cNodesArray[i]);
            allVarForCluster.add(cNodesArray[i].getRandomVariable());

            for (CNode neighbour : cNodesArray[i].getConnections())
                if(!deletedNode.contains(neighbour))
                    allVarForCluster.add(neighbour.getRandomVariable());

            Iterator cIt = clusters.iterator();
            while(cIt.hasNext() && !alreadyInCluster){
                Cluster c = (Cluster) cIt.next();
                alreadyInCluster = c.getVars().containsAll(allVarForCluster);
            }

            if(!alreadyInCluster)
                clusters.add(new Cluster(allVarForCluster));

            allVarForCluster.clear();
        }
        connectClusters(clusters);
        this.clusters.addAll(clusters);
        assignCPTs();

    }

    /**
     * Connection of clusters
     * @param clusters clusters to be connected
     */
    private void connectClusters(List<Cluster> clusters){
        Set<Cluster> connected = new HashSet<>();
        Cluster tmp;
        List<Cluster> reversedList = new LinkedList<>(clusters);
        Collections.reverse(reversedList);
        boolean inserted;
        if(reversedList.size() > 0){
            tmp = reversedList.iterator().next();
            connected.add(tmp);
        }
        for(Cluster cI : reversedList){
            inserted = false;
            if(!connected.contains(cI)){
                Iterator<Cluster> it = connected.iterator();
                while(it.hasNext() && !inserted){
                    Cluster cJ=it.next();
                    if(!cJ.equals(cI)){
                        Set<RandomVariable> allVarFromCluster = getAllVarsFromClusters(connected);
                        allVarFromCluster.retainAll(cI.getVars());
                        if(cJ.getVars().containsAll(allVarFromCluster)) {

                            cI.getNeighbours().add(cJ);
                            cJ.getNeighbours().add(cI);
                            inserted=true;
                            connected.add(cI);
                        }
                    }
                }
            }
        }
    }

    /**
     * Assignment of CPTs
     * @throws Exception exception
     */
    private void assignCPTs() throws Exception {
        boolean foundCluster;

        Set<Cluster> c = new HashSet<>(clusters);

        Iterator<Cluster> clusterIterator;
        CNode node;
        Cluster cluster;

        for(RandomVariable var : bn.getVariablesInTopologicalOrder()){

            foundCluster = false;
            node = (CNode) bn.getNode(var);
            clusterIterator = clusters.iterator();
            while(clusterIterator.hasNext() && !foundCluster){
                cluster = clusterIterator.next();
                if(cluster.getVars().containsAll(node.getFamily())){
                    c.remove(cluster);
                    foundCluster = true;
                    varAssignment.put(node.getRandomVariable(),cluster);
                    if(node.getCPT().getFactorFor() == null)
                        throw new Exception("node.getCPT().getFactorFor()");
                    cluster.setFactor(
                            cluster.getFactor() == null ?
                                    node.getCPT().getFactorFor() :
                                    cluster.getFactor().pointwiseProduct(node.getCPT().getFactorFor()));
                }
            }
        }
        for(Cluster notInit : c){

            Factor f = new ProbabilityTable(new double[]{1});
            notInit.setFactor(f);
        }
    }

    // CONTROL METHODS

    private void separatorControl(){
        for (Cluster c : clusters){
            for (ElTreeNode n : c.getNeighbours()){
                if(c.getSeparators(n).size() < 1){
                    System.err.println("error neighbour " + c + " " +  n);
                }
            }
        }
    }

    /**
     * Control if some neighbour is missing
     * @param nodes nodes
     * @throws Exception exception
     */
    private static void neighboursControl(Collection<ElTreeNode> nodes) throws Exception {
        for (ElTreeNode e : nodes)
            for (ElTreeNode n : e.getNeighbours())
                if(!n.getNeighbours().contains(e))
                    throw new Exception("error neighbour missing");
    }

    /**
     *
     * @param bn to be controlled
     * @throws Exception exception
     */
    private void controlBN(BayesianNetwork bn) throws Exception {
        Set<CNode> nodes = new HashSet<>();
        CNode node;
        if(bn.getVariablesInTopologicalOrder().size() < 1)
            throw new Exception ("Bayesian Network empty!");
        else{
            node = (CNode) bn.getNode(bn.getVariablesInTopologicalOrder().get(0));
            nodes.add(node);
            controlBNRec(node, nodes);
            if(bn.getVariablesInTopologicalOrder().size() != nodes.size())
                throw new Exception("There are at least 2 sub network disjoint, " +
                        "one consists of nodes" + nodes);
        }
    }

    private void controlBNRec (CNode node, Set<CNode> nodes){
        for(CNode c : node.getConnections()){
            if(!nodes.contains(c)){
                nodes.add(c);
                controlBNRec(c,nodes);
            }
        }
    }

    /**
     * Control if some cluster has null factor
     * @throws Exception if some cluster has null factor
     */
    private void factorControl() throws Exception {

        for(Cluster c : clusters)
            if(c.getFactor() == null)
                throw new Exception("cluster " + c  + " has null factor");
    }

    private Set<RandomVariable> getAllVarsFromClusters(Set<Cluster> clusters){
        Set<RandomVariable> cNodeSet = new HashSet<>();
        for (Cluster c : clusters)
            cNodeSet.addAll(c.getVars());
        return cNodeSet;
    }

    @Override
    public String toString() {
        return clusters.toString();
    }
}
