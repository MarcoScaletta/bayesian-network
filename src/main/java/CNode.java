import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.impl.FullCPTNode;
import javafx.util.Pair;

import java.util.*;

public class CNode extends FullCPTNode implements Comparable<CNode>{

    private String evidenceIndicator;
    private String factor;
    private Set<CNode> connections = new HashSet<CNode>();
    private Set<Pair<CNode,CNode>> fillInEdge;

    public CNode(RandomVariable var, double[] distribution) {
        this(var, distribution, (Node[])null);

    }

    public CNode(RandomVariable var, double[] values, Node... parents) {
        super(var, values, parents);
        this.evidenceIndicator = "e_"+var.getName();
        factor="";
        if(parents!=null && parents.length>0){
            factor = "f_"+var.getName()+"|";
            for (Node n : parents)
                factor+=n.getRandomVariable().getName();
        }


    }


    public void createConnection() {
        for (Node n : this.getChildren())
            connections.add((CNode)n);
        for (Node n : this.getParents())
            connections.add((CNode)n);

    }


    public void moralize(){
        Node[] parentArray = getParents().toArray(new Node[getParents().size()]);

        for (int i = 0; i < parentArray.length-1; i++) {
            for (int j = i+1; j < parentArray.length; j++) {
                ((CNode) parentArray[i]).getConnections().add((CNode)parentArray[j]);
                ((CNode) parentArray[j]).getConnections().add((CNode)parentArray[i]);
            }
        }
    }

    public void updateFillInEdge(){
        CNode[] connectionArray = connections.toArray(new CNode[connections.size()]);

        fillInEdge = new HashSet<>();
        for (int i = 0; i < connectionArray.length-1; i++)
            for (int j = i+1; j < connectionArray.length; j++)
                if (!connectionArray[i].connections.contains(connectionArray[j]))
                    fillInEdge.add(new Pair<>(connectionArray[i], connectionArray[j]));
    }

    public Set<Pair<CNode, CNode>> getFillInEdge() {
        return fillInEdge;
    }

    public Set<CNode> getConnections() {
        return connections;
    }

    public String getEvidenceIndicator() {
        return evidenceIndicator;
    }

    public String getFactor() {
        return factor;
    }

    public Set<CNode> getFamily(){
        Set<CNode> family = new HashSet<>();
        family.add(this);
        family.addAll((Set<CNode>)(Set<?>)this.getParents());
        return family;
    }

    @Override
    public String toString() {
        String val = "";
        return ""+this.getRandomVariable().getName()+/*":"+ Arrays.toString(this.fillInEdge) +*/""; //da eliminare
        /*val += this.getRandomVariable().getName() +": ";
        for (CNode c : connections)
            val+=c.getRandomVariable().getName()+" ";
        return val.substring(0,val.length()-1);*/
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
            return this.getFillInEdge().size() - that.getFillInEdge().size();
        return this.getConnections().size()-that.getConnections().size();
    }
}
