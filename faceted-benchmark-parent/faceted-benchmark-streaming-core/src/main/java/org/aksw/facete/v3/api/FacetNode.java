package org.aksw.facete.v3.api;

import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;


/**
 * An object backed by the set of resources at a certain (possibly empty) path of properties.
 * 
 * 
 * @author Claus Stadler, Jul 23, 2018
 *
 */
public interface FacetNode {
	FacetDirNode fwd();
	FacetDirNode bwd();

	
	// Convenience shortcuts
	default FacetMultiNode fwd(Property property) {
		return fwd().via(property);
	}

	default FacetMultiNode bwd(Property property) {
		return bwd().via(property);
	}

	void as(String varName);
	void as(Var var);
	Var getAlias();


	FacetNode parent();
	FacetNode root();
	
	/** Get the set of simple constraints affecting this facet.
	 * Simple constraints are expressions making use of only a single variable.
	 * The set of constraints is treated as a disjunction */
	Set<Expr> getConstraints();

	
	//Concept toConcept();
	
	// TODO Some API to get the values of this node by excluding all constraints
	DataQuery availableValues();

}

