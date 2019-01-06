import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.ConditionalProbabilityTable;
import aima.core.probability.bayes.FiniteNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookAlgorithm {

    /**
     * Implementing algorithm 9 FE1(N,Q)
     * @return prior marginal Pr(Q)
     */
    public static Factor factorElimination1(BayesianNetwork bn,
                                            RandomVariable q){
        List<Factor> s = new ArrayList<>();
        Set<RandomVariable> v;
        Factor fRoot=null;
        Factor fI,sumOut;
        FiniteNode node;
        for(RandomVariable r : bn.getVariablesInTopologicalOrder()){
            node= (FiniteNode) bn.getNode(r);
            fI=node.getCPT().getFactorFor();
            s.add(fI);
            if(fRoot==null && fI.contains(q))
                fRoot = fI;
        }

        while(s.size()>1){
            if(!s.get(0).equals(fRoot))
                fI = s.remove(0);
            else
                fI = s.remove(1);
            v = new HashSet<>(fI.getArgumentVariables());
            for(Factor f :s)
                v.removeAll(f.getArgumentVariables());

            sumOut = fI.sumOut(v.toArray(new RandomVariable[v.size()]));
            s.set(0, s.get(0).pointwiseProduct(sumOut));
        }
        return project(s.get(0),q);
    }

    /**
     *
     * @param factor factor to sum out on all its variables excepted var
     * @param var variable not to be sum out
     * @return return a factor
     */
    public static Factor project(Factor factor, RandomVariable var){
        Set<RandomVariable> vars = new HashSet<>(factor.getArgumentVariables());
        vars.remove(var);
        return factor.sumOut(vars.toArray(new RandomVariable[vars.size()]));
    }

    private static void setVariables(BayesianNetwork bn,
                                     Set<ConditionalProbabilityTable> s,
                                     Factor fRoot,
                                     RandomVariable q){
        FiniteNode node;

        for(RandomVariable r : bn.getVariablesInTopologicalOrder()){
            node= (FiniteNode) bn.getNode(r);
            s.add(node.getCPT());
        }
    }
}
