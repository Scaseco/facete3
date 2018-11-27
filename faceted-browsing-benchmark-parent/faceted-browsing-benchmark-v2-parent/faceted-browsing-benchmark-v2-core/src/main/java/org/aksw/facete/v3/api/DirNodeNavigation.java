package org.aksw.facete.v3.api;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Interface for DirNodes
 */
public interface DirNodeNavigation<M> {
	default M via(String propertyIRI) {
		return via(ResourceFactory.createProperty(propertyIRI));
	}
	default M via(Node node) {
		return via(node.getURI());
	}
	M via(Resource property);
}
