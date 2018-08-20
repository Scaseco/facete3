package org.hobbit.benchmark.faceted_browsing.v2.engine;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

public class FacetedQueryEngine {
	
	/**
	 * Return a PathQuery that for each property yields the count of distinct values. 
	 * 
	 * SELECT ?p Count(Distinct ?s) {
	 *   $[[concept(?s)]] .
	 *   ?s [[path]] ?o
	 * }
	 * 
	 * 
	 * @param concept
	 * @return
	 */
	Map<Node, Long> getFacetCounts(Concept concept) { // 		//List<Node> nodes);
		return null;
	}
	
	/**
	 * For each target value of the path, count how many distinct source values there are. 
	 * 
	 * @param concept
	 * @param path
	 * @return
	 */
	Map<Node, Long> getFacetValueCounts(Concept concept, Path path) {
		return null;
	}
	
	//getFacetValues();
}
