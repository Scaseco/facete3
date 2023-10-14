package org.hobbit.benchmark.faceted_browsing.v2.expr;

import java.util.List;
import java.util.Optional;

import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.apache.jena.sparql.core.Var;

public interface RelationExpr {
    Fragment eval();



    default RelationExpr project(List<Var> vars) {
        return null;

    }


    /*
     * Conversions to specific relation types
     * TODO - Move to Relation class
     */

    default Optional<Concept> asConcept() {
        return null;
    }

    default Optional<Fragment2> asBinaryRelation() {
        return null;
    }


    default Optional<Fragment3> asTernaryRelation() {
        return null;
    }
}
