package org.hobbit.benchmark.faceted_browsing.v2.main;

public interface ModelCreation<T> {
	T getModel() throws Exception;
	//Flowable<Triple> execTriples();
	//RDFConnection toTempStore();
	//ModelFile cacheToFile();
	ModelCreation<T> cache(boolean onOrOff);
}
