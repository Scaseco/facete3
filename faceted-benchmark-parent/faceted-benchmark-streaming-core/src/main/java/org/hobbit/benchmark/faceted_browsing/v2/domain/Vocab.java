package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Vocab {
	public static final Property root = property("root");
	public static final Property parent = property("parent");
	public static final Property alias = property("alias");

	public static final Property expr = property("alias");
	public static final Property constraint = property("constraint");
	public static final Property enabled = property("enabled");

	public static Property property(String localName) {
		return ResourceFactory.createProperty("http://aksw.org/adhoc/ontology/" + localName);
	}
}
