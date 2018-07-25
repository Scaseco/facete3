package org.aksw.facete.v3.impl;

import java.util.Set;

import org.aksw.facete.v3.api.DataQuery;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;


public class FacetNodeImpl
	implements FacetNodeResource
{
	protected Resource state;
	
	protected FacetNodeImpl(Resource state) {
		this.state = state;
	}
	
	@Override
	public Resource state() {
		return state;
	}
	
	@Override
	public FacetDirNode fwd() {
		return new FacetDirNodeImpl(this, true);
	}

	@Override
	public FacetDirNode bwd() {
		return new FacetDirNodeImpl(this, false);
	}

	@Override
	public Set<Expr> getConstraints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataQuery availableValues() {
		// TODO Auto-generated method stub
		return null;
	}

	public void as(String varName) {
		ResourceUtils.setLiteralProperty(state, Vocab.alias, varName);		
	}
	
	@Override
	public void as(Var var) {
		as(var.getName());
	}
	
	@Override
	public Var getAlias() {
		return ResourceUtils.getLiteralPropertyValue(state, Vocab.alias, String.class)
			.map(Var::alloc).orElse(null);
	}

	@Override
	public FacetNode parent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FacetNode root() {
		// TODO Auto-generated method stub
		return null;
	}
}
