package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.core.Var;


/**
 * An object backed by the set of resources at a certain (possibly empty) path of properties.
 * 
 * 
 * @author Claus Stadler, Jul 23, 2018
 *
 */
public interface FacetNode {
	FacetedQuery query();
	
	FacetDirNode fwd();
	FacetDirNode bwd();

	
	// Convenience shortcuts
	default FacetMultiNode fwd(Property property) {
		return fwd().via(property);
	}

	default FacetMultiNode bwd(Property property) {
		return bwd().via(property);
	}

	FacetNode as(String varName);
	FacetNode as(Var var);
	Var alias();


	FacetNode parent();

	BinaryRelation getReachingRelation();
	
	FacetNode root();
	
	/** Get the set of simple constraints affecting this facet.
	 * Simple constraints are expressions making use of only a single variable.
	 * The set of constraints is treated as a disjunction */
	//Set<Expr> getConstraints();

	/**
	 * List all
	 * 
	 * @return
	 */
//	Set<FacetConstraint> constraints();

	ConstraintFacade<? extends FacetNode> constraints();
	
	//Concept toConcept();
	
	// TODO Some API to get the values of this node by excluding all constraints
	DataQuery<?> availableValues();
	DataQuery<?> remainingValues();
}

