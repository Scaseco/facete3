package org.hobbit.benchmark.faceted_browsing.v2.expr;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.apache.jena.sparql.core.Var;

/**
 * Higher order binary relation which connects multiple source variables
 * to multiple target variablbes
 * 
 * @author Claus Stadler, Jun 5, 2018
 *
 */
interface ConceptEx {
	List<Var> getVars();
}

interface BinaryRelationEx {
	List<Var> getSourceVars();
	List<Var> getTargetVars();
	
	List<Var> getIntermediaryVars();
}

public interface RelationOps {
	/**
	 * Project can be used to pick a sub set of the given relation's distinguished variables
	 * 
	 * @param relation
	 * @param vars
	 */
	public static void project(Relation relation, List<Var> vars) {
		
	}
	
//	public static void groupBy(Relation relation, ) {
//		
//	}
	

	/**
	 * 
	 * @param attrRel
	 * @param filterRel
	 */
//	public static Relation join(Relation attrRel, Relation filterRel) {
//		ElementUtils.
//	}
	
}
