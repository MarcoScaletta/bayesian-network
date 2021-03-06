package structures.mpe;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;

import java.util.*;

public class MessageMPE {

    private Factor factor;

    private Set<RandomVariable> vars;
    private Map<Assign,Assign> maxAssignMap;

    public MessageMPE(Factor factor, Map<Assign, Assign> maxAssignMap) throws Exception {
        this.factor = factor;
        this.maxAssignMap = maxAssignMap;
        this.vars = new HashSet<>();

        Assign tmp = null;
        for(Assign assign : maxAssignMap.keySet()){
            if(tmp == null) {
                tmp = assign;
                vars.addAll(assign.getAssign().keySet());
            }else if(assign.getAssign().keySet().equals(tmp.getAssign().keySet())){
                if(vars.isEmpty())
                    vars.addAll(assign.getAssign().keySet());
            }else
                throw new Exception("All assignments have to have same random variables");
        }
    }

    public Set<RandomVariable> getVars() {
        return vars;
    }

    public Factor getFactor() {
        return factor;
    }

    public Map<Assign, Assign> getMaxAssignMap() {
        return maxAssignMap;
    }

    @Override
    public String toString() {
        return vars.toString() + "->"+this.maxAssignMap.values();
    }
}
