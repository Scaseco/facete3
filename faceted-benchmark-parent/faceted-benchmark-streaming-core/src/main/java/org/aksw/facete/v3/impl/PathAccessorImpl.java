package org.aksw.facete.v3.impl;

import java.util.Objects;

import org.aksw.facete.v3.api.FacetNode;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class PathAccessorImpl
	implements PathAccessor<FacetNode>
{
	protected FacetedQueryResource query;
	
	public PathAccessorImpl(FacetedQueryResource query) {
		this.query = query;
	}
	
	@Override
	public Class<FacetNode> getPathClass() {
		return FacetNode.class;
	}

	@Override
	public FacetNode getParent(FacetNode path) {
		return path.parent();
	}

	@Override
	public BinaryRelation getReachingRelation(FacetNode path) {
		return path.getReachingRelation();
	}

	@Override
	public boolean isReverse(FacetNode path) {
		BinaryRelation br = getReachingRelation(path);
		Triple t = Objects.requireNonNull(ElementUtils.extractTriple(br.getElement()));

		
		boolean result = !br.getSourceVar().equals(t.getSubject());
		return result;
	}

	@Override
	public String getPredicate(FacetNode path) {
		BinaryRelation br = getReachingRelation(path);
		Triple t = ElementUtils.extractTriple(br.getElement());

		Node node = t == null ? null : t.getPredicate();
		String result = node.getURI();
		return result;
	}

	@Override
	public Var getAlias(FacetNode path) {
		return path.alias();
	}

//	@Override
//	public FacetNode tryMapToPath(Expr expr) {
//		FacetNode result = null;
//
//		if(expr.isConstant()) {
//		 	NodeValue nv = ExprUtils.eval(expr);
//		 	Node node = nv.asNode();
//		
//		 	if(node.isBlank()) {
//			
//			 	Model model = query.modelRoot().getModel();
//			 	
//			 	//ModelUtils.convertGraphNodeToRDFNode(node, model);
//			 	Resource state = model.wrapAsResource(node);
//			 	
//			 	boolean isFacetNode = state.hasProperty(Vocab.parent) || state.getModel().contains(null, Vocab.root, state);
//			 	result = isFacetNode ? new FacetNodeImpl(query, state) : null;
//		 	}
//		}
//		return result;
//	}

	@Override
	public FacetNode tryMapToPath(Node node) {
		FacetNode result = null;
	
	 	if(node.isBlank()) {
		
		 	Model model = query.modelRoot().getModel();
		 	
		 	//ModelUtils.convertGraphNodeToRDFNode(node, model);
		 	Resource state = model.wrapAsResource(node);
		 	
		 	boolean isFacetNode = state.hasProperty(Vocab.parent) || state.getModel().contains(null, Vocab.root, state);
		 	result = isFacetNode ? new FacetNodeImpl(query, state) : null;
	 	}

	 	return result;
	}

}
