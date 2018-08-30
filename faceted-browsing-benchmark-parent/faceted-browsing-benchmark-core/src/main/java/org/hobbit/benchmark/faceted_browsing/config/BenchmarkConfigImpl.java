package org.hobbit.benchmark.faceted_browsing.config;

import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class BenchmarkConfigImpl
	extends ResourceImpl
	implements BenchmarkConfig
{
	public BenchmarkConfigImpl(Node n, EnhGraph g) {
		super(n, g);
	}

	@Override
	public String getBenchmarkControllerImageName() {
		return ResourceUtils.getLiteralPropertyValue(this, BenchmarkConfigVocab.benchmarkController, String.class);
	}

	@Override
	public String getDataGeneratorImageName() {
		return ResourceUtils.getLiteralPropertyValue(this, BenchmarkConfigVocab.dataGenerator, String.class);
	}

	@Override
	public String getTaskGeneratorImageName() {
		return ResourceUtils.getLiteralPropertyValue(this, BenchmarkConfigVocab.taskGenerator, String.class);
	}

	@Override
	public String getEvaluationModuleImageName() {
		return ResourceUtils.getLiteralPropertyValue(this, BenchmarkConfigVocab.evaluationModule, String.class);
	}

	@Override
	public String toString() {
		return "BenchmarkConfigImpl [this=" + super.toString() + ", getBenchmarkControllerImageName()=" + getBenchmarkControllerImageName()
				+ ", getDataGeneratorImageName()=" + getDataGeneratorImageName() + ", getTaskGeneratorImageName()="
				+ getTaskGeneratorImageName() + ", getEvaluationModuleImageName()=" + getEvaluationModuleImageName()
				+ "]";
	}	
	
}
