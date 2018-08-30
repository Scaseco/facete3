package org.aksw.facete.v3.impl;

import java.util.Objects;

import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class PathAccessorImpl
	implements PathAccessor<BgpNode>
{
	protected BgpNode query;
	
	public PathAccessorImpl(BgpNode query) {
		this.query = query;
	}
	
	@Override
	public Class<BgpNode> getPathClass() {
		return BgpNode.class;
	}

	@Override
	public BgpNode getParent(BgpNode path) {
		return path.parent();
	}

	@Override
	public BinaryRelation getReachingRelation(BgpNode path) {
		BinaryRelation result = BgpNode.getReachingRelation(path);
		return result;
		//		return path.getReachingRelation();
	}

	@Override
	public boolean isReverse(BgpNode path) {
		BinaryRelation br = getReachingRelation(path);
		Triple t = Objects.requireNonNull(ElementUtils.extractTriple(br.getElement()));

		
		boolean result = !br.getSourceVar().equals(t.getSubject());
		return result;
	}

	@Override
	public String getPredicate(BgpNode path) {
		BinaryRelation br = getReachingRelation(path);
		Triple t = ElementUtils.extractTriple(br.getElement());

		Node node = t == null ? null : t.getPredicate();
		String result = node.getURI();
		return result;
	}

	@Override
	public Var getAlias(BgpNode path) {
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
	public BgpNode tryMapToPath(Node node) {
		//FacetNode result = null;
		BgpNode result = null;
		
	 	if(node.isBlank()) {
	 		
		 	//Model model = query.modelRoot().getModel();
		 	Model model = query.getModel();
	 		
		 	//ModelUtils.convertGraphNodeToRDFNode(node, model);
		 	BgpNode state = model.wrapAsResource(node).as(BgpNode.class);

		 	
		 	boolean isFacetNode = state.hasProperty(Vocab.parent) || state.getModel().contains(null, Vocab.root, state);
		 	//result = isFacetNode ? new FacetNodeImpl(query, state) : null;
		 	result = isFacetNode ? state : null;
	 	}

	 	return result;
	}

}
