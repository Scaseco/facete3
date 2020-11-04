package org.aksw.facete3.app.vaadin.plugin.view;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Bibframe {
    // TODO: move this part to config and init it there  !!
    public static final Property title = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/title");
    public static final Property identifiedBy = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/identifiedBy");
    public static final Property summary = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/summary");
    // public static final Property creator = ResourceFactory.createProperty("http://purl.org/dc/terms/creator");

}