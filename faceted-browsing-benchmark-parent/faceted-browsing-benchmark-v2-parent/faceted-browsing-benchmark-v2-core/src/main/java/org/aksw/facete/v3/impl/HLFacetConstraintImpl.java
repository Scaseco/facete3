package org.aksw.facete.v3.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;

public class HLFacetConstraintImpl<P>
	implements HLFacetConstraint<P>
{
	protected P parent;
	protected FacetNode facetNode;
	
	// The expression that can be added and removed from the state
	//protected Expr constraintExpr;
	
	protected FacetConstraint state;
	
	public HLFacetConstraintImpl(P parent, FacetNode facetNode, FacetConstraint state) {
		super();
		this.parent = parent;
		this.facetNode = facetNode;
		this.state = state;
	}

	@Override
	public FacetConstraint state() {
		return state;
	}

	@Override
	public Expr expr() {
		Expr result = state.expr();
		return result;
	}

	public Map<Node, FacetNode> mentionedFacetNodes() {
		FacetedQueryResource fqr = facetNode.query().as(FacetedQueryResource.class);
//		FacetNodeResource root = facetNode.root().as(FacetNodeResource.class); 
		FacetNodeResource root = facetNode.query().root().as(FacetNodeResource.class); 

		BgpNode rootState = root.state();
		Expr baseExpr = state.expr();
		
		PathAccessor<BgpNode> pathAccessor = new PathAccessorImpl(rootState);
		
		Map<Node, BgpNode> paths = PathAccessorImpl.getPathsMentioned(baseExpr, pathAccessor::tryMapToPath);
		
		Map<Node, FacetNode> result = new LinkedHashMap<>();
		
		for(Entry<Node, BgpNode> e : paths.entrySet()) {
			result.put(e.getKey(), new FacetNodeImpl(fqr, e.getValue()));
		}
//		accessor.tryMapToPath(node)
		
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = obj instanceof HLFacetConstraint && Objects.equals(state, ((HLFacetConstraint)obj).state());
		return result;
	}


	@Override
	public boolean isActive() {
		Collection<FacetConstraint> items = facetNode.constraints().list();
		boolean result = items.contains(state);
		return result;
	}

	@Override
	public boolean setActive() {
		Collection<FacetConstraint> items = facetNode.constraints().list();
		boolean result = items.add(state);
		return result;
		//return this;
	}
	
	@Override
	public boolean remove() {
		Collection<FacetConstraint> items = facetNode.constraints().list();
		boolean result = items.remove(state);
		return result;
		//return this;
	}

	@Override
	public P parent() {
		return parent;
	}
}
