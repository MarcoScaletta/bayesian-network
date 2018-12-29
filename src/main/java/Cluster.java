import aima.core.probability.RandomVariable;
import javafx.util.Pair;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Cluster {

    private Set<CNode> mainCNodes;
    private Set<CNode> secondaryCNodes;
    private Set<Cluster> neighbours;


    public Cluster(CNode mainCNodes, Set<CNode> secondaryCNodes){
        this.mainCNodes = new HashSet<>();
        this.secondaryCNodes = new HashSet<>();
        this.mainCNodes.add(mainCNodes);
        this.secondaryCNodes.addAll(secondaryCNodes);
        this.neighbours = new HashSet<>();
    }

    public Set<CNode> getIntersection(Cluster cluster){
        Set<CNode> tmp = new HashSet<>(mainCNodes);
        tmp.addAll(secondaryCNodes);
        tmp.retainAll(cluster.getAllNodes());
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

    public Set<CNode> getMainCNodes() {
        return mainCNodes;
    }

    public Set<CNode> getSecondaryCNodes() {
        return secondaryCNodes;
    }

    public Set<CNode> getAllNodes(){
        Set<CNode> allNodes = new HashSet<>(mainCNodes);
        allNodes.addAll(secondaryCNodes);
        return allNodes;
    }

    @Override
    public String toString() {
        String nodesString = "[";
        String factor = "";
        for (CNode c : mainCNodes) {
            nodesString += c.getRandomVariable().getName() + " ";
            factor+=c.getEvidenceIndicator()+", "+ ((c.getFactor()!=null)?c.getFactor()+", ":"");
        }
        for (CNode c : secondaryCNodes)
            nodesString += c.getRandomVariable().getName() + " ";
        nodesString = nodesString.substring(0,nodesString.length()-1)+"]"/*->("+factor.substring(0,factor.length()-2)+")"*/;

        return nodesString;
    }
}
