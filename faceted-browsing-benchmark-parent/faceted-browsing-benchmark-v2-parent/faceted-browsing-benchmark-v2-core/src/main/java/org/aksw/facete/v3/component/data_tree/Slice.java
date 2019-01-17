package org.aksw.facete.v3.component.data_tree;

import java.util.function.Function;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.Relation;

public interface Slice {
	void setFilter(Relation filter);
	void setFilter(Function<? super Relation, ? extends Relation> filterFn);

	public Column addPredicate(String predicate, BinaryRelation binaryRelation);
}
