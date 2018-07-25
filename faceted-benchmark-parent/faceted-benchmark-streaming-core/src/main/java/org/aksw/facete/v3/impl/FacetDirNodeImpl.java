package org.aksw.facete.v3.impl;

import java.util.Collection;

import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.ModelUtils;

public class FacetDirNodeImpl
	implements FacetDirNode
{
//	abstract boolean isFwd();
//
//	@Override
//	public FacetDirNode getParent() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	protected FacetNodeResource parent;
	protected boolean isFwd;
	protected Var alias;
	
	public FacetDirNodeImpl(FacetNodeResource parent, boolean isFwd) {
		this.parent = parent;
		this.isFwd = isFwd;
	}
	
	@Override
	public FacetNode parent() {
		return parent;
	}

	@Override
	public FacetMultiNode via(Node node) {
		return via(node.getURI());
	}

	@Override
	public FacetMultiNode via(String propertyIRI) {
		return via(ResourceFactory.createProperty(propertyIRI));
	}
	
	@Override
	public FacetMultiNode via(Property property) {
		return new FacetMultiNodeImpl(parent, property);
		
	}
	
	@Override
	public Collection<FacetCount> getFacetsAndCounts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getFacetValuesAndCounts() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public FacetedQuery getQuery() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
