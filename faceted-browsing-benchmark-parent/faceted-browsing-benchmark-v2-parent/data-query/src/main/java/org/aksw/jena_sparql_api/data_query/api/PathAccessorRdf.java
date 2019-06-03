package org.aksw.jena_sparql_api.data_query.api;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;

public interface PathAccessorRdf<P>
	extends PathAccessorSimple<P>
{
	BinaryRelation getReachingRelation(P path);
	
	boolean isReverse(P path);
	String getPredicate(P path);
	
	String getAlias(P path);
}
