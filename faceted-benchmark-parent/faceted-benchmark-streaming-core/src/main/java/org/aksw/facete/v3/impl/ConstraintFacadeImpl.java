package org.aksw.facete.v3.impl;

import java.util.Collection;
import java.util.Set;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.NodeValue;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

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
		Resource modelRoot = parent.query().modelRoot();

		Set<FacetConstraint> set = new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);
		
		FacetConstraint c = modelRoot.getModel().createResource().as(FacetConstraint.class);
		c.expr(new E_Equals(NodeValue.makeNode(parent.state().asNode()), NodeValue.makeNode(node)));
		// TODO Using blank nodes for exprs was a bad idea...
		// We should just allocate var names
		
		//c.expr(new E_Equals(new ExprVar((Var)parent.state().asNode()), NodeValue.makeNode(node)));
		set.add(c);
		
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
