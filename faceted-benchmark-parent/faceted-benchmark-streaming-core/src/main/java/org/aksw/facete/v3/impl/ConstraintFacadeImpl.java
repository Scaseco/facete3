package org.aksw.facete.v3.impl;

import java.util.Collection;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetConstraint;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.NodeValue;

public class ConstraintFacadeImpl<B extends FacetNodeResource>
	implements ConstraintFacade<B>
{
	protected B parent;
	
	public ConstraintFacadeImpl(B parent) {
		this.parent = parent;
	}

	
	@Override
	public Collection<FacetConstraint> list() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConstraintFacade<B> eq(Node node) {
		new E_Equals(NodeValue.makeNode(parent.state().asNode()), NodeValue.makeNode(node));
		
		return this;
	}

	@Override
	public ConstraintFacade<B> gt(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConstraintFacade<B> neq(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public B end() {
		return parent;
	}

}
