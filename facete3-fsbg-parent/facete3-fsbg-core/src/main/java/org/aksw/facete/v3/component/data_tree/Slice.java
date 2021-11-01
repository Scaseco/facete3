package org.aksw.facete.v3.component.data_tree;

import java.util.function.Function;

import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.aksw.jenax.sparql.relation.api.Relation;

public interface Slice {
    void setFilter(Relation filter);
    void setFilter(Function<? super Relation, ? extends Relation> filterFn);

    public Column addPredicate(String predicate, BinaryRelation binaryRelation);
}
