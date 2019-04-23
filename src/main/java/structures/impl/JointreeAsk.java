package structures.impl;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesInference;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.domain.FiniteDomain;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;
import algorithm.BookAlgorithm;
import javafx.util.Pair;
import structures.Jointree;
import structures.elimination_tree.ElTreeNode;

import java.util.*;

public class JointreeAsk implements BayesInference {

    private AssignmentProposition[] observedEvidence;
    private final Jointree jointree;

    public JointreeAsk(Jointree jointree){
        this(jointree, "VAR_EL");
    }

    public JointreeAsk(Jointree jointree,String task, AssignmentProposition... observedEvidence){
        this.jointree = jointree;
        this.observedEvidence = observedEvidence;

        try {
            switch (task){
                case "VAR_EL":
                    factorElimination(jointree, observedEvidence);
                    break;
                case "MPE":
                    MPE(jointree, observedEvidence);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @Override
    public CategoricalDistribution ask(RandomVariable[] X, AssignmentProposition[] observedEvidence, BayesianNetwork bn) {
        if(!bn.equals(jointree.getBn())){
            try {
                throw new Exception("Error with bayesian network");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(this.observedEvidence.length > 0 && !Arrays.equals(observedEvidence, this.observedEvidence)){
            try {
                throw new Exception("Error: observed evidences inserted are different from previous ones: please recreate");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                return (CategoricalDistribution) this.ask(jointree, X);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Factor ask(Jointree g, RandomVariable... vars) throws Exception {
        Cluster root;
        root = BookAlgorithm.rootForVariables(g.getClusters(),vars);
        System.out.println("root for " + Arrays.toString(vars) + " is " +root);
        return BookAlgorithm.project(root.getMessage(),vars);
    }

    /**
     *
     * @param g jointree
     * @param q variables to be jointed
     * @return result of factor elimination with message reuse in clusters
     * @throws Exception Exception
     */
    public Factor factorEliminationMex(Jointree g,
                                       RandomVariable... q) throws Exception{
        List<Cluster> clusters = new ArrayList<>(g.getClusters());
        Cluster root = BookAlgorithm.rootForVariables(clusters,q);
        pull(root);
        return BookAlgorithm.project(root.getMessage(),q);
    }

    /**
     * Implentation of message passing algorithm
     * @param g jointree - structure containing set of cluster
     * @param e evidences to be inserted
     * @throws Exception Exception
     */
    private void factorElimination(Jointree g,
                                   AssignmentProposition... e)throws Exception{
        Cluster cluster;
        insertEvidences(g, e);
        cluster=g.getClusters().iterator().next();
        pull(cluster);
        push(cluster);
        for(Cluster c : g.getClusters()){
            c.setMessage(((ProbabilityTable) c.getMessage()).normalize());
        }
    }
    /**
     * Implentation of MPE algorithm
     * @param g jointree - structure containing set of cluster
     * @param e evidences to be inserted
     * @throws Exception Exception
     */
    private Pair<Assign, Double> MPE(Jointree g, AssignmentProposition... e)throws Exception{
        Cluster cluster;
        insertEvidences(g, e);
        cluster=g.getClusters().iterator().next();
        return pullMPE(cluster);
    }

    private void insertEvidences(Jointree g,
                                 AssignmentProposition... e) throws Exception {
        Map<RandomVariable,Cluster> evMap = g.getVarAssignment();
        Cluster cluster;

        for (AssignmentProposition a : e){
            if(!evMap.containsKey(a.getTermVariable()))
                throw new Exception("Jointree does not contain variable [" + a + "]");
            cluster = evMap.get(a.getTermVariable());

            insertEvidence(cluster,a);
        }
    }

    private void pull(Cluster root){
        Factor tmp,m_root = root.getFactor();

        for(ElTreeNode e : root.getNeighbours()){

            tmp = pullRec((Cluster)e,root);

            root.getMexFrom().put((Cluster) e,tmp);
            m_root = m_root.pointwiseProduct(tmp);
        }
        root.setMessage(m_root);
    }

    private Factor pullRec(Cluster i, Cluster j){
        Cluster k;
        Factor tmp;
        Factor m_ij =i.getFactor();
        for(ElTreeNode e : i.getNeighbours()) {
            if (!e.equals(j)) {
                k = (Cluster) e;
                tmp = pullRec(k,i);
                i.getMexFrom().put(k,tmp);
                m_ij = m_ij.pointwiseProduct(tmp);
            }
        }
        i.setMessage(m_ij);
        return BookAlgorithm.project(m_ij, i.getSeparators(j).toArray(new RandomVariable[0]));
    }



    private void push(Cluster root){
        pushRec(null,root);
    }

    private void pushRec(Cluster i, Cluster j) {
        Factor m_ij;
        if (i != null){
            m_ij = i.getFactor();
            if(i.getMexFrom().size() < i.getNeighbours().size()-1){
                System.err.println("ERROR MESSAGE PASSING");
            }
            for (Cluster c : i.getMexFrom().keySet()){
                if(!c.equals(j)){
                    m_ij = m_ij.pointwiseProduct(i.getMexFrom().get(c));
                }
            }

            m_ij = BookAlgorithm.project(m_ij,
                    i.getSeparators(j).toArray(new RandomVariable[0]));
            Factor m_j = j.getMessage();
            j.getMexFrom().put(i,m_ij);

            j.setMessage(m_j.pointwiseProduct(m_ij));
        }
        for(ElTreeNode e : j.getNeighbours())
            if(!e.equals(i))
                pushRec(j,(Cluster) e);
    }

    private Pair<Assign, Double> pullMPE(Cluster root) throws Exception {

        List<MessageMPE> messages = new ArrayList<>();
        Factor m_root = root.getFactor();
        Pair<Assign, Double> maxAssignPair;
        MessageMPE messageMPE;

        Assign subset;

        for(ElTreeNode e : root.getNeighbours()) {
            messageMPE = pullMPERec((Cluster)e, root);
            m_root = m_root.pointwiseProduct(messageMPE.getFactor());
            messages.add(messageMPE);
//            System.out.println("Message received " + messageMPE.getFactor());

        }
        maxAssignPair = BookAlgorithm.argmax(m_root);

        Assign maxAssign = new Assign(new HashMap<>(maxAssignPair.getKey().getAssign()));

        for(MessageMPE m : messages){
            subset = maxAssign.getSubset(m.getVars());
            maxAssignPair.getKey().merge(m.getMaxAssignMap().get(subset));
        }

        System.out.println("MPE " + maxAssignPair);

        return maxAssignPair;
    }

    private MessageMPE pullMPERec(Cluster i, Cluster j) throws Exception {

        List<MessageMPE> messages = new ArrayList<>();
        if(i.getFactor() == null)
            System.out.println("i="+i);
        Factor m_ij =i.getFactor();
        MessageMPE messageMPE;

        Assign subset;

        for(ElTreeNode e : i.getNeighbours()) {

            if (!e.equals(j)) {
                messageMPE = pullMPERec((Cluster) e, i);
                System.out.println((m_ij == null )+ " i " + i);
                m_ij = m_ij.pointwiseProduct(messageMPE.getFactor());
                messages.add(messageMPE);
            }
        }


            messageMPE = BookAlgorithm.projectArgmax(m_ij, i.getSeparators(j).toArray(new RandomVariable[0]));


            for(Assign a : messageMPE.getMaxAssignMap().keySet()){
                Assign maxAssign = new Assign(new HashMap<>(a.getAssign()));

                for(MessageMPE m : messages){

                    subset = maxAssign.getSubset(m.getVars());
                    a.merge(m.getMaxAssignMap().get(subset));
                }
            }
        return messageMPE;
    }


    private void insertEvidence(Cluster cluster, AssignmentProposition assign){

        RandomVariable var = assign.getTermVariable();
        ProbabilityTable prod = new ProbabilityTable(var);
        prod.setValue(((FiniteDomain) var.getDomain()).getOffset(assign.getValue()),1);
        cluster.setFactor(cluster.getFactor().pointwiseProduct(prod));
    }


}
