package org.aksw.facete.v3.impl;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
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

	public Set<FacetNode> mentionedFacetNodes() {
		FacetedQueryResource fqr = facetNode.query().as(FacetedQueryResource.class);
		FacetNodeResource root = facetNode.root().as(FacetNodeResource.class); 
		BgpNode rootState = root.state();
		Expr baseExpr = state.expr();
		
		PathAccessor<BgpNode> pathAccessor = new PathAccessorImpl(rootState);
		
		Set<BgpNode> paths = PathAccessorImpl.getPathsMentioned(baseExpr, pathAccessor::tryMapToPath);
		
		Set<FacetNode> result = new LinkedHashSet<>();
		
		for(BgpNode bgpNode : paths) {
			result.add(new FacetNodeImpl(fqr, bgpNode));
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
		boolean result = facetNode.constraints().list().contains(state);
		return result;
	}

	@Override
	public boolean setActive() {
		boolean result = facetNode.constraints().list().add(state);
		return result;
		//return this;
	}
	
	@Override
	public boolean remove() {
		boolean result = facetNode.constraints().list().remove(state);
		return result;
		//return this;
	}

	@Override
	public P parent() {
		return parent;
	}
}
