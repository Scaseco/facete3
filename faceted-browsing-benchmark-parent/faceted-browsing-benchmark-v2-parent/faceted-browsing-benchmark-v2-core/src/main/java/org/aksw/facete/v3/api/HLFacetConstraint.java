package org.aksw.facete.v3.api;

import java.util.Set;

import org.apache.jena.sparql.expr.Expr;

public interface HLFacetConstraint {
	FacetConstraint state();
	Set<FacetNode> mentionedFacetNodes();

	Expr expr();
}
