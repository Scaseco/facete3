package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;

public interface SPath
	extends Selection
{
	//ExprVar asExpr();
	
//	SPathNode getSource();
//	SPathNode getTarget();
	SPath getParent();
	
	
	String getPredicate();
	boolean isReverse();

	SPath get(String predicate, boolean reverse);
	
	BinaryRelation getReachingBinaryRelation();
//	void setParent(Resource source);
//	void setTarget(Resource target);

	//void setPredicate(Property p);
	//void setReverse(boolean isReverse);
}
