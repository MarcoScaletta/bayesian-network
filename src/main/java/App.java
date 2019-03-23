import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.exact.EliminationAsk;
import aima.core.probability.bayes.exact.EnumerationAsk;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.RandVar;
import algorithm.BookAlgorithm;
import com.google.common.collect.Sets;
import javafx.util.Pair;
import networkbuilding.BNReader;
import networkbuilding.ProbabilityParser;
import networkbuilding.VariableParser;
import structures.CNode;
import structures.Jointree;
import structures.impl.Cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {


    private static Map<String, String> map = new HashMap<>();

    public static void main(String[] args) throws Exception {

        String s ="(AIDS_early) 0.0, 1.0;";

        System.out.println(s.matches(ProbabilityParser.PROB_LINE));
        BayesianNetwork bn = new BNReader("earthquake.bif") {

            protected Node nodeCreation(RandomVariable var, double[] probs, Node... parents) {
                //System.out.println(Arrays.toString(probs));
                return new CNode(var,probs,parents);
            }
        }.getBayesianNetwork();

        Jointree g = new Jointree(bn);

        RandomVariable v = bn.getVariablesInTopologicalOrder().get(2);
        System.out.println("Pr(" + v + ")\n" + BookAlgorithm.factorElimination2(bn,new ArrayList<>(g.getClusters()), v));
        g = new Jointree(bn);
        System.out.println("Pr(" + v + ")\n" + BookAlgorithm.factorEliminationMex(g, v));


        v = bn.getVariablesInTopologicalOrder().get(4);
        //System.out.println("Pr(" + v + ")\n" + BookAlgorithm.factorElimination2(bn,new ArrayList<>(g.getClusters()), v));
        //g = new Jointree(bn);
        System.out.println("Pr(" + v + ")\n" + BookAlgorithm.factorEliminationMex(g,  v));
//        setMap(bn);
//        printNetwork(bn);
//        printGraph(g);
        printClusters(g);
//        g = new Jointree(bn);
//        for (RandomVariable v : bn.getVariablesInTopologicalOrder()) {
//            g = new Jointree(bn);
//            System.out.println("Pr(" + v + ")\n" + BookAlgorithm.factorElimination2(bn,new ArrayList<>(g.getClusters()), v));
//            System.out.println(new EliminationAsk().ask(new RandomVariable[]{v}, new AssignmentProposition[]{}, bn));
//        }


//
//
//
//        for (RandomVariable v : bn.getVariablesInTopologicalOrder()) {
//            long t;
//            g = new Jointree(bn);
//            System.out.println("\nPr(" + v + ")");
//            try {
//                t = System.currentTimeMillis();
//
//                System.out.println(BookAlgorithm.factorElimination3(bn, new ArrayList<>(g.getClusters()), v));
////                System.out.println("Time (millis): " + (System.currentTimeMillis() - t));
//                System.out.println(new EnumerationAsk().ask(new RandomVariable[]{v}, new AssignmentProposition[]{}, bn));
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//
//            g = new Jointree(bn);
//
//            t = System.currentTimeMillis();
//            System.out.println(new EliminationAsk().ask(new RandomVariable[]{v}, new AssignmentProposition[]{}, bn));
//            System.out.println("Time (millis): " + (System.currentTimeMillis()-t));
//
//            g = new Jointree(bn);
//
//            try {
//
//                t = System.currentTimeMillis();
//                System.out.println(BookAlgorithm.factorElimination3(bn, new ArrayList<>(g.getClusters()), v));
//                System.out.println("Time (millis): " + (System.currentTimeMillis()-t));
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//
//
//
//        RandomVariable a = new RandVar("A",new BooleanDomain());
//        RandomVariable b = new RandVar("B",new BooleanDomain());
//        Pair<BayesianNetwork,AssignmentProposition[]> pair = myNetwork1();
//
//        myNetwork();
//
//
//        structures.Jointree g = new structures.Jointree(pair.getKey());
//
//        printNetwork(pair.getKey());
//        printGraph(g);
//        printClusters(g);
//        algorithm.ClusterAsk.getPriorMarginal(
//                pair.getValue(),
//                pair.getKey(),
//                g);
//    }
    }


    private static void setMap(BayesianNetwork bn){
        List<RandomVariable> vars = bn.getVariablesInTopologicalOrder();
        int numChar;
        String subString = "";
        for(RandomVariable r : vars){
            numChar = 1;
            for(RandomVariable var : vars){
                if(!var.equals(r))
                    for (int i = 0; i < r.getName().length() &&
                            var.getName().startsWith(subString); i++) {

                        subString = r.getName().substring(0,i);

                    }
            }
            map.put(r.getName(),subString);
            //System.out.println(subString);
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
        RandomVariable [] vars = g.getBn().getVariablesInTopologicalOrder().toArray(new RandomVariable[g.getBn().getVariablesInTopologicalOrder().size()]);
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
        Cluster [] clusters = g.getClusters().toArray(new Cluster[g.getClusters().size()]);
        for (int i = 0; i < clusters.length-1; i++) {
            for (int j = i+1; j < clusters.length; j++) {
                if(clusters[j].getNeighbours().contains(clusters[i]))
                    line+=clusters[i]+","+clusters[j]+"\n";
                    /*for (structures.CNode c : clusters[i].getMainCNodes()){
                        if(!c.getFactor().equals(""))
                            line+=clusters[i]+","+c.getFactor()+"\n";
                        line+=clusters[i]+","+c.getEvidenceIndicator()+"\n";
                    }*/
            }
        }
        printToFile(line,"clusters.csv");
    }

    private static void printToFile(String string, String fileName)throws FileNotFoundException{
        PrintWriter pw = new PrintWriter(new File(fileName));
        pw.write(string);
        pw.close();
    }

    public static RandomVariable boolRandVar(String name){
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
        assigments = assigmentSet.toArray(new AssignmentProposition[assigmentSet.size()]);
        return new Pair<>((BayesianNetwork) new BayesNet(a,b,f),assigments);
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
        assigments = assigmentSet.toArray(new AssignmentProposition[assigmentSet.size()]);
        return new Pair<>((BayesianNetwork) new BayesNet(a),assigments);
    }




}



