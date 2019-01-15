package org.hobbit.benchmark.faceted_browsing.v2;

import java.io.IOException;
import java.io.InputStream;

import org.hobbit.benchmark.faceted_browsing.v2.main.MainFacetedBrowsingBenchmarkV2Run;
import org.junit.Assert;
import org.junit.Test;

public class TestResourceAccess {
	@Test
	public void testResourceAccessTasks() throws IOException {
    	try(InputStream in = MainFacetedBrowsingBenchmarkV2Run.openBz2InputStream("hobbit-sensor-stream-150k-events-tasks.ttl.bz2")) {
    		Assert.assertNotNull(in);
    	}
	}

	@Test
	public void testResourceAccessData() throws IOException {
    	try(InputStream in = MainFacetedBrowsingBenchmarkV2Run.openBz2InputStream("hobbit-sensor-stream-150k-events-data.trig.bz2")) {
    		Assert.assertNotNull(in);
    	}
	}

}
