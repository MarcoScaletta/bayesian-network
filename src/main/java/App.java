import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.exact.EliminationAsk;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.domain.FiniteDomain;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;
import aima.core.probability.util.RandVar;
import algorithm.BookAlgorithm;
import javafx.util.Pair;
import logger.Logger;
import networkbuilding.bnparser.BifBNReader;
import structures.CNode;
import structures.Jointree;
import structures.impl.Cluster;
import structures.impl.JointreeAsk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class App {


    private static Map<String, String> map = new HashMap<>();




    public static void main(String[] args) throws Exception {

        RandomVariable a =  boolRandVar("A");
        RandomVariable b =  boolRandVar("B");
        RandomVariable e =  boolRandVar("E");

        Factor ABE = new ProbabilityTable(
                new double[] {0.95,0.05,0.94, 0.06,0.29, 0.71,0.001, 0.999},b,e,a);

//        BookAlgorithm.printFactor(ABE);
//        BookAlgorithm.printFactor(ABE.sumOut(a,e));
//        System.out.println(BookAlgorithm.projectArgmax(ABE,b));

        BayesianNetwork bn = new BifBNReader("hailfinder.bif") {

            protected Node nodeCreation(RandomVariable var, double[] probs, Node... parents) {
                return new CNode(var,probs,parents);
            }
        }.getBayesianNetwork();



//
        RandomVariable var0 = bn.getVariablesInTopologicalOrder().get(0);
        RandomVariable var1 = bn.getVariablesInTopologicalOrder().get(1);
        AssignmentProposition [] a1 = // new AssignmentProposition[0];
                new AssignmentProposition[] {
                        new AssignmentProposition(var0,((FiniteDomain)var0.getDomain()).getValueAt(0)),
                        new AssignmentProposition(var1,((FiniteDomain)var1.getDomain()).getValueAt(0))};
        System.out.println("Assign=" + Arrays.toString(a1));
        Jointree j2 = new Jointree(bn);


//        for (Cluster c : j2.getClusters()) {
//            if(c == null)
//                System.err.println("cluster null");
//            else if(c.getFactor() == null)
//                System.err.println(c + "c.getFactor() null");
//
//
//        }
//        for (Cluster c : j2.getClusters()){
//                BookAlgorithm.printFactor(c.getFactor());
//                System.out.println();
//        }
        printClusters(j2);
//        Factor jAlgo,ask;
//
//        boolean found;
//        int error = 0;
//        int tot=0;
        JointreeAsk jAsk = new JointreeAsk(j2,"MPE",a1);
//        for (RandomVariable r : bn.getVariablesInTopologicalOrder()){
//            if(!r.equals(var1)) {
//                System.out.println("\nSTART:");
//                System.out.println("Assign: " + Arrays.toString(a1));
//                System.out.println("Query var: " + r);
//                jAlgo =
//                        (ProbabilityTable)  jAsk.ask(new RandomVariable[]{r}, a1, bn);
//                ask = (ProbabilityTable) new EliminationAsk().ask(new RandomVariable[]{r}, a1, bn);
//                System.out.println("MioVal: " + jAlgo);
//                System.out.println("Atteso: " + ask);
//                System.out.println("Calculating error on " + r);
//                found= false;
//                for (int i = 0; i < jAlgo.getValues().length && !found; i++) {
//                    if(Math.abs(jAlgo.getValues()[i]-ask.getValues()[i]) > 0.00000000001){
//                        found = true;
//                        error++;
//                    }
//                }
//                tot++;
//            }
//        }
//        System.out.println("Total errors: " + error +" of "+tot);
//        System.out.println("FINISH");

    }


    private static void setMap(BayesianNetwork bn){
        List<RandomVariable> vars = bn.getVariablesInTopologicalOrder();
        String subString = "";
        for(RandomVariable r : vars){
            for(RandomVariable var : vars){
                if(!var.equals(r))
                    for (int i = 0; i < r.getName().length() &&
                            var.getName().startsWith(subString); i++) {

                        subString = r.getName().substring(0,i);

                    }
            }
            map.put(r.getName(),subString);
        }
    }


    private static void printNetwork(BayesianNetwork bn) throws FileNotFoundException {
        String line = "";
        List<RandomVariable> r = bn.getVariablesInTopologicalOrder();
        for(RandomVariable var : r)
            for (Node child : bn.getNode(var).getChildren())
                line+= map.get(var.getName()) +","+map.get(child.getRandomVariable().getName())+"\n";

        printToFile(line,"network.csv");

    }

    private static void printGraph(Jointree g) throws FileNotFoundException {
        String line = "";
        RandomVariable [] vars = g.getBn().getVariablesInTopologicalOrder().toArray(new RandomVariable[0]);
        CNode cI,cJ;
        for (int i = 0; i < vars.length-1; i++) {
            cI = (CNode) g.getBn().getNode(vars[i]);
            for (int j = i+1; j < vars.length; j++) {
                cJ = (CNode) g.getBn().getNode(vars[j]);
                if(cJ.getConnections().contains(cI))
                    line+=map.get(cI.getRandomVariable().getName())
                            +","+map.get(cJ.getRandomVariable().getName())+"\n";
            }
        }
        printToFile(line,"graph.csv");

    }



    private static void printClusters(Jointree g) throws FileNotFoundException {
        String line = "";
        Cluster [] clusters = g.getClusters().toArray(new Cluster[0]);
        for (int i = 0; i < clusters.length-1; i++) {
            for (int j = i+1; j < clusters.length; j++) {
                if(clusters[j].getNeighbours().contains(clusters[i]))
                    line+=clusters[i]+","+clusters[j]+"\n";
            }
        }
        printToFile(line,"clusters.csv");
    }

    private static void printToFile(String string, String fileName)throws FileNotFoundException{
        PrintWriter pw = new PrintWriter(new File(fileName));
        pw.write(string);
        pw.close();
    }

    private static RandomVariable boolRandVar(String name){
        return new RandVar(name, new BooleanDomain());
    }

    public static Pair<BayesianNetwork,AssignmentProposition[]> myNetwork() {

        AssignmentProposition[] assigments;
        Set<AssignmentProposition> assigmentSet = new HashSet<>();
        FiniteNode a = boolNode("A");
        assigmentSet.add(new AssignmentProposition(a.getRandomVariable(), false));
        FiniteNode b = boolNode("B");
        assigmentSet.add(new AssignmentProposition(b.getRandomVariable(), true));
        FiniteNode c = boolNode("C",a,b);

        @SuppressWarnings("unused")
        FiniteNode d = boolNode("D",c);


        FiniteNode f = boolNode("F");

        FiniteNode e = boolNode("E",c,f);

        @SuppressWarnings("unused")
        FiniteNode g = boolNode("G",f);
        assigments = assigmentSet.toArray(new AssignmentProposition[0]);
        return new Pair<>(new BayesNet(a,b,f),assigments);
    }

    private static FiniteNode boolNode(String name, Node... parents){
        return new CNode(boolRandVar(name),prob((int) Math.pow(2, parents.length+1)),parents);
    }

    private static double[] prob(int size){
        double[] prob = new double[size];
        for (int i = 0; i < size; i+=2){
            prob[i] = 0.6;
            prob[i+1] = 0.4;
        }
        return prob;
    }

    public static Pair<BayesianNetwork,AssignmentProposition[]> myNetwork1() {
        FiniteNode a = new CNode(
                new RandVar("Aa",new BooleanDomain()),
                new double[] {0.3,0.7});
        FiniteNode b = new CNode(
                new RandVar("B",new BooleanDomain()),
                new double[] {0.2,0.8,0.75,0.25},a);
        FiniteNode c = new CNode(
                new RandVar("C",new BooleanDomain()),
                new double[] {0.7,0.3,0.1,0.9},a);
        FiniteNode d = new CNode(
                new RandVar("D",new BooleanDomain()),
                new double[] {0.95,0.05,0.9,0.1,0.8,0.2,0,1},b,c);
        FiniteNode e = new CNode(
                new RandVar("E",new BooleanDomain()),
                new double[] {0.7,0.3,0,1},c);
        AssignmentProposition[] assigments;
        Set<AssignmentProposition> assigmentSet = new HashSet<>();
        assigmentSet.add(new AssignmentProposition(a.getRandomVariable(), false));
        assigments = assigmentSet.toArray(new AssignmentProposition[0]);
        return new Pair<>(new BayesNet(a),assigments);
    }




}



