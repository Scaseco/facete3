package org.hobbit.benchmark.faceted_browsing.v1.config;

import org.apache.jena.riot.RDFDataMgr;
import org.hobbit.benchmark.faceted_browsing.config.BenchmarkConfig;

public class FacetedBrowsingBenchmarkV1Constants {
	public static final BenchmarkConfig config = RDFDataMgr.loadModel("faceted-browsing-benchmark-v1-config.ttl").getResource("http://project-hobbit.eu/resource/faceted-browsing-benchmark-v1-config").as(BenchmarkConfig.class);
}
