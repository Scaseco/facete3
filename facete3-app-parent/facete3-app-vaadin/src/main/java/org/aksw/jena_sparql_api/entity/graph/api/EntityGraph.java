package org.aksw.jena_sparql_api.entity.graph.api;

import java.util.Collection;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.expr.ExprList;

import com.google.common.collect.Multimap;





/**
 * The entity graph builder provides the management interface for building
 * structures of {@link EntityGraphNode}s and {@link EntityGraphFragment}s.
 * 
 * @author raven
 *
 */
interface EntityGraphBuilder {

}


// TODO We need a function to build the effective entity graph fragment
// from an EntityGraphNode's base relation and an AttributeSpec
// However, the template does not have to be dynamic (though the API design could allow for that)

interface AttributeValueSet {
	
	// EntityGraphFragment 
	
}




/**
 * A virtual (RDF) graph is a collection of RDF-to-RDF views which are
 * in essence SPARQL construct queries with additional metadata.
 * 
 * The naming Graph is derived from Jena's {@link Graph} class which
 * represents a collection of triples.
 * 
 * A virtual graph allows for rewriting SPARQL queries to SPARQL
 * 
 * @param V The view type
 */
interface VirtualGraph {
	
}


interface SourceSelector<V> {
	Collection<V> selectSources(
			QuadPattern quads,
			ExprList conditions,
			Object constraints);
}



//public interface EntityGraph {
//	
//	
//	
//	/**
//	 * 
//	 * @return
//	 */
//	EntityQueryImpl getEntityGraphForPredicate(Node p);
//	
//	
//}
