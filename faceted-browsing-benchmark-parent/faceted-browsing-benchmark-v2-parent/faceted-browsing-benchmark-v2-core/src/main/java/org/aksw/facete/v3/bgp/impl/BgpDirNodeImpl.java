package org.aksw.facete.v3.bgp.impl;

import java.util.Map;

import org.aksw.facete.v3.bgp.api.BgpDirNode;
import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class BgpDirNodeImpl
	implements BgpDirNode
{
	protected BgpNode node;
	protected boolean isFwd;
	
	public BgpDirNodeImpl(BgpNode node, boolean isFwd) {
		this.node = node;
		this.isFwd = isFwd;
	}
	
	@Override
	public boolean isFwd() {
		return isFwd;
	}
	
	@Override
	public BgpMultiNode via(String propertyIRI) {
		return via(ResourceFactory.createProperty(propertyIRI));
	}

	@Override
	public BgpMultiNode via(Node node) {
		return via(node.getURI());
	}

	@Override
	public BgpMultiNode via(Property property) {
		Map<Resource, BgpMultiNode> map = isFwd ? node.fwdMultiNodes() : node.bwdMultiNodes() ;
		
		BgpMultiNode result = node.bwdMultiNodes().get(property);
		if(result == null) {
			result = node.getModel().createResource().as(BgpMultiNode.class);
			map.put(property, result);
		}
		
		return result;
	}
}
