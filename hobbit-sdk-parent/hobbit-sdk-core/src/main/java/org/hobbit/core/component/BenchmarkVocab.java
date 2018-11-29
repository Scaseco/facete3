package org.hobbit.core.component;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class BenchmarkVocab {
    public static final Property taskPayload = ResourceFactory.createProperty("http://example.org/taskPayload");
    public static final Property expectedResult = ResourceFactory.createProperty("http://example.org/expectedResult");
    public static final Property actualResult = ResourceFactory.createProperty("http://example.org/actualResult");
}
