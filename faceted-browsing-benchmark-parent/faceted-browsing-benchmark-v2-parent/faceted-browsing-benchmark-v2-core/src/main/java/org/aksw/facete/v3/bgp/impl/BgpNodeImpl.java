package org.aksw.facete.v3.bgp.impl;

import java.util.Map;
import java.util.Optional;

import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.facete.v3.bgp.api.BgpDirNode;
import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.impl.ResourceBase;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.views.map.MapFromKeyConverter;
import org.aksw.jena_sparql_api.utils.views.map.MapFromProperty;
import org.aksw.jena_sparql_api.utils.views.map.MapFromValueConverter;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

import com.google.common.base.Converter;

public class BgpNodeImpl
	extends ResourceBase
	implements BgpNode
{
	
	public BgpNodeImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	public Map<Resource, BgpMultiNode> createMap(Property p) {
		Map<RDFNode, Resource> map = new MapFromProperty(this, p, Vocab.property);

		Map<Resource, Resource> m = new MapFromKeyConverter<>(map, Converter.from(r -> r.as(Resource.class), RDFNode::asResource));		
		Map<Resource, BgpMultiNode> result = new MapFromValueConverter<>(m, Converter.from(r -> r.as(BgpMultiNode.class), RDFNode::asResource));
		return result;	
	}
	
	@Override
	public Map<Resource, BgpMultiNode> fwdMultiNodes() {
		Map<Resource, BgpMultiNode> result = createMap(Vocab.fwd);
		return result;
	}

	@Override
	public Map<Resource, BgpMultiNode> bwdMultiNodes() {
		Map<Resource, BgpMultiNode> result = createMap(Vocab.bwd);		
		return result;
	}

	
	@Override
	public BgpDirNode fwd() {
		return new BgpDirNodeImpl(this, false);
	}

	@Override
	public BgpDirNode bwd() {
		return new BgpDirNodeImpl(this, true);
	}

	@Override
	public BgpNode as(String varName) {
		ResourceUtils.setLiteralProperty(this, Vocab.alias, varName);
		return this;
	}

//	@Override
//	public FacetNode as(Var var) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Var alias() {
		Var result = ResourceUtils.tryGetLiteralPropertyValue(this, Vocab.alias, String.class)
			.map(Var::alloc)
			.orElse(null);

		return result;
	}

	@Override
	public BgpNode root() {
		BgpNode result = TreeUtils.<BgpNode>findRoot(this, n -> Optional.ofNullable(n.parent()).map(BgpMultiNode::parent).orElse(null));
		return result;
	}

	@Override
	public BgpNode as(Var var) {
		return as(var == null ? null : var.getName());
	}

	@Override
	public BgpMultiNode parent() {
		//BgpMultiNode result = ResourceUtils.getPropertyValue(this, Vocab.parent, BgpMultiNode.class);
		
		BgpMultiNode result = ResourceUtils.getReversePropertyValue(this, Vocab.one, BgpMultiNode.class);
		return result;
	}

	@Override
	public String toString() {
		return "BgpNodeImpl [root()=" + root() + ", parent()=" + parent() + "]";
	}
//
//	@Override
//	public BgpNode parent(BgpMultiNode parent) {
//		ResourceUtils.getReverseProperty(s, p)
//		
//		Vocab.fwd
//		
//		//ResourceUtils.setProperty(this, Vocab.parent, parent);
//		
//		return this;
//	}
}
