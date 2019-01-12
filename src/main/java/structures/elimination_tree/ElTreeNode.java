package structures.elimination_tree;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;

import java.util.HashSet;
import java.util.Set;

public interface ElTreeNode {


    Factor getFactor();

    void setFactor(Factor factor);

    Set<ElTreeNode> getNeighbours();

    Set<ElTreeNode> detDeleted();

    Set<RandomVariable> getSeparators(ElTreeNode elTreeNode);

}
