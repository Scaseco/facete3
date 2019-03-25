package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.apache.jena.rdf.model.Model;

public interface ModelCreation {
	Model getModel() throws Exception;
	//Flowable<Triple> execTriples();
	//RDFConnection toTempStore();
	//ModelFile cacheToFile();
	ModelCreation cache();
}
