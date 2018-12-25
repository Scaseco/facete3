package org.aksw.facete.v3.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

import com.google.common.base.Converter;
import com.google.common.collect.Iterators;
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
	public boolean hasExpr(Expr expr) {
		boolean result = listHl().stream().map(HLFacetConstraint::expr)
			.filter(e -> Objects.equals(expr, e))
			.findFirst().isPresent();
		
		return result;
	}

	@Override
	public ConstraintFacadeImpl<B> addExpr(Expr expr) {
		Resource modelRoot = parent.query().modelRoot();
		Collection<FacetConstraint> set = list();
		
		FacetConstraint c = modelRoot.getModel().createResource().as(FacetConstraint.class);
		c.expr(new E_Bound(NodeValue.makeNode(parent.state().asNode())));
		
		set.add(c);

		return this;
	}

	@Override
	public boolean removeExpr(Expr expr) {
		ExtendedIterator<Expr> it = WrappedIterator.create(listHl().iterator())
				.mapWith(HLFacetConstraint::expr);
		
		boolean result = Iterators.removeAll(it, Collections.singleton(expr));
		return result;
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
	public Expr thisAsExpr() {
		Expr result = NodeValue.makeNode(parent.state().asNode());
		return result;
	}
	
	@Override
	public HLFacetConstraint<ConstraintFacade<B>> eq(Node node) {
		Expr expr = new E_Equals(thisAsExpr(), NodeValue.makeNode(node));
		
		// Check if constraint with that expression already exists
		List<FacetConstraint> existingEqualConstraints = list().stream().filter(c -> Objects.equals(c.expr(), expr)).collect(Collectors.toList());
		
		FacetConstraint c;
		if(existingEqualConstraints.isEmpty()) {
		
			
			Resource modelRoot = parent.query().modelRoot();
	
			//Collection<FacetConstraint> set = list(); //new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);
			
			
			c = modelRoot.getModel().createResource().as(FacetConstraint.class);
			c.expr(expr);
		} else {
			c = existingEqualConstraints.iterator().next();
		}
		// TODO Using blank nodes for exprs was a bad idea...
		// We should just allocate var names
		
		//c.expr(new E_Equals(new ExprVar((Var)parent.state().asNode()), NodeValue.makeNode(node)));
		
		//set.add(c);
		
		HLFacetConstraint<ConstraintFacade<B>> result = new HLFacetConstraintImpl<>(this, parent, c);

		return result;
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
	public Collection<HLFacetConstraint<? extends ConstraintFacade<B>>> listHl() {
		Collection<FacetConstraint> lowLevel = list();

		CollectionFromConverter<HLFacetConstraint<? extends ConstraintFacade<B>>, FacetConstraint> result = new CollectionFromConverter<>(lowLevel, Converter.from(
			hl -> hl.state(),
			ll -> new HLFacetConstraintImpl<>(this, parent, ll)
		));
	
		return result;
	}


}
