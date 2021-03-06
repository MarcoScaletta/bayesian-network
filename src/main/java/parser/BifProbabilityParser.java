package parser;

import aima.core.probability.RandomVariable;
import aima.core.probability.domain.FiniteDomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BifProbabilityParser{

    private static String ONE_VAR = "(.*)";

    private static String OPEN_C = "\\s*\\(\\s*";
    private static String CLOSE_C= "\\s*\\)\\s*";

    private static String VAR_DEF = "(.*)";

    private static String ONE_VAL = ONE_VAR;
    private static String MORE_VALS = "(((\\w+),\\s*)+\\s*(\\w+))";
    private static String VALS = "("+OPEN_C+"("+ONE_VAL+"|"+MORE_VALS+")"+CLOSE_C+")";
    private static String PROB = "((\\d+\\.\\d+)(e(-|\\+)\\d+)?)";
    private static String ONE_PROB = "\\s*"+PROB+"\\s*";


    private static String MORE_PROBS = "(("+ONE_PROB+",\\s*)+"+ONE_PROB+")";


    private static String ALL_PROBS = "("+ONE_PROB +"|"+MORE_PROBS +")\\s*;";
    private static String VALS_PROBS = "("+"("+ONE_VAL+"|"+VALS+")"+ALL_PROBS+"\\s*)+";

    private static String HEADER = "probability"+VAR_DEF;
    private static String PROB_LINE = VALS_PROBS;

    private static String REMOVABLE_WORD_CHARS_HEADER = "(probability)|[^\\w ]|(\\{)";
    private static String REMOVABLE_WORD_CHARS_PROB = "[()},;\\n]";

    private String condVar;

    private List<String> parsedVariables;

    private String [][] values;
    private double[] probabilities;

    /**
     * Reading of probabilitis
     * @param toParse string to be parsed
     * @param map of string and variables
     * @throws Exception exception
     */
    public BifProbabilityParser(String toParse, Map<String, RandomVariable> map) throws Exception {

        String[] vars;
        String[] probsString;
        List<Double> matchingProbs = new ArrayList<>();
        String [] toParses = toParse.split("\n");

        if(!toParses[0].matches(HEADER))
            throw new Exception("wrong header probability definition " + toParses[0]);
        for (int i = 1; i < toParses.length; i++) {
            if(!toParses[i].matches(PROB_LINE) && !toParses[i].matches("}"))
                throw new Exception("wrong probability definition " + toParses[i]);
        }

        parsedVariables = new ArrayList<>();
        String[] parsed = toParse.split("\\{");
        vars = parsed[0].replaceAll(REMOVABLE_WORD_CHARS_HEADER,"").
                replaceAll("(\\s+\\s)", " ").trim().split(" ");

        probsString = parsed[1].replaceAll(REMOVABLE_WORD_CHARS_PROB,"").
                replaceAll("(\\s+\\s)", " ").trim().split(" ");

        String [] parsedValProb = parsed[1].split(";");
        List<String> val = new ArrayList<>();

        int comb = parsedValProb.length-1;
        for (int i = 0; i < probsString.length; i++) {
            try {
                matchingProbs.add(Double.parseDouble(probsString[i]));
            }catch (Exception e){
                val.add(probsString[i]);
            }
        }
        values = new String[comb][val.size()/comb];


        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++)
                values[i][j] = val.remove(0);
        }
        condVar = vars[0].toUpperCase();
        for (int i = 1; i < vars.length; i++){

            parsedVariables.add(vars[i].toUpperCase());
        }

        this.probabilities = new double[matchingProbs.size()];
        for (int i = 0; i < probabilities.length; i++)
            probabilities[i] = matchingProbs.get(i);
    }

    public double[] getProbabilities() {
        return probabilities;
    }

    public String getCondVar() {
        return condVar;
    }

    public List<String> getParsedVariables() {
        return parsedVariables;
    }

    @Override
    public String toString() {
        String ret =  "( "+condVar + " ";
        if(getParsedVariables().size() > 0)
            ret+= "| ";
        for(String s : getParsedVariables())
            ret += s + " ";
        ret = ret.substring(0,ret.length()-1);
        ret+= " )";
        return ret;
    }

}
