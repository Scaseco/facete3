package org.hobbit.benchmark.faceted_browsing.v2.domain;

import java.util.Set;

import org.apache.jena.sparql.expr.Expr;

public class FacetedQuery {
	protected Dimension root;
	protected Set<Expr> exprs;
	
	/**
	 * The focus dimension determines the set of resources for which to compute the facet-value
	 * counts
	 */
	protected Dimension focus;
	

}
