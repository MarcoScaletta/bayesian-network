package structures;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.Node;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;
import algorithm.BookAlgorithm;
import javafx.util.Pair;
import structures.elimination_tree.ElTreeNode;
import structures.impl.Cluster;

import java.util.*;

public class Jointree {



    private Set<Cluster> clusters;
    private BayesianNetwork bn;

    private HashMap<RandomVariable,Cluster> varAssignment;

    private void controlBN(BayesianNetwork bn) throws Exception {
        Set<CNode> nodes = new HashSet<>();
        CNode node;
        if(bn.getVariablesInTopologicalOrder().size() < 1)
            throw new Exception ("Bayesian Network empty!");
        else{
            node = (CNode) bn.getNode(bn.getVariablesInTopologicalOrder().get(0));
            nodes.add(node);
            controlBNRec(node, nodes);
            System.err.println("BN SIZE: " + bn.getVariablesInTopologicalOrder().size());
            System.err.println("SUB BN SIZE: " + nodes.size());
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

    private void factorControl() throws Exception {

        for(Cluster c : clusters)
            if(c.getFactor() == null){
                for(RandomVariable r : bn.getVariablesInTopologicalOrder()){
                    System.out.println(r + " D=" + r.getDomain());
                }

                throw new Exception("cluster " + c  + " has null factor");
            }
    }

    public Jointree(BayesianNetwork bn) throws Exception {
        this.bn = bn;
        this.clusters = new HashSet<>();
        this.varAssignment = new HashMap<>();



        CNode [] cNodesArray =  init();

        controlBN(bn);


        fillEdge(cNodesArray);

        createJointree(cNodesArray);
        BookAlgorithm.neighboursControl(new ArrayList<>(clusters));
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

    private Set<RandomVariable> getAllVarsFromClusters(Set<Cluster> clusters){
        Set<RandomVariable> cNodeSet = new HashSet<>();
        for (Cluster c : clusters)
            cNodeSet.addAll(c.getVars());
        return cNodeSet;
    }

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

        System.out.println("cluster not init " + c);


    }

    public void separatorControl(){
        for (Cluster c : clusters){
            for (ElTreeNode n : c.getNeighbours()){
                if(c.getSeparators(n).size() < 1){
                    System.err.println("error neighbour " + c + " " +  n);
                }
            }
        }
    }

    private Set<RandomVariable> getAllVarsFromNodes(Set<CNode> nodes){
        Set<RandomVariable> vars = new HashSet<>();
        for (CNode n : nodes)
            vars.add(n.getRandomVariable());
        return vars;
    }

}
