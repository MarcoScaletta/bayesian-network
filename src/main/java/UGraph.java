import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import javafx.util.Pair;

import java.util.*;

public class UGraph {

    private BayesianNetwork bn;
    private Set<CNode> cNodes;

    private List<Cluster> clusters;
    private Set<String> clusteredVar;

    public UGraph(BayesianNetwork bn){
        this.bn=bn;
        this.cNodes = new HashSet<>();
        this.clusters = new LinkedList<>();
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
        findClusters(cNodesArray);

        System.out.println("Cluster:");
        for (Cluster c : clusters)
            System.out.println("-> "+c);
        connectClusters();

    }


    private void fillEdge(CNode [] cNodesArray){
        Set<CNode> eliminatedCNode = new HashSet<>();
        Arrays.sort(cNodesArray);

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


    private void findClusters(CNode [] cNodesArray){

        CNode mainCNode;
        boolean alreadyInCluster;
        boolean familyInCluster;


        Set<CNode> deletedNode = new HashSet<>();
        Set<CNode> tmpNeighbourSet = new HashSet<>();


        for (int i = 0; i < cNodesArray.length; i++) {
            alreadyInCluster = false;
            deletedNode.add(cNodesArray[i]);

            mainCNode = cNodesArray[i];
            Set<CNode> tmp = new HashSet<>();
            for (CNode c : mainCNode.getConnections())
                if(!deletedNode.contains(c))
                    tmp.add(c);

            //tmpNeighbourSet.addAll((Set<CNode>) (Set<?>) mainCNode.getParents());
            familyInCluster = false;

            Iterator cIt1 = clusters.iterator();
            while(cIt1.hasNext() && !familyInCluster){
                if(((Cluster)cIt1.next()).getAllNodes().containsAll(mainCNode.getFamily()))
                    familyInCluster=true;
            }
            if(!familyInCluster)
                tmpNeighbourSet.addAll((Set<CNode>) (Set<?>) mainCNode.getParents());


            for (CNode neighbour : cNodesArray[i].getConnections()){
                if(!deletedNode.contains(neighbour)){
                    tmpNeighbourSet.add(neighbour);
                }
            }

            Iterator cIt = clusters.iterator();
            while(cIt.hasNext() && !alreadyInCluster){
                Cluster c = (Cluster) cIt.next();

                Set<CNode> childAndParents = new HashSet<CNode>();
                Set<CNode> allNodeOfCluster = new HashSet<CNode>();

                allNodeOfCluster.addAll(c.getSecondaryCNodes());
                allNodeOfCluster.addAll(c.getMainCNodes());
                childAndParents.add(mainCNode);
                childAndParents.addAll(tmpNeighbourSet);

                alreadyInCluster = allNodeOfCluster.containsAll(childAndParents);

                if(alreadyInCluster){
                    c.getSecondaryCNodes().remove(mainCNode);
                    c.getMainCNodes().add(mainCNode);
                }

            }

            if(!alreadyInCluster)
                clusters.add(new Cluster(mainCNode,tmpNeighbourSet));

            tmpNeighbourSet.clear();
        }

    }

    private void connectClusters(){
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
                System.out.println("Cluster:"+cI);
                Iterator<Cluster> it = connected.iterator();
                while(it.hasNext() && !inserted){
                    Cluster cJ=it.next();
                    if(!cJ.equals(cI)){
                        Set<CNode> allCNodesFromClusters = getAllCNodesFromClusters(connected);
                        allCNodesFromClusters.retainAll(cI.getAllNodes());
                        if(cJ.getAllNodes().containsAll(allCNodesFromClusters)) {

                            System.out.println("--------CJ :"+cJ);
                            System.out.println("--------CI :"+cI);
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

    public List<Cluster> getClusters() {
        return clusters;
    }

    public Set<CNode> getcNodes() {
        return cNodes;
    }
}
