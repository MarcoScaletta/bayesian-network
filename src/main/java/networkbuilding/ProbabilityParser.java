package networkbuilding;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProbabilityParser implements Comparable<ProbabilityParser>{

    private static String ALL_CHAR = "((.*)|(\\s*))*";

    private static String ONE_VAR = "(.*)";
    private static String TWO_VARS = "((\\w+)\\s*\\|\\s*(\\w+))";
    private static String MORE_VARS = "((\\w+)\\s*\\|\\s*((\\w+),\\s*)+\\s*(\\w+))";

    private static String OPEN_C = "\\s*\\(\\s*";
    private static String CLOSE_C= "\\s*\\)\\s*";

    private static String VAR_DEF = "(.*)";//OPEN_C+"("+ONE_VAR+"|"+TWO_VARS+"|"+MORE_VARS+")"+CLOSE_C;

    private static String ONE_VAL = ONE_VAR;
    private static String MORE_VALS = "(((\\w+),\\s*)+\\s*(\\w+))";
    private static String VALS = "("+OPEN_C+"("+ONE_VAL+"|"+MORE_VALS+")"+CLOSE_C+")";
    private static String PROB = "((\\d+\\.\\d+)(e(-|\\+)\\d+)?)";
    private static String ONE_PROB = "\\s*"+PROB+"\\s*";
    private static String ONE_PROB_EXP = "("+ONE_PROB + "e(-?)(\\d+))";


    private static String MORE_PROBS = "(("+ONE_PROB+",\\s*)+"+ONE_PROB+")";


    private static String ALL_PROBS = "("+ONE_PROB +"|"+MORE_PROBS +")\\s*;";
    private static String VALS_PROBS = "("+"("+ONE_VAL+"|"+VALS+")"+ALL_PROBS+"\\s*)+";

    private static String HEADER = "probability"+VAR_DEF;
    public static String PROB_LINE = VALS_PROBS;

    public static String PATTERN_BIF_PROBABILITY = "probability"+VAR_DEF+"\\{\\s*"+VALS_PROBS+"\\s*}\\s*";

    private static String REMOVABLE_WORD_CHARS_HEADER = "(probability)|[^\\w ]|(\\{)";
    private static String REMOVABLE_WORD_CHARS_PROB = "[()},;\\n]";

    private String condVar;

    private Set<String> setParsedVariables;

    private String[] parsedVariables;
    private double[] probabilities;
    private List<Double> probs;

    public ProbabilityParser(String toParse) throws Exception {
        probs = new ArrayList<>();
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

        setParsedVariables = new HashSet<>();
        String[] parsed = toParse.split("\\{");
        vars = parsed[0].replaceAll(REMOVABLE_WORD_CHARS_HEADER,"").
                replaceAll("(\\s+\\s)", " ").trim().split(" ");

        probsString = parsed[1].replaceAll(REMOVABLE_WORD_CHARS_PROB,"").
                replaceAll("(\\s+\\s)", " ").trim().split(" ");

        for (int i = 0; i < probsString.length; i++) {
            try {
                matchingProbs.add(Double.parseDouble(probsString[i]));
            }catch (Exception ignored){
            }
        }
        condVar = vars[0].toUpperCase();

        for (int i = 1; i < vars.length; i++)
            setParsedVariables.add(vars[i].toUpperCase());


        this.parsedVariables = vars;
        this.probabilities = new double[matchingProbs.size()];
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = matchingProbs.get(i);
        }

    }

    public void parseHeader(String header) throws Exception {
        String[] vars;
        if(setParsedVariables !=null)
            throw new Exception("Attention already defined conditioning variable: " + header);

        setParsedVariables = new HashSet<>();
        vars = header.replaceAll(REMOVABLE_WORD_CHARS_HEADER,"").
                replaceAll("(\\s+\\s)", " ").trim().split(" ");
        condVar = vars[0];

        for (int i = 1; i < vars.length; i++)
            setParsedVariables.add(vars[i]);
    }

    public void parseProb(String prob) throws Exception {
        String[] vars;
        String[] probsString;
        List<Double> matchingProbs = new ArrayList<>();

        probsString = prob.replaceAll(REMOVABLE_WORD_CHARS_PROB,"").
                replaceAll("(\\s+\\s)", " ").trim().split(" ");


        for (String s : probsString) {
            try {
                probs.add(Double.parseDouble(s));
            } catch (Exception ignored) {
            }
        }
    }

    public void addProb(String prob){

    }


    /*public String[] getParsedVariables() {
        return parsedVariables;
    }*/

    public double[] getProbabilities() {
        return probabilities;
    }

    public String getCondVar() {
        return condVar;
    }

    public Set<String> getSetParsedVariables() {
        return setParsedVariables;
    }

    @Override
    public String toString() {
        String ret =  "( "+condVar + " ";
        if(getSetParsedVariables().size() > 0)
            ret+= "| ";
        for(String s : getSetParsedVariables())
            ret += s + " ";
        ret = ret.substring(0,ret.length()-1);
        ret+= " )";
        return ret;
    }

    @Override
    public int compareTo(ProbabilityParser that) {
        /*for(String s : that.getParsedVariables())
            if(s.equals(this.getParsedVariables()[0]))
                return -1;
        for(String s : this.getParsedVariables())
            if(s.equals(that.getParsedVariables()[0]))
                return 1;
        if(this.getParsedVariables().length < that.getParsedVariables().length)
            return -1;
        if(this.getParsedVariables().length > that.getParsedVariables().length)
            return 1;
        return 0;*/
        return 0;
    }
}
