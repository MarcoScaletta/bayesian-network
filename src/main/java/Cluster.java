import aima.core.probability.RandomVariable;

import java.util.HashSet;
import java.util.Set;

public class Cluster {

    private Set<CNode> allNodes;

    private Set<Cluster> neighbours;


    public Cluster(Set<CNode> allNodes){
        this.allNodes = new HashSet<>(allNodes);
        this.neighbours = new HashSet<>();
    }

    public Set<CNode> getIntersection(Cluster that){
        Set<CNode> tmp = new HashSet<>(this.allNodes);
        tmp.retainAll(that.allNodes);
        return tmp;
    }

    public Set<RandomVariable> getSeparators(Cluster that){
        Set<RandomVariable> separators = new HashSet<>();
        for (CNode node : getIntersection(that))
            separators.add(node.getRandomVariable());
        return separators;
    }

    public Set<Cluster> getNeighbours() {
        return neighbours;
    }


    public Set<CNode> getAllNodes(){
        return allNodes;
    }

    @Override
    public String toString() {
        String nodesString = "[";
        for (CNode c : allNodes)
            nodesString += c.getRandomVariable().getName() + " ";
        nodesString = nodesString.substring(0,nodesString.length()-1)+"]";

        return nodesString;
    }
}
