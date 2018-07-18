package org.hobbit.faceted_browsing.action;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class MapStateImpl
	extends ResourceImpl
	implements MapState
{
	
	public MapStateImpl(Node n, EnhGraph m) {
		super(n, m);
	}
	

}

