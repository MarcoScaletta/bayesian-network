package structures.jointree;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;

import java.util.Set;

public interface ElTreeNode {


    Factor getFactor();

    void setFactor(Factor factor);

    Set<ElTreeNode> getNeighbours();

    /**
     *
     * @param elTreeNode node of jointree
     * @return separator of this and elTreeNode
     */
    Set<RandomVariable> getSeparators(ElTreeNode elTreeNode);

}
