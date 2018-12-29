import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.example.BayesNetExampleFactory;
import aima.core.probability.example.ExampleRV;
import aima.core.probability.util.RandVar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class App {
    public static void main(String[] args) throws FileNotFoundException {


        BayesianNetwork bn = BayesNetExampleFactory.constructBurglaryAlarmNetwork();/*myNetwork();*/
        Node n = bn.getNode(ExampleRV.ALARM_RV);
        System.out.println(n.getCPD());
/*
        UGraph g = new UGraph(bn);

        printNetwork(bn);
        printGraph(g);
        printClusters(g);*/
    }

    private static void printNetwork(BayesianNetwork bn) throws FileNotFoundException {
        String line = "";
        List<RandomVariable> r = bn.getVariablesInTopologicalOrder();
        for(RandomVariable var : r)
            for (Node child : bn.getNode(var).getChildren())
                line+= var.getName() +","+child.getRandomVariable().getName()+"\n";

        printToFile(line,"network.csv");

    }

    private static void printGraph(UGraph g) throws FileNotFoundException {
        String line = "";
        CNode [] cNodes = g.getcNodes().toArray(new CNode[g.getcNodes().size()]);
        for (int i = 0; i < cNodes.length-1; i++) {
            for (int j = i+1; j < cNodes.length; j++) {
                if(cNodes[j].getConnections().contains(cNodes[i]))
                line+=cNodes[i].getRandomVariable().getName()
                        +","+cNodes[j].getRandomVariable().getName()+"\n";
            }
        }
        printToFile(line,"graph.csv");

    }



    private static void printClusters(UGraph g) throws FileNotFoundException {
        String line = "";
        Cluster [] clusters = g.getClusters().toArray(new Cluster[g.getClusters().size()]);
        for (int i = 0; i < clusters.length-1; i++) {
            for (int j = i+1; j < clusters.length; j++) {
                if(clusters[j].getNeighbours().contains(clusters[i]))
                    line+=clusters[i]+","+clusters[j]+"\n";
                    /*for (CNode c : clusters[i].getMainCNodes()){
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

    public static BayesianNetwork myNetwork() {

        FiniteNode a = boolNode("A");

        FiniteNode b = boolNode("B",a);
        FiniteNode c = boolNode("C",a);

        @SuppressWarnings("unused")
        FiniteNode d = boolNode("D",a,b);


        FiniteNode e = boolNode("E",a,c);

        FiniteNode f = boolNode("F",a);

        @SuppressWarnings("unused")
        FiniteNode g = boolNode("G",d,f);

        @SuppressWarnings("unused")
        FiniteNode h = boolNode("H",e,f);

        return new BayesNet(a);
    }

    private static FiniteNode boolNode(String name, Node... parents){
        return new CNode(boolRandVar(name),prob((int) Math.pow(2, parents.length+1)),parents);
    }

    private static double[] prob(int size){
        double[] prob = new double[size];
        for (int i = 0; i < size; i ++)
            prob[i] = 0.5;
        return prob;
    }





}



