package org.aksw.facete.v3.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

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
		BgpNode result = Optional.ofNullable(path.parent()).map(BgpMultiNode::parent).orElse(null);
		return result;
		//return path.parent();
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
		String result = node == null ? null : node.getURI();
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

		 	boolean isBgpNode = state.hasProperty(RDF.type, Vocab.BgpNode);
		 	
		 	//boolean isFacetNode = state.hasProperty(Vocab.parent) || state.getModel().contains(null, Vocab.root, state);
		 	//result = isFacetNode ? new FacetNodeImpl(query, state) : null;
		 	result = isBgpNode ? state : null;
	 	}

	 	return result;
	}

	public static <P> Map<Node, P> getPathsMentioned(Expr expr, Function<? super Node, ? extends P> tryMapPath) {
		Map<Node, P> result = Streams.stream(Traverser.forTree(ExprUtils::getSubExprs).depthFirstPreOrder(expr).iterator())
			.filter(Expr::isConstant)
			.map(org.apache.jena.sparql.util.ExprUtils::eval)
			.map(NodeValue::asNode)
			.map(node -> Maps.immutableEntry(node, tryMapPath.apply(node)))
			.filter(e -> e.getValue() != null)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> u));

		return result;
	}

}
