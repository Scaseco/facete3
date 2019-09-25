package org.hobbit.benchmark.faceted_browsing.v2.expr;

import java.util.List;
import java.util.Optional;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
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
