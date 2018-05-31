package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.apache.jena.sparql.core.Var;

public interface PathAccessor<P> {
	Class<P> getPathClass();
	
	P getParent(P path);
	BinaryRelation getReachingRelation(P path);
	
	
	boolean isReverse(P path);
	String getPredicate(P path);
	
	Var getAlias(P path);
}
