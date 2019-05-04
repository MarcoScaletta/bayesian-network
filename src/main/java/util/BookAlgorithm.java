package util;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.util.ProbabilityTable;
import javafx.util.Pair;
import structures.jointree.Cluster;
import structures.mpe.Assign;
import structures.mpe.MessageMPE;

import java.util.*;

public class BookAlgorithm {

    /**
     * Print a factor
     * @param f factor to be printed
     */
    public static void printFactor(Factor f){
        f.iterateOver((v,p)->System.out.println("P("+v+")="+p));
    }

    /**
     *
     * @param factor factor to be projected
     * @param vars variable on which perform projection
     * @return MessageMPE that contain new assignment and filtered old assignments
     * @throws Exception if there are problem creating object MessageMPE
     */
    public static MessageMPE
    projectArgmax(Factor factor, RandomVariable... vars) throws Exception {
        ProbabilityTable p = new ProbabilityTable(vars);
        final Map<Assign, Assign> map = new HashMap<>();

        factor.iterateOver((map1, v) -> p.iterateOverTable((map2, v1) -> {
            boolean b = true;
            double val;
            int index;
            Assign tmp1,tmp2;
            Object [] values;

                for (RandomVariable r : map2.keySet()) {

                    b = b && map1.get(r).equals(map2.get(r));
                }
            if(b){

                values = map2.values().toArray();
                index = p.getIndex(values);

                val = p.getValue(values);
                if(v > val){
                    tmp1 =new Assign(new HashMap<>(map1));
                    tmp2 = new Assign(new HashMap<>(map2));
                    p.setValue(index, v);
                    map.put(tmp2,tmp1);
                }
            }

        }));

        return new MessageMPE(p,map);
    }

    /**
     *
     * @param factor to argmax
     * @return assignment that maximizes probability of factor, and its value
     */
    public static Pair<Assign,Double> argmax(Factor factor){
        Map<String, Assign> map = new HashMap<>();
        Map<String,Double> maxVal = new HashMap<>();
        maxVal.put("MAX",0.0);

        factor.iterateOver((map1, v) -> {
            if(v > maxVal.get("MAX")){
                maxVal.put("MAX",v);
                map.put("MAX_ASSIGN",new Assign(new HashMap<>(map1)));

            }});
        return new Pair<>(map.get("MAX_ASSIGN"),maxVal.get("MAX"));
    }

    /**
     *
     * @param factor factor to sum out on all its variables excepted var
     * @param variables variables not to be summed out
     * @return return a factor
     */
    public static Factor project(Factor factor, RandomVariable... variables){
        Set<RandomVariable> vars = new HashSet<>(factor.getArgumentVariables());
        for(RandomVariable r : variables)
            vars.remove(r);
        return factor.sumOut(vars.toArray(new RandomVariable[0]));
    }

    /**
     *
     * @param nodes collection of cluster where perform searching
     * @param q array of variables
     * @return cluster that contains all variables in q
     * @throws Exception if there's not a cluster that contains q
     */
    public static Cluster rootForVariables(Collection<Cluster> nodes,
                                           RandomVariable... q) throws Exception {
        Cluster root = null;
        for (Cluster node : nodes) {

            if(node.getFactor() != null){
                if(root==null && node.getFactor().getArgumentVariables().containsAll(Arrays.asList(q))){
                    root = node;
                }
                if(node.getNeighbours().size() == 0)
                    throw new Exception("Node"+node.getFactor().getArgumentVariables()+" has no neighbour");
            }

        }
        if(root == null)
            throw new Exception("There isn't a node that contains"+ Arrays.toString(q));
        return root;
    }

}
