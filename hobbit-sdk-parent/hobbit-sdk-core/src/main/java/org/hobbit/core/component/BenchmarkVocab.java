package org.hobbit.core.component;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class BenchmarkVocab {
	public static final String ns = "http://example.org/";

	public static class Strs {
	    public static final String taskPayload = ns + "taskPayload";
	    public static final String expectedResult = ns + "expectedResult";
	    public static final String actualResult = ns + "actualResult";

	    public static final String expectedResultSetSize = ns + "expectedResultSetSize";
	}
	
    public static final Property taskPayload = ResourceFactory.createProperty(Strs.taskPayload);
    public static final Property expectedResultSetSize = ResourceFactory.createProperty(Strs.expectedResultSetSize);
    public static final Property expectedResult = ResourceFactory.createProperty(Strs.expectedResult);
    public static final Property actualResult = ResourceFactory.createProperty(Strs.actualResult);
}
