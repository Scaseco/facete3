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
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.RangeSpec;

import com.google.common.base.Converter;
import com.google.common.collect.BoundType;
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


	//@Override

	public HLFacetConstraint<? extends ConstraintFacade<B>> createConstraint(Expr expr) {
		Resource modelRoot = parent.query().modelRoot();
		//Collection<FacetConstraint> set = list();
		
		FacetConstraint c = modelRoot.getModel().createResource().as(FacetConstraint.class);
		c.expr(expr);
		
		
		HLFacetConstraint<ConstraintFacade<B>> result = new HLFacetConstraintImpl<>(this, parent, c);

		//c.expr(new E_Bound(NodeValue.makeNode(parent.state().asNode())));
		
		return result;
	}
//	
//	@Override
//	public ConstraintFacadeImpl<B> addExpr(Expr expr) {
//		
//		set.add(c);
//
//		return this;
//	}
	
	@Override
	public HLFacetConstraint<? extends ConstraintFacade<B>> exists() {
		Expr expr = new E_Bound(thisAsExpr());
		HLFacetConstraint<? extends ConstraintFacade<B>> result = getOrCreateConstraint(expr);
		return result;
	}

	
	@Override
	public Expr thisAsExpr() {
		Expr result = NodeValue.makeNode(parent.state().asNode());
		return result;
	}
	
	/**
	 * At present we allow a null argument to denote absent values. 
	 * 
	 */
	@Override
	public HLFacetConstraint<? extends ConstraintFacade<B>> eq(Node node) {
		HLFacetConstraint<? extends ConstraintFacade<B>> result;
		
		if(node == null || N_ABSENT.equals(node)) {
			result = absent();
		} else {		
			Expr expr = new E_Equals(thisAsExpr(), NodeValue.makeNode(node));
			result = getOrCreateConstraint(expr);
		}

		return result;

//		
//		// Check if constraint with that expression already exists
//		List<FacetConstraint> existingEqualConstraints = list().stream().filter(c -> Objects.equals(c.expr(), expr)).collect(Collectors.toList());
//		
//		FacetConstraint c;
//		if(existingEqualConstraints.isEmpty()) {
//		
//			
//			Resource modelRoot = parent.query().modelRoot();
//	
//			//Collection<FacetConstraint> set = list(); //new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);
//			
//			
//			c = modelRoot.getModel().createResource().as(FacetConstraint.class);
//			c.expr(expr);
//		} else {
//			c = existingEqualConstraints.iterator().next();
//		}
//		// TODO Using blank nodes for exprs was a bad idea...
//		// We should just allocate var names
//		
//		//c.expr(new E_Equals(new ExprVar((Var)parent.state().asNode()), NodeValue.makeNode(node)));
//		
//		//set.add(c);
//		
//		HLFacetConstraint<ConstraintFacade<B>> result = new HLFacetConstraintImpl<>(this, parent, c);
//
//		return result;
	}

	@Override
	public HLFacetConstraint<ConstraintFacade<B>> gt(Node node) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public HLFacetConstraint<ConstraintFacade<B>> neq(Node node) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public B end() {
		return parent;
	}


	@Override
	public HLFacetConstraint<? extends ConstraintFacade<B>> nodeRange(Range<NodeHolder> range) {
		Expr expr = RangeUtils.createExpr(parent.state().asNode(), range);
		HLFacetConstraint<? extends ConstraintFacade<B>> result = getOrCreateConstraint(expr);
		return result;
//		
//		Resource modelRoot = parent.query().modelRoot();
//
//		Set<FacetConstraint> set = new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);
//		
//		FacetConstraint c = modelRoot.getModel().createResource().as(FacetConstraint.class);
//		Expr expr = RangeUtils.createExpr(parent.state().asNode(), range);
//		c.expr(expr);
//		// TODO Using blank nodes for exprs was a bad idea...
//		// We should just allocate var names
//		
//		//c.expr(new E_Equals(new ExprVar((Var)parent.state().asNode()), NodeValue.makeNode(node)));
//		set.add(c);
//		
//		return this;
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

	
	public static final Node N_ABSENT = NodeFactory.createURI("http://special.absent/none");
	public static final NodeValue NV_ABSENT = NodeValue.makeNode(N_ABSENT);

	@Override
	public HLFacetConstraint<? extends ConstraintFacade<B>> absent() {
		Expr expr = new E_Equals(thisAsExpr(), NV_ABSENT);
		HLFacetConstraint<? extends ConstraintFacade<B>> result = getOrCreateConstraint(expr);
		return result;
	}

	
	/**
	 * Create a range from a range from Java objects (such as Integers) via Jena's Type mapper
	 * 
	 * @param range
	 * @return
	 */
	public static Range<NodeHolder> toNodeRange(Range<?> range) {
		TypeMapper tm = TypeMapper.getInstance();
		
		Node lowerNode = null;
		Node upperNode = null;
		
		BoundType lowerBoundType = null;
		BoundType upperBoundType = null;
		
		if(range.hasLowerBound()) {
			lowerBoundType = range.lowerBoundType();

			Object lb = range.lowerEndpoint();
			Class<?> lbClass = lb.getClass();

			RDFDatatype dtype = tm.getTypeByClass(lbClass);
			if(dtype == null) {
				throw new IllegalArgumentException("No type mapper entry for " + lbClass);
			}
			
			lowerNode = NodeFactory.createLiteralByValue(lb, dtype);
		}
		
		if(range.hasUpperBound()) {
			upperBoundType = range.upperBoundType();

			Object ub = range.upperEndpoint();
			Class<?> ubClass = ub.getClass();

			RDFDatatype dtype = tm.getTypeByClass(ubClass);
			if(dtype == null) {
				throw new IllegalArgumentException("No type mapper entry for " + ubClass);
			}

			upperNode = NodeFactory.createLiteralByValue(ub, dtype);
		}
		
		NodeHolder lowerNh = lowerNode == null ? null : new NodeHolder(lowerNode);
		NodeHolder upperNh = upperNode == null ? null : new NodeHolder(upperNode);
		
		Range<NodeHolder> result = RangeSpec.createRange(lowerNh, lowerBoundType, upperNh, upperBoundType);	
		return result;
	}
	
	@Override
	public HLFacetConstraint<? extends ConstraintFacade<B>> range(Range<?> range) {
		Range<NodeHolder> nodeRange = toNodeRange(range);
	
		HLFacetConstraint<? extends ConstraintFacade<B>> result = nodeRange(nodeRange);
		return result;
	}


}
