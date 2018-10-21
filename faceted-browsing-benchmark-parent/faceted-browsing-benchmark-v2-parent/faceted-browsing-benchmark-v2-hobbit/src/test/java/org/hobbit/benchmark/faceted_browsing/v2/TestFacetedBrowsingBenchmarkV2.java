package org.hobbit.benchmark.faceted_browsing.v2;

import java.io.IOException;
import java.net.MalformedURLException;

import org.hobbit.benchmark.faceted_browsing.main.HobbitBenchmarkUtils;
import org.hobbit.benchmark.faceted_browsing.v2.config.ConfigVirtualDockerServiceFactoryV2;
import org.junit.Test;

public class TestFacetedBrowsingBenchmarkV2 {
	@Test
	public void testFacetedBrowsingBenchmarkBenchmarkV1() throws MalformedURLException, IOException {
		HobbitBenchmarkUtils.testBenchmarkTwoAppContexts(ConfigVirtualDockerServiceFactoryV2.class);	
	}	
}
