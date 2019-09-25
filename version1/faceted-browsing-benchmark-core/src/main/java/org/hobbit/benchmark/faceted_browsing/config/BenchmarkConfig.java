package org.hobbit.benchmark.faceted_browsing.config;

import org.apache.jena.rdf.model.Resource;

public interface BenchmarkConfig
	extends Resource
{
	String getBenchmarkControllerImageName();
	String getDataGeneratorImageName();
	String getTaskGeneratorImageName();
	String getEvaluationModuleImageName();
}
