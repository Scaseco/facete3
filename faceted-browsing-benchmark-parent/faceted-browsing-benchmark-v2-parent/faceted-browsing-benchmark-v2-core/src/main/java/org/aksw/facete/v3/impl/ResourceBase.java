package org.aksw.facete.v3.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class ResourceBase
	extends ResourceImpl
{
	public ResourceBase(Node n, EnhGraph m) {
		super(n, m);
	}
}
