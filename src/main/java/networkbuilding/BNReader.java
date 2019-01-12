package networkbuilding;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.impl.BayesNet;
import com.google.common.collect.Lists;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public abstract class BNReader {

    private static String PROB = "probability";
    private static String VAR  = "variable";

    private List<VariableParser> variableParsers;
    private List<ProbabilityParser> probabilityParsers;

    private Map<String, RandomVariable> variableHashMap = new HashMap<>();

    private BayesianNetwork bayesianNetwork;

    public BNReader(String file) throws Exception {
        variableParsers = new ArrayList<>();
        probabilityParsers = new ArrayList<>();
        System.out.println("Build network");
        bayesianNetwork = buildNetwork(file);
    }

    private BayesianNetwork buildNetwork(String file) throws Exception {


        System.out.println("varAndProbDef");
        varAndProbDef(file);

        System.out.println("sortProbParsers");
        sortProbParsers();
//        for (ProbabilityParser probabilityParser : probabilityParsers)
//            System.out.println(probabilityParser);
        Map<String, Node> nodeHashMap = new HashMap<>();
        Set<Node> roots = new HashSet<>();

        String actualVar;
        Set<String> varsName;

        Set<Node> parents;

        Node actualNode;

        for (VariableParser v : variableParsers)
            variableHashMap.put(v.getParsedVariable().getName(),v.getParsedVariable());

        fixProbabilities();

        for (ProbabilityParser p : probabilityParsers){
            actualVar = p.getCondVar();
            varsName = p.getSetParsedVariables();

            if(!variableHashMap.containsKey(actualVar))
                throw new Exception("Variable ["+ actualVar +"] not exists");
            if(varsName.size() > 0){
                parents = new HashSet<>();

                for (String varName : varsName){
                    if(!variableHashMap.containsKey(varName))
                        throw new Exception("Variable ["+ varName +"] not exists");
                    if(!nodeHashMap.containsKey(varName))
                        throw new Exception("Probability for ["+ varName +"] not defined");
                    parents.add(nodeHashMap.get(varName));
                }

                actualNode = nodeCreation(variableHashMap.get(actualVar),p.getProbabilities(),
                        parents.toArray(new Node[parents.size()]));
            }else{
                actualNode = nodeCreation(variableHashMap.get(actualVar),p.getProbabilities());
                roots.add(actualNode);
            }
            nodeHashMap.put(actualVar,actualNode);

        }



        return new BayesNet(roots.toArray(new Node[roots.size()]));
    }



    private Pair<String[],String[]> varAndProbDef(String filename) {

        ProbabilityParser probabilityParser;
        long line = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {

            String sCurrentLine,definition;

            while ((sCurrentLine = br.readLine()) != null) {
                line++;
                definition = "";
                if(sCurrentLine.startsWith(PROB) || sCurrentLine.startsWith(VAR)){
                    do{
                        definition += sCurrentLine+"\n";
                        sCurrentLine = br.readLine();
                        line++;
                    }while(!sCurrentLine.startsWith("}"));
                    definition += sCurrentLine;
                    if(definition.startsWith(PROB)) {
                        try {
                            boolean foundFirst = false;

                            probabilityParser = new ProbabilityParser(definition);
                            for (int i = 0; i <  probabilityParsers.size() && !foundFirst; i++) {
//                                if(probabilityParser.getCondVar().equals("HYPOVOLEMIA")){
//                                    System.out.println("   "+probabilityParsers.get(i));
//                                }

                                if(probabilityParsers.get(i).getSetParsedVariables().contains(
                                        probabilityParser.getCondVar())){
//                                    System.out.println(">>"+probabilityParser.getCondVar() + " contained");
                                    foundFirst = true;
                                    if(i>0){
                                        probabilityParsers.add(i-1,probabilityParser);
                                    }else
                                        probabilityParsers.add(0,probabilityParser);
                                }
                            }
                            if(!foundFirst)
                                probabilityParsers.add(probabilityParser);
//                            System.out.println("Added " + probabilityParser);
//                            System.out.println(Arrays.toString(probabilityParsers.toArray()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.err.println("line:" + line);
                        }
                    }
                    else {

                        try {
                            variableParsers.add(new VariableParser(definition));
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.err.println("line:" + line);
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Node nodeCreation(RandomVariable var, double[] probs){
        return nodeCreation(var, probs,(Node[]) null);
    }

    private void sortProbParsers() throws Exception {
        Set<String> missing = missingVariables(probabilityParsers);
        List<ProbabilityParser> parsers;
        Set<String> added = new HashSet<>();
        if(missing.size()>0)
            throw new Exception("Missing probability for "+ missing);
        parsers = new ArrayList<>();
        ProbabilityParser actual;
        while(probabilityParsers.size() >0){
            actual = probabilityParsers.remove(0);
            if(added.containsAll(actual.getSetParsedVariables())) {
                parsers.add(actual);
                added.add(actual.getCondVar());
            }else
                probabilityParsers.add(actual);
        }
        probabilityParsers =  parsers;
    }

    private void fixProbabilities() throws Exception {
        RandomVariable var;
        double sum = 0;
        double error =Math.pow(10,-6);
        for (ProbabilityParser p : probabilityParsers){
            sum = 0;
            var = variableHashMap.get(p.getCondVar());

            System.out.println("p: "+p);
            System.out.println("p.probs: "+ Arrays.toString(p.getProbabilities()));
            for (int i=0;i<=p.getProbabilities().length;i++){
                if(var == null)
                    throw new Exception("var is null for " + p);

                if(var.getDomain() == null)
                    throw new Exception("var.getDomain() is null");

                if(i>0 && i%var.getDomain().size()==0){
//                    System.out.println("sum: "+sum);
//                    System.out.println("i: "+i);
//                    System.out.println("var.getDomain().size(): "+var.getDomain().size());
                    if(Math.abs(1-sum) > 0){
                        if(Math.abs(1-sum) > error)
                            throw new Exception("Row "+ ((i/var.getDomain().size()) + 1) +" of CPT does not sum to 1.0 with error equals to "+error + " (sum to "+sum+") " + p);
                        else{
                            p.getProbabilities()[i-1]+=(1-sum);
                        }
                    }
                    sum = 0;
                }
                if(i<p.getProbabilities().length) {
                    if ((sum + p.getProbabilities()[i] - p.getProbabilities()[i] != sum)) {


//                        System.out.println("sum="+sum);
//                        System.out.println("p.getProbabilities()[i]="+p.getProbabilities()[i]);
                        sum = sum * 1000;
                        sum += (p.getProbabilities()[i] * 1000);
                        sum = sum / 1000;
//                        System.out.println("p.getProbabilities()["+i+"]: "+p.getProbabilities()[i]);
                    } else{
//                        System.out.println("p.getProbabilities()["+i+"]: "+p.getProbabilities()[i]);
                        sum += p.getProbabilities()[i];
                    }
                }
            }
        }
    }

    private Set<String> missingVariables(List<ProbabilityParser> set){
        Set<String> allVar = new HashSet<>();
        Set<String> missingVariable = new  HashSet<>();
        for (ProbabilityParser parser : set)
            allVar.add(parser.getCondVar());
        for(ProbabilityParser parser : set)
            for (String s : parser.getSetParsedVariables())
                if(!allVar.contains(s))
                    missingVariable.add(s);
        return missingVariable;

    }

    protected abstract Node nodeCreation(RandomVariable var, double[] probs, Node... parents);

    // PUBLIC METHODS

    public BayesianNetwork getBayesianNetwork() {
        return bayesianNetwork;
    }
}
