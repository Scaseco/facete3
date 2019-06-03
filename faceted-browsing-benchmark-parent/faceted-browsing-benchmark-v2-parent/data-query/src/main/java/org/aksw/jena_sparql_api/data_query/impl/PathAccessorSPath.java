package org.aksw.jena_sparql_api.data_query.impl;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.data_query.api.PathAccessor;
import org.aksw.jena_sparql_api.data_query.api.SPath;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class PathAccessorSPath
	implements PathAccessor<SPath>
{
	@Override
	public Class<SPath> getPathClass() {
		return SPath.class;
	}
	
	@Override
	public SPath getParent(SPath path) {
		return path.getParent();
	}

	@Override
	public BinaryRelation getReachingRelation(SPath path) {
		return path.getReachingBinaryRelation();
	}

	@Override
	public String getAlias(SPath path) {
		return path.getAlias();
	}

	@Override
	public boolean isReverse(SPath path) {
		return path.isReverse();
	}
	
	@Override
	public String getPredicate(SPath path) {
		return path.getPredicate();
	}

	@Override
	public SPath tryMapToPath(Node node) {
		SPath result = node instanceof NodePath ? ((NodePath)node).getPath() : null;

		return result;
	}
}
