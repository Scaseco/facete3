package org.hobbit.benchmark.faceted_browsing.v1;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.benchmark.faceted_browsing.main.HobbitBenchmarkUtils;
import org.hobbit.benchmark.faceted_browsing.v1.config.ConfigVirtualDockerServiceFactoryV1;
import org.hobbit.benchmark.faceted_browsing.v1.config.FacetedBrowsingBenchmarkV1Constants;
import org.junit.Test;

public class TestFacetedBrowsingBenchmarkV1 {
	@Test
	public void testFacetedBrowsingBenchmarkBenchmarkV1() throws MalformedURLException, IOException {
		HobbitBenchmarkUtils.testBenchmarkTwoAppContexts(
				ModelFactory.createDefaultModel(),
				FacetedBrowsingBenchmarkV1Constants.config.getBenchmarkControllerImageName(),
				"git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/system-adapter-mocha-jena-in-memory",				
				ConfigVirtualDockerServiceFactoryV1.class);	
	}	
}
