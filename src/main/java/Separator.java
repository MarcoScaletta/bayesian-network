import aima.core.probability.RandomVariable;

import java.util.Arrays;
import java.util.Set;

public class Separator {

    private Set<RandomVariable> vars;

    public Separator(Set<RandomVariable> vars) {
        this.vars = vars;
    }

    public void setVars(Set<RandomVariable> vars) {
        this.vars = vars;
    }

    @Override
    public String toString() {
        return Arrays.toString(vars.toArray());
    }
}
