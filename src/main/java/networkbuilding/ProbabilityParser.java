package networkbuilding;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ProbabilityParser implements Comparable<ProbabilityParser>{


    private static String ONE_VAR = "(\\w+)";
    private static String TWO_VARS = "((\\w+)\\s*\\|\\s*(\\w+))";
    private static String MORE_VARS = "((\\w+)\\s*\\|\\s*((\\w+),\\s*)+\\s*(\\w+))";

    private static String OPEN_C = "\\s*\\(\\s*";
    private static String CLOSE_C= "\\s*\\)\\s*";

    private static String VAR_DEF = OPEN_C+"("+ONE_VAR+"|"+TWO_VARS+"|"+MORE_VARS+")"+CLOSE_C;

    private static String ONE_VAL = ONE_VAR;
    private static String MORE_VALS = "(((\\w+),\\s*)+\\s*(\\w+))";
    private static String VALS = "("+OPEN_C+"("+ONE_VAL+"|"+MORE_VALS+")"+CLOSE_C+")";
    private static String ONE_PROB = "((\\s*(\\d+\\.\\d+)\\s*)|((\\s*(\\d+\\.\\d+)\\s*)e(-?)(\\d+)))";
    private static String ONE_PROB_EXP = "("+ONE_PROB + "e(-?)(\\d+))";


    private static String MORE_PROBS = "(("+ONE_PROB+",\\s*)+"+ONE_PROB+")";


    private static String ALL_PROBS = "("+ONE_PROB +"|"+MORE_PROBS +")\\s*;";
    private static String VALS_PROBS = "("+"("+ONE_VAL+"|"+VALS+")"+ALL_PROBS+"\\s*)+";

    public static String PATTERN_BIF_PROBABILITY = "probability"+VAR_DEF+"\\{\\s*"+VALS_PROBS+"\\s*}\\s*";

    private static String REMOVABLE_WORD_CHARS_HEADER = "(probability)|[^\\w ]";
    private static String REMOVABLE_WORD_CHARS_PROB = "[^\\de\\-\\. ]|e\\w+|e\\s+";

    private String condVar;

    private Set<String> setParsedVariables;

    private String[] parsedVariables;
    private double[] probabilities;

    public ProbabilityParser(String toParse) throws Exception {
        String[] vars;
        String[] probsString;
        double [] probs;
        if(!toParse.matches(PATTERN_BIF_PROBABILITY))
            throw new Exception("wrong probability definition " + toParse);

        setParsedVariables = new HashSet<>();
        String[] parsed = toParse.split("\\{");
        vars = parsed[0].replaceAll(REMOVABLE_WORD_CHARS_HEADER,"").
                replaceAll("(\\s+\\s)", " ").trim().split(" ");
        probsString = parsed[1].replaceAll(REMOVABLE_WORD_CHARS_PROB,"").
                replaceAll("(\\s+\\s)", " ").trim().split(" ");
        probs = new double[probsString.length];
        for (int i = 0; i < probs.length; i++)
            probs[i] = Double.parseDouble(probsString[i]);
        condVar = vars[0];

        for (int i = 1; i < vars.length; i++)
            setParsedVariables.add(vars[i]);

        this.parsedVariables = vars;
        this.probabilities = probs;
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
