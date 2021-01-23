package org.aksw.jena_sparql_api.entity.query.api;

import java.util.Collection;

import org.aksw.jena_sparql_api.entity.graph.api.EntityGraphNode;

//public interface EntityRelation {
//	
//}

public interface EntityQueryNode {
	
	/**
	 * The set of entity graph nodes that back this query node.
	 * The attributes that can be traversed depend on it.
	 * 
	 * @return
	 */
	Collection<EntityGraphNode> getEntityGraphNode();
	
	// stream<Entry<EntityGraphFragment, Node> streamPredicates()

	
}
