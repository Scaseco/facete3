package org.aksw.jena_sparql_api.changeset.ex.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class CSX {
	public static final String ns = "http://aksw.org/ontology/ex/changeset/schema#";

	public static Resource resource(String localName) {
		return ResourceFactory.createResource(ns + localName);
	}

	public static Property property(String localName) {
		return ResourceFactory.createProperty(ns + localName);
	}

	public static final Resource ChangeSetGroup = resource("ChangeSetGroup");
	public static final Property precedingChangeSetGroup = property("precedingChangeSetGroup");

	// Pointer into a chain of changeset groups
    public static final Resource redoPointer = resource("redoPointer"); //ResourceFactory.createResource("http://aksw.org/ontology/ex/changeset/redoPointer");
    public static final Property value = property("value");

    public static final Property latestChangeSet = property("latestChangeSet"); //ResourceFactory.createResource("http://aksw.org/ontology/ex/changeset/redoPointer");
    public static final Property isLatestChangeSetUndone = property("isLatestChangeSetUndone");
    
    public static final Property latestChangeSetGroup = property("latestChangeSetGroup"); //ResourceFactory.createResource("http://aksw.org/ontology/ex/changeset/redoPointer");
    public static final Property isLatestChangeSetGroupUndone = property("isLatestChangeSetGroupUndone");
    
}
