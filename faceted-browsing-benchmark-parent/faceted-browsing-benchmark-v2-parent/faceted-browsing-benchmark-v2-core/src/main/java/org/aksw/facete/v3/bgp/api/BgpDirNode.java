package org.aksw.facete.v3.bgp.api;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;

/**
 * 
 * @author raven
 *
 */
public interface BgpDirNode {
	BgpMultiNode via(String propertyIRI);
	BgpMultiNode via(Node node);
	BgpMultiNode via(Property property);
}
