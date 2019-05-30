package org.aksw.facete.v3.path.impl;

import org.aksw.facete.v3.path.api.PathResolverDirNode;
import org.aksw.facete.v3.path.api.PathResolverNode;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;

// Path resolver over partitioned queries
public class PathResolverNodePQ
	implements PathResolverNode<Void>
{
	protected PartitionedQuery1 pq;
	//protected BiFunction<> dirNodeFactory;
	
	@Override
	public PathResolverDirNode<Void> fwd() {
		return null;
	}

	@Override
	public PathResolverDirNode<Void> bwd() {
		return null;
	}

	@Override
	public Void getResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
