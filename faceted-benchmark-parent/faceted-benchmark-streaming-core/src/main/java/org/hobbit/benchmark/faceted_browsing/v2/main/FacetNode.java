package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.jena_sparql_api.concepts.Concept;

/**
 * An object backed by the set of resources at a certain (possibly empty) path of properties.
 * 
 * 
 * @author Claus Stadler, Jul 23, 2018
 *
 */
public interface FacetNode {
	/** The set of all outgoing edges - similar corrensponds to the triple ?s ?p ?o */
	FacetEdgeSet out();
}


/**
 * An abstraction of a set of edges
 * 
 * @author Claus Stadler, Jul 23, 2018
 *
 */
interface FacetEdgeSet {
	FacetEdgeSet filter(Concept c);
	FacetEdgeSet order();
	
	// session.root().get("somePredicate").outgoing()
	//                   .out(somePredicate)
	// orderByCount
	// orderByIRI();
	// orderByLabel() - default labels
	// orderByAttribute(BinaryRelation)
	
	// Return the facets (properties) with their distinct value counts
	void getFacetCounts();
}

interface FacetEdge {

}