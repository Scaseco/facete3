package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Vocab {
	public static final Property root = property("root");
	public static final Property alias = property("alias");
	
	public static Property property(String localName) {
		return ResourceFactory.createProperty("http://aksw.org/adhoc/ontology/" + localName);
	}
}
