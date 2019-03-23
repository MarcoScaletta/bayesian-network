package structures.impl;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import structures.elimination_tree.ElTreeNode;

import java.util.HashSet;
import java.util.Set;

public class Cluster implements ElTreeNode {


    private Set<ElTreeNode> deleted;

    private Set<RandomVariable> vars;

    private Set<ElTreeNode> neighbours;

    private Factor factor;

    private Factor message;

    private ElTreeNode messageTo;


    public Cluster(Set<RandomVariable> allVars){
        this.vars = new HashSet<>(allVars);
        this.neighbours = new HashSet<>();
        this.deleted = new HashSet<>();
    }

    public Set<RandomVariable> getVars() {
        return vars;
    }

    public Set<RandomVariable> getIntersection(Cluster that){
        Set<RandomVariable> tmp = new HashSet<>(this.vars);
        tmp.retainAll(that.vars);
        return tmp;
    }

    public Set<RandomVariable> getSeparators(ElTreeNode that){
        return new HashSet<>(getIntersection((Cluster) that));
    }


    @Override
    public Factor getFactor() {
        return factor;
    }

    @Override
    public Set<ElTreeNode> getNeighbours() {
        return neighbours;
    }

    @Override
    public Set<ElTreeNode> detDeleted() {
        return deleted;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    public Factor getMessage() {
        return message;
    }

    public void setMessage(Factor message) {
        this.message = message;
    }

    public ElTreeNode getMessageTo() {
        return messageTo;
    }

    public void setMessageTo(ElTreeNode messageTo) {
        this.messageTo = messageTo;
    }

    @Override
    public String toString() {
        String nodesString = "[";
        for (RandomVariable var : vars)
            nodesString += var.getName() + " ";
        nodesString = nodesString.substring(0,nodesString.length()-1)+"]";

        return nodesString;
    }
}
