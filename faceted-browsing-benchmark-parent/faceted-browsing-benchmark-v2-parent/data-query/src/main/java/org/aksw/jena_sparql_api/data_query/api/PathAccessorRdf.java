package org.aksw.jena_sparql_api.data_query.api;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.apache.jena.sparql.core.Var;

public interface PathAccessorRdf<P>
	extends PathAccessorSimple<P>
{
	BinaryRelation getReachingRelation(P path);
	
	boolean isReverse(P path);
	String getPredicate(P path);
	
	Var getAlias(P path);
}
