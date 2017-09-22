package org.hobbit.benchmarks.faceted_browsing.components;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class FacetedBrowsingVocab {
    public static final Property scenarioId = ResourceFactory.createProperty("http://example.org/scenarioId");
    public static final Property queryId = ResourceFactory.createProperty("http://example.org/queryId");
}
