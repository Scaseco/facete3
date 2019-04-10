package org.hobbit.benchmark.faceted_browsing.v2;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.main.HobbitBenchmarkUtils;
import org.hobbit.benchmark.faceted_browsing.v2.config.ConfigVirtualDockerServiceFactoryV2;
import org.hobbit.benchmark.faceted_browsing.v2.config.FacetedBrowsingBenchmarkV2Constants;
import org.junit.Test;

public class TestFacetedBrowsingBenchmarkV2 {
	@Test
	public void testFacetedBrowsingBenchmarkBenchmarkV2() throws MalformedURLException, IOException {
		Model configModel = ModelFactory.createDefaultModel();
		configModel.createResource("http://www.example.org/testBenchmarkConfig")
			.addLiteral(FacetedBrowsingVocab.preconfData, "path-data-simple.ttl")
			.addLiteral(FacetedBrowsingVocab.preconfTasks, "test-tasks.trig");

		
		HobbitBenchmarkUtils.testBenchmarkTwoAppContexts(
				configModel,
				FacetedBrowsingBenchmarkV2Constants.config.getBenchmarkControllerImageName(),
				"git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/system-adapter-mocha-jena-in-memory",
				ConfigVirtualDockerServiceFactoryV2.class);	
	}	
}
