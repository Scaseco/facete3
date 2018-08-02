package org.aksw.facete.v3.api;

import java.util.Collection;

import org.apache.jena.graph.Node;

/**
 * In general, there are anonymous and named constraints.
 * Named constraints can be
 * 
 * 
 * @author raven
 *
 */
public interface ConstraintFacade<B> {
	Collection<FacetConstraint> list();
	
	/** Add an anonymous equal constraint */
	ConstraintFacade<B> eq(Node node);

	ConstraintFacade<B> exists();
	
	ConstraintFacade<B> gt(Node node);
	
	
	ConstraintFacade<B> neq(Node node);
	
	/** End constraint building and return the parent object */
	B end();
}
