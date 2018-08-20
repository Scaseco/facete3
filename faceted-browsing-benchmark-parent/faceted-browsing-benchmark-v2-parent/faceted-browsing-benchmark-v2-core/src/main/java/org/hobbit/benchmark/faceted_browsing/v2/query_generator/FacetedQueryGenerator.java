package org.hobbit.benchmark.faceted_browsing.v2.query_generator;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.query.Query;

public class FacetedQueryGenerator {
	/**
	 * Generate the query whose valuation yields the set of properties
	 * of the resources matching the given concept.
	 * 
	 * 
	 * ?p : (concept(?s) . ?s ?p ?o)
	 * 
	 * @param concept
	 * @return
	 */
	public Concept createQueryFacets(Concept concept) {
		return null;
	}
	
	
	
	/**
	 * Generate the query that yields the set of properties together
	 * with the respective count of resources for which the property exists
	 * 
	 * SELECT ?p (COUNT(DISTINCT(?s)) AS ?c) {
	 *   concept(?s) .
	 *   ?s ?p ?o
	 * }
	 * 
	 * @param concept
	 * @return
	 */
	public Query createQueryFacetCounts(Concept concept) {
		return null;
	}
}
