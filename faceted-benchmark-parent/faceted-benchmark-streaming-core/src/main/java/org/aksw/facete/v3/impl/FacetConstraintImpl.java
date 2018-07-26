package org.aksw.facete.v3.impl;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class FacetConstraintImpl
	extends ResourceBase
	implements FacetConstraint
{
	public FacetConstraintImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public boolean enabled() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.property("disabled"), Boolean.class).orElse(true);
	}

	@Override
	public FacetConstraint enabled(boolean onOrOff) {

		if(onOrOff) {
			ResourceUtils.setLiteralProperty(this, Vocab.property("disabled"), null);			
		} else {
			ResourceUtils.setLiteralProperty(this, Vocab.property("disabled"), onOrOff);
		}
		
		return this;
	}

	@Override
	public Expr expr() {
		String str = ResourceUtils.getLiteralPropertyValue(this, Vocab.property("expr"), String.class).orElse(null);
		Expr result = ExprUtils.parse(str);
		return result;
	}

	
	@Override
	public FacetConstraint expr(Expr expr) {
		String str = expr.toString();		
		ResourceUtils.setLiteralProperty(this, Vocab.property("expr"), str);

		return this;
	}
	
}
