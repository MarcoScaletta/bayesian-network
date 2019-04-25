package structures.MPE;

import aima.core.probability.RandomVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Assign {

    private final Map<RandomVariable, Object> assign;

    public Assign(RandomVariable r, Object o){
        this();
        assign.put(r,o);

    }

    private Assign() {
        this(new HashMap<>());
    }

    public Assign(Map<RandomVariable, Object> assign) {
        this.assign = assign;
    }

    public Map<RandomVariable, Object> getAssign() {
        return assign;
    }

    public void merge(Assign assign){
        if(assign != null)
            this.assign.putAll(assign.assign);
    }

    public boolean isSubsetOf(Assign assign){
        Map<RandomVariable,Object> map = new HashMap<>(this.assign);

        for(RandomVariable r : this.assign.keySet())
            if(!assign.assign.containsKey(r) || !assign.assign.get(r).equals(this.assign.get(r)))
                return false;
        return true;
    }

    public Assign getSubset(Set<RandomVariable> vars){
        Map<RandomVariable, Object> map = new HashMap<>();
        for (RandomVariable r : vars){
            map.put(r, assign.get(r));
        }
        return new Assign(map);
    }

    public String toString(){

        return assign/*.keySet()*/.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assign assign1 = (Assign) o;
        return assign.equals(assign1.assign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assign);
    }
}
