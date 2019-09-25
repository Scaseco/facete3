package org.hobbit.benchmark.faceted_browsing.config;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class BenchmarkConfigVocab {
	public static final String ns = "http://project-hobbit.eu/ontology/";

	public static Resource resource(String uriref) { return ResourceFactory.createResource(ns + uriref); }
	public static Property property(String uriref) { return ResourceFactory.createProperty(ns + uriref); }
	
	public static final Property benchmarkController = property("benchmarkController");
	public static final Property dataGenerator = property("dataGenerator");
	public static final Property taskGenerator = property("taskGenerator");
	public static final Property evaluationModule = property("evaluationModule");
//	public static final Property benchmarkController = property("benchmarkController");
//	public static final Property benchmarkController = property("benchmarkController");
}
