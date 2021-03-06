package ask;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesInference;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.domain.FiniteDomain;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;
import structures.jointree.Cluster;
import structures.jointree.ElTreeNode;
import structures.jointree.Jointree;
import structures.mpe.Assign;
import structures.mpe.MessageMPE;
import util.BookAlgorithm;
import javafx.util.Pair;

import java.util.*;

public class JointreeAsk implements BayesInference {

    private AssignmentProposition[] observedEvidence;
    private final Jointree jointree;

    public JointreeAsk(Jointree jointree,String task, AssignmentProposition... observedEvidence){
        this.jointree = jointree;
        this.observedEvidence = observedEvidence;

        try {
            switch (task){
                case "VAR_EL":
                    factorElimination(jointree, observedEvidence);
                    break;
                case "MPE":
                    System.out.println(MPE(jointree, observedEvidence));
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
     * Implentation of message passing util
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
     * Implementation of MPE util
     * @param g jointree - structure containing set of cluster
     * @param e evidences to be inserted
     * @return max assignment ad its value
     * @throws Exception Exception
     */
    private Pair<Assign, Double> MPE(Jointree g, AssignmentProposition... e)throws Exception{
        Cluster cluster;
        insertEvidences(g, e);
        cluster=g.getClusters().iterator().next();
        return pullMPE(cluster);
    }


    /**
     * Insertion of evidences
     * @param g jointree in which insert the evidences
     * @param e evidences
     * @throws Exception if g doesn't contain a variable which is found in e
     */
    private void insertEvidences(Jointree g,
                                 AssignmentProposition... e) throws Exception {
        Map<RandomVariable,Cluster> evMap = g.getVarAssignment();
        Cluster cluster;

        for (AssignmentProposition a : e){
            if(!evMap.containsKey(a.getTermVariable()))
                throw new Exception("Jointree does not contain variable [" + a.getTermVariable() + "]");
            cluster = evMap.get(a.getTermVariable());

            insertEvidence(cluster,a);
        }
    }

    /**
     * Insert an evidence into a cluster
     * @param cluster in which insert the evidence
     * @param assign evidence to be inserted
     */
    private void insertEvidence(Cluster cluster, AssignmentProposition assign){

        RandomVariable var = assign.getTermVariable();
        ProbabilityTable prod = new ProbabilityTable(var);
        prod.setValue(((FiniteDomain) var.getDomain()).getOffset(assign.getValue()),1);
        cluster.setFactor(cluster.getFactor().pointwiseProduct(prod));
    }

    /**
     * Pull method
     * @param root cluster from which start pull
     */
    private void pull(Cluster root){
        Factor tmp,m_root = root.getFactor();

        for(ElTreeNode e : root.getNeighbours()){

            tmp = pullRec((Cluster)e,root);

            root.getMexFrom().put((Cluster) e,tmp);
            m_root = m_root.pointwiseProduct(tmp);
        }
        root.setMessage(m_root);
    }

    /**
     * Recursive implementation of pull algorithm
     * @param i cluster that send the message
     * @param j cluster that receive the message
     * @return the message
     */
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


    /**
     * Push method
     * @param root
     */
    private void push(Cluster root){
        pushRec(null,root);
    }

    /**
     * Iterative implementation of push algorithm
     * @param i cluster that send the message
     * @param j cluster that receive the message
     */
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

    /**
     * Message passing for calculate MPE
     * @param root cluster from which start
     * @return maximum assignment and its value
     * @throws Exception if called method throws exceptions
     */
    private Pair<Assign, Double> pullMPE(Cluster root) throws Exception {

        Set<RandomVariable> vars = new HashSet<>(jointree.getBn().getVariablesInTopologicalOrder());
        List<MessageMPE> messages = new ArrayList<>();
        Factor m_root = root.getFactor();
        Pair<Assign, Double> maxAssignPair;
        MessageMPE messageMPE;

        Assign subset;

        for(ElTreeNode e : root.getNeighbours()) {
            messageMPE = pullMPERec((Cluster)e, root);
            m_root = m_root.pointwiseProduct(messageMPE.getFactor());
            messages.add(messageMPE);
        }

        maxAssignPair = BookAlgorithm.argmax(m_root);

        Assign maxAssign = new Assign(new HashMap<>(maxAssignPair.getKey().getAssign()));
        for(MessageMPE m : messages){
            subset = maxAssign.getSubset(m.getVars());
            maxAssignPair.getKey().merge(m.getMaxAssignMap().get(subset));
        }

        return maxAssignPair;
    }

    /**
     * Recursive implementation of pullMPE algorithm
     * @param i cluster that send the message
     * @param j cluster that receive the message
     * @return the message
     */
    private MessageMPE pullMPERec(Cluster i, Cluster j) {

        List<MessageMPE> messages = new ArrayList<>();
        Factor m_ij =i.getFactor();
        MessageMPE messageMPE = null;

        for(ElTreeNode e : i.getNeighbours()) {

            if (!e.equals(j)) {
                messageMPE = pullMPERec((Cluster) e, i);
                m_ij = m_ij.pointwiseProduct(messageMPE.getFactor());
                messages.add(messageMPE);
            }
        }

        RandomVariable[] sep;
        try {
            if(!i.hasTrivialFactor() || m_ij.getArgumentVariables().containsAll(i.getSeparators(j))){
                sep = i.getSeparators(j).toArray(new RandomVariable[0]);
                messageMPE = BookAlgorithm.projectArgmax(m_ij, sep);
                updateMex(messageMPE,messages);
            }else{
                Set<RandomVariable> var = new HashSet<>(m_ij.getArgumentVariables());
                var.retainAll(i.getSeparators(j));
                sep = var.toArray(new RandomVariable[0]);
                messageMPE = BookAlgorithm.projectArgmax(m_ij, sep);
                updateMex(messageMPE,messages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageMPE;
    }

    /**
     * Filtering of old assignments
     * @param projectMax message to be update to be sent
     * @param messages list of received message
     */
    private void updateMex(MessageMPE projectMax, List<MessageMPE> messages){

        for(Assign a : projectMax.getMaxAssignMap().keySet())
            for(MessageMPE m : messages)
                for (Assign assign : m.getMaxAssignMap().keySet())
                    if(assign.isSubsetOf(projectMax.getMaxAssignMap().get(a)))
                        projectMax.getMaxAssignMap().get(a).merge(m.getMaxAssignMap().get(assign));
    }



}
