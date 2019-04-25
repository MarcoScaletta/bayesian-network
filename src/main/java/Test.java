import BayesianNetworkFactory.BayesianNetworkFactory;
import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.exact.EliminationAsk;
import aima.core.probability.domain.FiniteDomain;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;
import structures.ask.JointreeAsk;
import structures.impl.Jointree;

import java.util.Arrays;
import java.util.Scanner;

public class Test {

    public static void testQuery() throws Exception {
        for(BayesianNetwork bn : BayesianNetworkFactory.bayesianNetworks){

            RandomVariable v1 = bn.getVariablesInTopologicalOrder().get(1);
            AssignmentProposition[] a1 =
                    new AssignmentProposition[] {
                            new AssignmentProposition(v1,((FiniteDomain)v1.getDomain()).getValueAt(0))};
            Factor jAlgo,ask;
            boolean found;
            int error = 0;
            int tot=0;
            Jointree j = new Jointree(bn);
            JointreeAsk jAsk = new JointreeAsk(new Jointree(bn),"VAR_EL",a1);
            System.out.println(j);
            for (RandomVariable r : bn.getVariablesInTopologicalOrder()){
            if(!r.equals(v1)) {
                System.out.println("\nSTART:");
                System.out.println("Assign: " + Arrays.toString(a1));
                System.out.println("Query var: " + r);
                jAlgo =
                        (ProbabilityTable)  jAsk.ask(new RandomVariable[]{r}, a1, bn);
                ask = (ProbabilityTable) new EliminationAsk().ask(new RandomVariable[]{r}, a1, bn);
                System.out.println("MioVal: " + jAlgo);
                System.out.println("Atteso: " + ask);
                System.out.println("Calculating error on " + r);
                found= false;
                for (int i = 0; i < jAlgo.getValues().length && !found; i++) {
                    if(Math.abs(jAlgo.getValues()[i]-ask.getValues()[i]) > 0.00000000001){
                        found = true;
                        error++;
                    }
                }
                tot++;

                if(error == 0)
                    System.out.println("No error!");
                else
                    System.err.println("Error!");
            }
        }
        System.out.println("Total errors: " + error +" of "+tot);
        System.out.println("FINISH");
            (new Scanner(System.in)).nextLine();
        }
    }

    public static void testMPE() throws Exception {
        int i = 0;
        for(BayesianNetwork bn : BayesianNetworkFactory.bayesianNetworks){

            System.out.println("\nMPE su rete " + ++i);
            RandomVariable v1 = bn.getVariablesInTopologicalOrder().get(1);
            AssignmentProposition[] a1 =
                    new AssignmentProposition[] {
                            new AssignmentProposition(v1,((FiniteDomain)v1.getDomain()).getValueAt(0))};

            System.out.println("Assign: " + Arrays.toString(a1));
            Jointree j = new Jointree(bn);
            JointreeAsk jAsk = new JointreeAsk(new Jointree(bn),"MPE",a1);
        }

    }
}
