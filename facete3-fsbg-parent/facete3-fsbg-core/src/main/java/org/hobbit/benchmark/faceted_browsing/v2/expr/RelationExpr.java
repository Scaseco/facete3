package org.hobbit.benchmark.faceted_browsing.v2.expr;

import java.util.List;
import java.util.Optional;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.TernaryRelation;
import org.apache.jena.sparql.core.Var;

public interface RelationExpr {
    Relation eval();



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

    default Optional<BinaryRelation> asBinaryRelation() {
        return null;
    }


    default Optional<TernaryRelation> asTernaryRelation() {
        return null;
    }
}
