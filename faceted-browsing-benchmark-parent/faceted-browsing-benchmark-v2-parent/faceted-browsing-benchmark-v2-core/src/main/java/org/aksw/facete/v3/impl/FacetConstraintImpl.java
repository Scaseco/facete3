package org.aksw.facete.v3.impl;

import java.util.Optional;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.graph.NodeTransformExpr;
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
		return ResourceUtils.tryGetLiteralPropertyValue(this, Vocab.enabled, Boolean.class).orElse(true);
	}

	@Override
	public FacetConstraint enabled(boolean onOrOff) {

		if(onOrOff) {
			ResourceUtils.setLiteralProperty(this, Vocab.enabled, null);			
		} else {
			ResourceUtils.setLiteralProperty(this, Vocab.enabled, onOrOff);
		}
		
		return this;
	}

	@Override
	public Expr expr() {
		String str = ResourceUtils.tryGetLiteralPropertyValue(this, Vocab.expr, String.class).orElse(null);
		Expr result = ExprUtils.parse(str);
		
		result = result.applyNodeTransform(FacetConstraintImpl::varToBlankNode);

		return result;
	}

	public static Node varToBlankNode(Node node) {
		return Optional.of(node)
				.filter(Node::isVariable)
				.map(y -> (Var)y)
				.map(Var::getName)
				.filter(n -> n.startsWith("___"))
				.map(n -> n.substring(3).replace("_", "-"))
				.map(NodeFactory::createBlankNode)
				.orElse(node);
	}
	
	
	public static Node blankNodeToVar(Node node) {
		return !node.isBlank() ? node : Var.alloc("___" + node.getBlankNodeLabel().replace("-", "_"));
	}
	
	@Override
	public FacetConstraint expr(Expr expr) {
		//expr = expr.applyNodeTransform(FacetConstraintImpl::blankNodeToVar);
		expr = ExprTransformer.transform(new NodeTransformExpr(FacetConstraintImpl::blankNodeToVar), expr);
		
		String str = ExprUtils.fmtSPARQL(expr);
		ResourceUtils.setLiteralProperty(this, Vocab.expr, str);

		return this;
	}
	
}
