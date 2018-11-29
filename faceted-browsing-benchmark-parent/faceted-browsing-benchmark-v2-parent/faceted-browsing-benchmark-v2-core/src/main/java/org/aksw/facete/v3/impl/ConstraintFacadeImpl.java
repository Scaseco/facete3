package org.aksw.facete.v3.impl;

import java.util.Collection;
import java.util.Set;

import org.aksw.commons.accessors.CollectionFromConverter;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.jena_sparql_api.utils.NodeHolder;
import org.aksw.jena_sparql_api.utils.RangeUtils;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.commons.lang.NotImplementedException;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

import com.google.common.base.Converter;
import com.google.common.collect.Range;

public class ConstraintFacadeImpl<B extends FacetNodeResource>
	implements ConstraintFacade<B>
{
	protected B parent;
	
	public ConstraintFacadeImpl(B parent) {
		this.parent = parent;
	}

	
	@Override
	public Collection<FacetConstraint> list() {
		// TODO Only list the constraints for the parent facet node
		
		Resource modelRoot = parent.query().modelRoot();
		Set<FacetConstraint> set = new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);

		return set;
	}
	
	@Override
	public ConstraintFacade<B> exists() {
		Resource modelRoot = parent.query().modelRoot();
		Collection<FacetConstraint> set = list();
		
		FacetConstraint c = modelRoot.getModel().createResource().as(FacetConstraint.class);
		c.expr(new E_Bound(NodeValue.makeNode(parent.state().asNode())));
		
		set.add(c);

		return this;
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
		throw new NotImplementedException();
	}

	@Override
	public ConstraintFacade<B> neq(Node node) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public B end() {
		return parent;
	}


	@Override
	public ConstraintFacade<B> range(Range<NodeHolder> range) {
		Resource modelRoot = parent.query().modelRoot();

		Set<FacetConstraint> set = new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);
		
		FacetConstraint c = modelRoot.getModel().createResource().as(FacetConstraint.class);
		Expr expr = RangeUtils.createExpr(parent.state().asNode(), range);
		c.expr(expr);
		// TODO Using blank nodes for exprs was a bad idea...
		// We should just allocate var names
		
		//c.expr(new E_Equals(new ExprVar((Var)parent.state().asNode()), NodeValue.makeNode(node)));
		set.add(c);
		
		return this;
	}


	@Override
	public Collection<HLFacetConstraint> listHl() {
		Collection<FacetConstraint> lowLevel = list();

		CollectionFromConverter<HLFacetConstraint, FacetConstraint> result = new CollectionFromConverter<>(lowLevel, Converter.from(
			hl -> hl.state(),
			ll -> new HLFacetConstraintImpl(parent, ll)
		));
	
		return result;
	}

}
