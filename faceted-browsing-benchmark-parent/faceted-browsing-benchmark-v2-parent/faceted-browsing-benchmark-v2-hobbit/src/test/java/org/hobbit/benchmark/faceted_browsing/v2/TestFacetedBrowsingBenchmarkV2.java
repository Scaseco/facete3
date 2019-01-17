package org.hobbit.benchmark.faceted_browsing.v2;

import java.io.IOException;
import java.net.MalformedURLException;

import org.hobbit.benchmark.faceted_browsing.main.HobbitBenchmarkUtils;
import org.hobbit.benchmark.faceted_browsing.v2.config.ConfigVirtualDockerServiceFactoryV2;
import org.hobbit.benchmark.faceted_browsing.v2.config.FacetedBrowsingBenchmarkV2Constants;
import org.junit.Test;

public class TestFacetedBrowsingBenchmarkV2 {
	@Test
	public void testFacetedBrowsingBenchmarkBenchmarkV2() throws MalformedURLException, IOException {
		HobbitBenchmarkUtils.testBenchmarkTwoAppContexts(
				FacetedBrowsingBenchmarkV2Constants.config.getBenchmarkControllerImageName(),
				"git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/system-adapter-mocha-jena-in-memory",
				ConfigVirtualDockerServiceFactoryV2.class);	
	}	
}
