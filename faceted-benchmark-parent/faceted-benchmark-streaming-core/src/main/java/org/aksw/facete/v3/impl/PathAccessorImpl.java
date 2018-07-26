package org.aksw.facete.v3.impl;

import java.util.Objects;

import org.aksw.facete.v3.api.FacetNode;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;

public class PathAccessorImpl
	implements PathAccessor<FacetNode>
{
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

}
