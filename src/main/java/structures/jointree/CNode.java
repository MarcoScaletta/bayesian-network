package structures.jointree;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.impl.FullCPTNode;
import javafx.util.Pair;

import java.util.*;

public class CNode extends FullCPTNode implements Comparable<CNode>{


    private String name;
    private Set<CNode> connections = new HashSet<>();
    private Set<Pair<CNode,CNode>> fillInEdge;
    private int fillInEdgeNum;

    public CNode(RandomVariable var, double[] values, Node... parents) {
        super(var, values, parents);
        this.name = var.getName();
    }

    /**
     * Creation of connection of this CNode
     */
    public void createConnection() {
        for (Node n : this.getChildren())
            connections.add((CNode)n);
        for (Node n : this.getParents())
            connections.add((CNode)n);
    }

    /**
     * Sub-moralization of graph to which this node belongs
     */
    public void moralize(){
        Node[] parentArray = getParents().toArray(new Node[0]);

        for (int i = 0; i < parentArray.length-1; i++) {
            for (int j = i+1; j < parentArray.length; j++) {
                ((CNode) parentArray[i]).getConnections().add((CNode)parentArray[j]);
                ((CNode) parentArray[j]).getConnections().add((CNode)parentArray[i]);
            }
        }
    }

    /**
     * Updating of number of edge that would be create with fill-in-edge method of graph
     */
    public void updateFillInEdge(){
        CNode[] connectionArray = connections.toArray(new CNode[0]);

        fillInEdge = new HashSet<>();
        this.fillInEdgeNum=0;
        for (int i = 0; i < connectionArray.length-1; i++)
            for (int j = i+1; j < connectionArray.length; j++)
                if (!connectionArray[i].connections.contains(connectionArray[j])){
                    fillInEdge.add(new Pair<>(connectionArray[i], connectionArray[j]));
                    fillInEdgeNum++;
                }
    }

    /**
     *
     * @return the family of the node
     */
    public Set<RandomVariable> getFamily(){
        Set<RandomVariable> family = new HashSet<>();
        family.add(this.getRandomVariable());
        for (Node node : this.getParents())
            family.add(node.getRandomVariable());
        return family;
    }

    public Set<Pair<CNode, CNode>> getFillInEdge() {
        return fillInEdge;
    }

    public Set<CNode> getConnections() {
        return connections;
    }

    @Override
    public String toString() {return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CNode cNode = (CNode) o;
        return this.getRandomVariable().getName()
                .equals(cNode.getRandomVariable().getName());
    }

    public int compareTo(CNode that) {
        if(this.getConnections().size()  == that.getConnections().size())
            return this.fillInEdgeNum - that.fillInEdgeNum;
        return this.getConnections().size()-that.getConnections().size();
    }
}
