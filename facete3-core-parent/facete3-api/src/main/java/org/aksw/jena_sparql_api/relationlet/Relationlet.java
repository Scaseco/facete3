package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.facete.v3.api.path.NestedVarMap;
import org.apache.jena.sparql.core.Var;

/**
 * Probably Relationlet should become the new Relation class
 *
 * The question is how Relation's distinguished vars translate to this class -
 * is it exposedVars? - not really; exposedVars is a Set whereas distinguished vars is a list
 *
 * @author raven
 *
 */
public interface Relationlet
{
    Collection<Var> getExposedVars();
    Set<Var> getVarsMentioned();

    default boolean isPinned(Var var) {
        Set<Var> pinnedVars = getPinnedVars();
        boolean result = pinnedVars.contains(var);
        return result;
    }

    default Relationlet pinVar(Var var) {
        return setPinnedVar(var, true);
    }

    /**
     * Adds all variables <b>currently</b> returned by getVarsMentioned() to the set of fixed vars.
     * Does not mark vars that become available in the future as fixed.
     *
     * @return
     */
    default Relationlet pinAllVars() {
        Set<Var> vars = getVarsMentioned();
        Relationlet result = pinAllVars(vars);
        return result;
    }

    default Relationlet pinAllVars(Iterable<Var> vars) {
        for(Var var : vars) {
            setPinnedVar(var, true);
        //return setVarFixed(var, true);
        }
        return this;
    }

    Set<Var> getPinnedVars();
    Relationlet setPinnedVar(Var var, boolean onOrOff);

    RelationletSimple materialize();

    NestedVarMap getNestedVarMap();

    default Var resolve(VarRefStatic varRef) {
        List<String> labels = varRef.getLabels();
        Var v = varRef.getV();

        NestedVarMap src = getNestedVarMap();
        NestedVarMap tgt = src.get(labels);
        Map<Var, Var> map = tgt.getLocalToFinalVarMap();

        Var result =  map.get(v);
        return result;
    }
}