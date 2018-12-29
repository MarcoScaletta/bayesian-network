import aima.core.probability.RandomVariable;

import java.util.HashSet;
import java.util.Set;

public class ClusteringNode{

    private String varName;

    private Set<ClusteringNode> parents;
    private Set<ClusteringNode> children;

    public ClusteringNode(RandomVariable var){
        parents=new HashSet<ClusteringNode>();
        children=new HashSet<ClusteringNode>();
    }

    public String getVarName() {
        return varName;
    }

    public Set<ClusteringNode> getParents() {
        return parents;
    }

    public Set<ClusteringNode> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusteringNode that = (ClusteringNode) o;
        return varName.equals(that.varName);
    }

    @Override
    public int hashCode() {
        return varName.hashCode();
    }

    @Override
    public String toString() {
        String val = "";
        val+=varName + ": ";
        for (ClusteringNode c : parents){
            val+=c.varName + " ";
        }
        return val;
    }
}
