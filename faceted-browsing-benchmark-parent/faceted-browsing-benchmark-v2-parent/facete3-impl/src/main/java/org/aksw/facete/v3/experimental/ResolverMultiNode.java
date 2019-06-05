package org.aksw.facete.v3.experimental;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;

public class ResolverMultiNode
	extends PathMultiNode<ResolverNode, ResolverDirNode, ResolverMultiNode>
{
	protected Resolver resolver;
//	protected Map<String, ResolverNode> aliasToNode = new LinkedHashMap<>();

	
	public ResolverMultiNode(ResolverDirNode parent, Resource property) {
		super(parent, property);		
		this.resolver = parent.getResolver();
	}

	public Resolver getResolver() {
		return resolver;
	}

	@Override
	protected ResolverNode viaImpl(String alias) {
		Node n = property.asNode();
		P_Path0 step = isFwd ? new P_Link(n) : new P_ReverseLink(n);
		Resolver child = resolver.resolve(step, alias);
		ResolverNode result = new ResolverNode(this, alias, child);
		return result;
	}

//	@Override
//	public Map<String, ResolverNode> list() {
//		return aliasToNode;
//	}

}