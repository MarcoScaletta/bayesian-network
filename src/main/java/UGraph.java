import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import javafx.util.Pair;

import java.util.*;

public class UGraph {

    private BayesianNetwork bn;
    private Set<CNode> cNodes;

    private Set<String> clusteredVar;
    private Jointree jointree;

    public UGraph(BayesianNetwork bn){
        this.bn=bn;
        this.cNodes = new HashSet<>();

        this.clusteredVar = new HashSet<>();
        for (RandomVariable r : bn.getVariablesInTopologicalOrder()){
            CNode c = (CNode)  bn.getNode(r);
            cNodes.add(c);
        }

        for (CNode cNode:cNodes) {
            cNode.createConnection();
            cNode.moralize();
        }

        for(CNode cNode : cNodes)
            cNode.updateFillInEdge();
        CNode [] cNodesArray =  cNodes.toArray(new CNode[cNodes.size()]);
        Arrays.sort(cNodesArray);
        System.out.println("Elimination order:"+Arrays.toString(cNodesArray));
        fillEdge(cNodesArray);
        createJointree(cNodesArray);
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


    private void createJointree(CNode [] cNodesArray){

        boolean alreadyInCluster;

        List<Cluster> clusters = new LinkedList<>();

        Set<CNode> deletedNode = new HashSet<>();
        Set<CNode> allNodesForCluster = new HashSet<>();

        for (int i = 0; i < cNodesArray.length; i++) {
            alreadyInCluster = false;
            deletedNode.add(cNodesArray[i]);
            allNodesForCluster.add(cNodesArray[i]);

            for (CNode neighbour : cNodesArray[i].getConnections())
                if(!deletedNode.contains(neighbour))
                    allNodesForCluster.add(neighbour);

            Iterator cIt = clusters.iterator();
            while(cIt.hasNext() && !alreadyInCluster){
                Cluster c = (Cluster) cIt.next();
                alreadyInCluster = c.getAllNodes().containsAll(allNodesForCluster);
            }

            if(!alreadyInCluster)
                clusters.add(new Cluster(allNodesForCluster));

            allNodesForCluster.clear();
        }
        connectClusters(clusters);
        jointree = new Jointree(clusters);

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
            System.out.println("Cluster:"+cI);
            inserted = false;
            if(!connected.contains(cI)){
                Iterator<Cluster> it = connected.iterator();
                while(it.hasNext() && !inserted){
                    Cluster cJ=it.next();
                    if(!cJ.equals(cI)){
                        Set<CNode> allCNodesFromClusters = getAllCNodesFromClusters(connected);
                        allCNodesFromClusters.retainAll(cI.getAllNodes());
                        if(cJ.getAllNodes().containsAll(allCNodesFromClusters)) {

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

    private Set<CNode> getAllCNodesFromClusters(Set<Cluster> clusters){
        Set<CNode> cNodeSet = new HashSet<>();
        for (Cluster c : clusters)
            cNodeSet.addAll(c.getAllNodes());
        return cNodeSet;
    }

    public Jointree getJointree() {
        return jointree;
    }

    public Set<CNode> getcNodes() {
        return cNodes;
    }
}
