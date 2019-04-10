package org.hobbit.core.component;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class BenchmarkVocab {
	public static final String ns = "http://www.example.org/";

	public static class Strs {
	    public static final String taskPayload = ns + "taskPayload";
	    
	    public static final String expectedResult = ns + "expectedResult";
	    public static final String expectedResultSize = ns + "expectedResultSize";

	    public static final String actualResult = ns + "actualResult";
	    public static final String actualResultSize = ns + "actualResultSize";

	}
	
    public static final Property taskPayload = ResourceFactory.createProperty(Strs.taskPayload);
    public static final Property expectedResultSize = ResourceFactory.createProperty(Strs.expectedResultSize);
    public static final Property expectedResult = ResourceFactory.createProperty(Strs.expectedResult);
    public static final Property actualResult = ResourceFactory.createProperty(Strs.actualResult);
    public static final Property actualResultSize = ResourceFactory.createProperty(Strs.actualResultSize);
}
