package org.aksw.facete.v3.experimental;

import java.util.Collection;

import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.apache.jena.rdf.model.Resource;

public class ResolverDirNode
	extends PathDirNode<ResolverNode, ResolverMultiNode>
{
	protected Resolver resolver;

	
	public ResolverDirNode(ResolverNode parent, boolean isFwd) {
		super(parent, isFwd);
		this.resolver = parent.getResolver();
		this.isFwd = isFwd;
	}
	
	public Resolver getResolver() {
		return resolver;
	}

	public Collection<TernaryRelation> getContrib() {
		Collection<TernaryRelation> result = resolver.getContrib(isFwd);
		return result;
	}

	@Override
	protected ResolverMultiNode viaImpl(Resource property) {
		return new ResolverMultiNode(this, property);
	}
}