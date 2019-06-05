package org.aksw.facete.v3.experimental;

import java.util.Collection;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public class ResolverNode
	extends PathNode<ResolverNode, ResolverDirNode, ResolverMultiNode>
{
	protected Resolver resolver;
	
//	public ResolverNode(Resolver resolver) {
//		super();
//		this.resolver = resolver;
//	}

	public ResolverNode(ResolverMultiNode parent, String alias, Resolver resolver) {
		super(parent, alias);
		this.resolver = resolver;
	}	

	public Resolver getResolver() {
		return resolver;
	}

	@Override
	public ResolverDirNode create(boolean isFwd) {
		return new ResolverDirNode(this, isFwd);
	}
	
	Collection<BinaryRelation> getPaths() {
		Collection<BinaryRelation> result = resolver.getPaths();
		return result;
	}


	public static ResolverNode from(Resolver resolver) {
		return new ResolverNode(null, null, resolver);
	}
	
	public static ResolverNode from(PartitionedQuery1 pq) {
		return from(Resolver.from(pq));
	}

	public static ResolverNode from(Query query, Var partitionVar) {
		return from(new PartitionedQuery1(query, partitionVar));
	}
	
	
}