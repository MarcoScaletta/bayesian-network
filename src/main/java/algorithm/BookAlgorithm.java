package algorithm;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.ConditionalProbabilityTable;
import aima.core.probability.bayes.FiniteNode;
import structures.Jointree;
import structures.elimination_tree.ElTreeNode;
import javafx.util.Pair;
import structures.impl.Cluster;

import java.util.*;

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
     * Implementing algorithm 9 FE2(N,Q,(T,Phi),r)
     *
     * @return prior marginal Pr(Q)
     */
    public static Factor factorElimination2(BayesianNetwork bn,
                                            List<ElTreeNode> nodes,
                                            RandomVariable... q) throws Exception {
        List<ElTreeNode> leaves = new ArrayList<>();
        ElTreeNode nI,nJ,root = null;
        Pair<ElTreeNode,ElTreeNode> pairNINJ;
        Set<RandomVariable> v;
        Factor sumOut,phiI,phiJ;

//        try {
        root = rootForVariables(nodes,leaves,q);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        while(nodes.size() > 1){
            pairNINJ = returnNINJ(leaves,root);
            nI = pairNINJ.getKey();
            nJ = pairNINJ.getValue();
            nodes.remove(nI);
            phiI = nI.getFactor();
            v = new HashSet<>(phiI.getArgumentVariables());

            for(ElTreeNode node :nodes){
                if(node.getFactor() !=null)
                    v.removeAll(node.getFactor().getArgumentVariables());
            }
            sumOut = phiI.sumOut(v.toArray(new RandomVariable[v.size()]));

            if(nJ.getFactor() ==null)
                nJ.setFactor(sumOut);
            else
                nJ.setFactor(nJ.getFactor().pointwiseProduct(sumOut));

        }
        return project(root.getFactor(),q);

    }



    /**
     * Implementing algorithm 9 FE2(N,Q,(T,Phi),r)
     *
     * @return prior marginal Pr(Q)
     */

    public static Factor factorElimination3(BayesianNetwork bn,
                                            List<ElTreeNode> nodes,
                                            RandomVariable... q) throws Exception {
        for (ElTreeNode e : nodes)
            for (ElTreeNode n : e.getNeighbours())
                if(!n.getNeighbours().contains(e))
                    throw new Exception("error neighbour missing");


        List<ElTreeNode> leaves = new ArrayList<>();
        Set<ElTreeNode> setNodes = new HashSet<>(nodes);
        ElTreeNode nI,nJ, root = null;
        Pair<ElTreeNode,ElTreeNode> pairNINJ;
        Factor sumOut,phiI,phiJ,project;
        Pair<ElTreeNode,List<ElTreeNode>> rootLeaves;

//        try {
        root = rootForVariables(nodes,leaves,q);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



        while(setNodes.size() > 1){
            setNodes.remove(setNodes.iterator().next());
            pairNINJ = returnNINJ(leaves,root);
            nI = pairNINJ.getKey();
            nJ = pairNINJ.getValue();

            nodes.remove(nI);
            phiI = nI.getFactor();
            phiJ = nJ.getFactor();
            project = project(phiI,nI.getSeparators(nJ).toArray(
                    new RandomVariable[nI.getSeparators(nJ).size()]));

            if(nJ.getFactor() == null)
                nJ.setFactor(project);
            else
                nJ.setFactor(nJ.getFactor().pointwiseProduct(project));
        }
        return project(root.getFactor(),q);
    }

    /**
     *
     * @param g jointree
     * @param q variables to be jointed
     * @return
     * @throws Exception
     */
    @SuppressWarnings("Duplicates")
    public static Factor factorEliminationMex(Jointree g,
                                              RandomVariable... q) throws Exception{
        List<Cluster> clusters = new ArrayList<>(g.getClusters());
        return project(pushCollect(rootForVariables(clusters,q)),q);
    }

    public static void neighboursControl(Collection<ElTreeNode> nodes) throws Exception {
        for (ElTreeNode e : nodes)
            for (ElTreeNode n : e.getNeighbours())
                if(!n.getNeighbours().contains(e))
                    throw new Exception("error neighbour missing");
    }

    private static Factor pushCollect(Cluster root){
        return pushCollectRec(root,null);
    }


    @SuppressWarnings("Duplicates")
    private static Factor pushCollectRec(Cluster from, Cluster to){

        if(to==null || !to.equals(from.getMessageTo())) {
            from.setMessage(from.getFactor());

            from.setMessageTo(to);
            for (ElTreeNode node : from.getNeighbours()){

                System.out.println("OP");
                if (!node.equals(to))
                    from.setMessage(from.getMessage().pointwiseProduct(pushCollectRec((Cluster) node, from)));
            }
            if (to != null)
                from.setMessage(project(from.getMessage(), from.getSeparators(to).toArray(new RandomVariable[0])));
        }

        return from.getMessage();
    }

    private static Pair<ElTreeNode,ElTreeNode> returnNINJ(List<ElTreeNode> leaves, ElTreeNode root) throws Exception {
        ElTreeNode nI,nJ;
        if(leaves.size()==0)
            throw new Exception("There are no more leaves (node with only 1 neighbour)");
        else{
            if(!leaves.get(0).equals(root))
                nI = leaves.remove(0);
            else
                nI = leaves.remove(1);
            nJ = nI.getNeighbours().iterator().next();
            for(ElTreeNode n : nI.getNeighbours()){
                n.getNeighbours().remove(nI);
                if(n.getNeighbours().size() == 1)
                    leaves.add(n);
            }
        }
        return new Pair<>(nI,nJ);
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
        return factor.sumOut(vars.toArray(new RandomVariable[vars.size()]));
    }

    // PRIVATE METHODS

    private static Cluster rootForVariables(List<Cluster> nodes,
                                               RandomVariable... q) throws Exception {
        Cluster root = null;
        for (Cluster node : nodes) {

            if(node.getFactor() != null){
                if(root==null && node.getFactor().getArgumentVariables().containsAll(Arrays.asList(q))){
                    root = node;
                }
                //System.out.println("inner:"+node +"-->" + node.getNeighbours().size());
                if(node.getNeighbours().size() == 0)
                    throw new Exception("Node"+node.getFactor().getArgumentVariables()+" has no neighbour");
                else if(node.getNeighbours().size() == 1){
                }
            }

        }
        if(root == null)
            throw new Exception("There isn't a node that contains"+ Arrays.toString(q));
        return root;
    }

    private static ElTreeNode rootForVariables(List<ElTreeNode> nodes,
                                               List<ElTreeNode> leaves,
                                               RandomVariable... q) throws Exception {
        ElTreeNode root = null;
        for (ElTreeNode node : nodes) {

            if(node.getFactor() != null){
                if(root==null && node.getFactor().getArgumentVariables().containsAll(Arrays.asList(q))){
                    root = node;
                }
                //System.out.println("inner:"+node +"-->" + node.getNeighbours().size());
                if(node.getNeighbours().size() == 0)
                    throw new Exception("Node"+node.getFactor().getArgumentVariables()+" has no neighbour");
                else if(node.getNeighbours().size() == 1){
                    leaves.add(node);

                }
            }

        }
        if(root == null)
            throw new Exception("There isn't a node that contains"+ Arrays.toString(q));
        return root;
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
