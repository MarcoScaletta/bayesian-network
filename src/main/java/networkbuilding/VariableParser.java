package networkbuilding;

import aima.core.probability.RandomVariable;
import aima.core.probability.domain.*;
import aima.core.probability.util.RandVar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VariableParser {

    private static final String HEADER_NAME_REGEX = "(variable) (\\w+) \\{";
    private static final String TYPE_VALUE_REGEX =
            "( {2})(type) (\\w+) (\\[) (\\d+) ] " +
                    "\\{ ((\\w+)|((\\w)+, )+(\\w+)) };";
    private static final String CLOSE = "}";

    private static final String PATTERN_BIF_VARIABLE = HEADER_NAME_REGEX+"\\s*"+TYPE_VALUE_REGEX+"\\s*"+CLOSE;

    private RandomVariable parsedVariable;
    private String[] types;

    public VariableParser(String toParse) throws Exception {
        String varName;
        int numVal;
        Object [] values;
        Domain domain = null;

        if(!toParse.matches(HEADER_NAME_REGEX+"\n"+TYPE_VALUE_REGEX+"\n"+CLOSE))
            throw new Exception("wrong variable definition " + toParse);

        String[] parsed = toParse.replaceAll(
                "(variable)|(type)|(,)|(;)|(\n)|\\{|}|\\[|]", "")
                .replaceAll("(\\s+\\s)", " ").trim().split(" ");

        varName = parsed[0];

        if (parsed[1].equals("discrete")) {
            values = Arrays.copyOfRange(parsed,3,parsed.length);
            if(values.length != Integer.parseInt(parsed[2]))
                throw new Exception("error defining domain size for variable "+"["+varName+"]");
            domain = new ArbitraryTokenDomain(values);
        }else if(parsed[1].equals("boolean"))
            domain = new BooleanDomain();

        if(domain == null)
            throw new Exception("domain not defined for variable "+"["+varName+"]");

        this.parsedVariable = new RandVar(varName,domain);
    }

    public RandomVariable getParsedVariable() {
        return parsedVariable;
    }
}