package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public abstract class AbstractResourceImpl
	extends ResourceImpl
{
	public AbstractResourceImpl(Node n, EnhGraph m) {
		super(n, m);
	}

}
