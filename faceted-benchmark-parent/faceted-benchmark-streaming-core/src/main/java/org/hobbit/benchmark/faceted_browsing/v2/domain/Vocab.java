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

	public static final Property value = property("value");
	public static final Property facetCount = property("facetCount");
	public static final Property facetValueCount = property("facetValueCount");

	
	public static final Property totalValueCount = property("totalValueCount");
	public static final Property distinctValueCount = property("distinctValueCount");
	public static final Property min = property("min");
	public static final Property max = property("max");
	public static final Property groupKey = property("groupKey");

	public static Property property(String localName) {
		return ResourceFactory.createProperty("http://aksw.org/adhoc/ontology/" + localName);
	}
}
