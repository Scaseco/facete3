package org.hobbit.benchmark.faceted_browsing.v1;

import java.io.IOException;
import java.net.MalformedURLException;

import org.hobbit.benchmark.faceted_browsing.main.HobbitBenchmarkUtils;
import org.hobbit.benchmark.faceted_browsing.v1.config.ConfigVirtualDockerServiceFactoryV1;
import org.junit.Test;

public class TestFacetedBrowsingBenchmarkV1 {
	@Test
	public void testFacetedBrowsingBenchmarkBenchmarkV1() throws MalformedURLException, IOException {
		HobbitBenchmarkUtils.testBenchmarkTwoAppContexts(ConfigVirtualDockerServiceFactoryV1.class);	
	}	
}
