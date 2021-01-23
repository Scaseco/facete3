package org.aksw.jena_sparql_api.entity.graph.api;

import java.util.Collection;

import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplate;

/**
 * An entity graph node combines a base relation that intensionally specifies the set of
 * entities using SPARQL with a set of {@link EntityGraphFragments} that describe the attributes.
 * 
 * 
 * For example, a set of people may be described by the unary relation derived from the SPARQL query
 * [SELECT] ?s [WHERE] {?s a Person}.
 * 
 * e a union of EntityGraphFragments
 * 
 * @author raven
 *
 */
public interface EntityGraphNode {
	// The base entity template providing the
	EntityTemplate getBaseTemplate();
	
	Collection<EntityGraphFragment> getGraphFragments();
}