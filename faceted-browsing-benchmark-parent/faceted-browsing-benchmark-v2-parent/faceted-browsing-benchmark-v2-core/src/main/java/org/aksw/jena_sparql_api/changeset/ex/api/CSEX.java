package org.aksw.jena_sparql_api.changeset.ex.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class CSEX {
	public static final String ns = "http://aksw.org/ontology/ex/changeset/schema#";

	public static Resource resource(String localName) {
		return ResourceFactory.createResource(ns + localName);
	}

	public static Property property(String localName) {
		return ResourceFactory.createProperty(ns + localName);
	}

	public static final Resource ChangeSetGroup = resource("ChangeSetGroup");
	public static final Property precedingChangeSetGroup = property("precedingChangeSetGroup");
}
