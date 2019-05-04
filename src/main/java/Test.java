import BayesianNetworkFactory.BayesianNetworkFactory;
import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.exact.EliminationAsk;
import aima.core.probability.domain.FiniteDomain;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;
import ask.JointreeAsk;
import structures.jointree.Cluster;
import structures.jointree.Jointree;

import java.util.Arrays;
import java.util.Scanner;

public class Test {

    private static double errorLimit = Math.pow(10,-15);

    private static String [] bns = new String[] {"cancer", "earthquake", "survey","asia","child"};

    /**
     * Print jointree construction time
     * @throws Exception exception
     */
    public static void jointreeTimeConstruction() throws Exception {
        init();
        for(String s : bns){
            testConstruction(s);
        }

    }

    /**
     * Print jointree width
     * @throws Exception
     */
    public static void testWidth() throws Exception {
        for(String s : bns)
            testWidth(s);
    }

    /**
     * Single testWidth
     * @param s BayesianNetwork name
     * @throws Exception exception
     */
    public static void testWidth(String s) throws Exception {
        int clusterSize, familySize, maxClusterSize=0, maxFamilySize = 0;

        BayesianNetwork bn;

        bn = BayesianNetworkFactory.createJTNetwork(s +".bif");
        for (RandomVariable r : bn.getVariablesInTopologicalOrder()){
            familySize = bn.getNode(r).getParents().size() + 1;
            if(familySize > maxFamilySize){
                maxFamilySize = familySize;
            }
        }
        System.out.println("Nella rete " + s + " la dimensione della famiglia piu' grande e': " + maxFamilySize);
        Jointree j = new Jointree(bn);
        for (Cluster c : j.getClusters()){
            clusterSize = c.getVars().size();
            if(clusterSize > maxClusterSize)
                maxClusterSize = clusterSize;
        }

        System.out.println("Il cluster piu' grande della rete " + s + " ha dimensione: " + maxClusterSize);


    }


    /**
     * Single testConstruction
     * @param s BayesianNetwork name
     * @throws Exception exception
     */
    public static void testConstruction(String s) throws Exception{
        BayesianNetwork bn;
        bn = BayesianNetworkFactory.createJTNetwork(s +".bif");
        System.out.println("\nCostruzione jointree della rete [" + s + "] con "
                + bn.getVariablesInTopologicalOrder().size() + " nodi");
        double startTime = System.nanoTime();
        new Jointree(bn);
        double finalTime = (System.nanoTime() - startTime)/1000000000;
        System.out.println("Tempo di costruzione jointree " + finalTime + " secondi");

    }

    /**
     * Test result and print time of computations
     * @throws Exception exception
     */
    public static void testQuery() throws Exception {
        BayesianNetwork bn;
        for(String s : bns){
            bn = BayesianNetworkFactory.createJTNetwork(s+".bif");
            System.out.println("\nQUERY su rete [" + s + "] con "
                    + bn.getVariablesInTopologicalOrder().size() + " nodi");
            testQuery(bn);
            System.out.println("\nFINE QUERY su rete [" + s + "] con "
                    + bn.getVariablesInTopologicalOrder().size() + " nodi");
        }
    }

    /**
     * Single testQuery
     * @param bn BayesianNetwork from which create jointree
     * @throws Exception exception
     */
    public static void testQuery(BayesianNetwork bn) throws Exception {
        RandomVariable v1 = bn.getVariablesInTopologicalOrder().get(1);
        AssignmentProposition[] a1 =
                new AssignmentProposition[] {
                        new AssignmentProposition(v1,((FiniteDomain)v1.getDomain()).getValueAt(0))};
        Factor jAlgo,ask;
        boolean found;
        int error = 0;
        int tot=0;
        double mexPassTime;

        Jointree j = new Jointree(bn);
        double startTime = System.nanoTime();
        JointreeAsk jAsk = new JointreeAsk(j,"VAR_EL",a1);
        mexPassTime = (System.nanoTime() - startTime)/1000000000;
        System.out.println(j);
        double totTime = 0.0;
        double tmp;
        for (RandomVariable r : bn.getVariablesInTopologicalOrder()){
            if(!r.equals(v1)) {
                System.out.println("\nSTART:");
                System.out.println("Assign: " + Arrays.toString(a1));
                System.out.println("Query var: " + r);
                tmp =System.nanoTime();
                jAlgo =
                        (ProbabilityTable)  jAsk.ask(new RandomVariable[]{r}, a1, bn);
                totTime += System.nanoTime() - tmp;
                ask = (ProbabilityTable) new EliminationAsk().ask(new RandomVariable[]{r}, a1, bn);
                System.out.println("MioVal: " + jAlgo);
                System.out.println("Atteso: " + ask);
                System.out.println("Calculating error on " + r);

                found= false;
                for (int i = 0; i < jAlgo.getValues().length && !found; i++) {

                    if(Math.abs(jAlgo.getValues()[i]-ask.getValues()[i]) > errorLimit){
                        found = true;
                        error++;
                    }
                }
                tot++;

                if(error == 0)
                    System.out.println("No error > " + errorLimit +"!");
                else
                    System.err.println("Error!");
            }
        }
        System.out.println("Tempo message-passing " + mexPassTime + " secondi");

        System.out.println("Tempo totale " + totTime/1000000000 + " secondi");

        System.out.println("Total errors: " + error +" of "+tot);
        System.out.println("FINISH");
    }

    /**
     * Print MPE results
     * @throws Exception exception
     */
    public static void testMPE() throws Exception {
        BayesianNetwork bn;
        for(String s : bns){
            bn = BayesianNetworkFactory.createJTNetwork(s+".bif");

            System.out.println("\nMPE su rete [" + s + "] con "
                    + bn.getVariablesInTopologicalOrder().size() + " nodi");
            testMPE(bn);
        }
    }

    /**
     * Single testMPE
     * @param bn BayesianNetwork from which create jointree
     * @throws Exception exception
     */
    public static void testMPE(BayesianNetwork bn) throws Exception {
        RandomVariable v1 = bn.getVariablesInTopologicalOrder().get(1);
        AssignmentProposition[] a1 =
                new AssignmentProposition[] {
                        new AssignmentProposition(v1,((FiniteDomain)v1.getDomain()).getValueAt(0))};
        System.out.println("Assign: " + Arrays.toString(a1));
        double startTime;
        Jointree j = new Jointree(bn);

        startTime = System.nanoTime();
        JointreeAsk jAsk = new JointreeAsk(j,"MPE",a1);
        System.out.println("Tempo message passing " + (System.nanoTime() - startTime)/1000000000 + " secondi");

    }


    private static void init() throws Exception {
        Jointree j = new Jointree(BayesianNetworkFactory.createJTNetwork("cancer.bif"));
        JointreeAsk jAsk = new JointreeAsk(j,"VAR_EL");
        jAsk.ask(
                new RandomVariable[]{j.getBn().getVariablesInTopologicalOrder().get(0)},
                new AssignmentProposition[0], j.getBn());
        jAsk = new JointreeAsk(j,"MPE");
    }
}
