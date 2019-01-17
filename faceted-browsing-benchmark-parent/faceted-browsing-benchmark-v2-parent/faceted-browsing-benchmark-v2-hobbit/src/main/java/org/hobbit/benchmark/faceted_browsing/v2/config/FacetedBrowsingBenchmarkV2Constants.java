package org.hobbit.benchmark.faceted_browsing.v2.config;

import org.apache.jena.riot.RDFDataMgr;
import org.hobbit.benchmark.faceted_browsing.config.BenchmarkConfig;

public class FacetedBrowsingBenchmarkV2Constants {
	public static final BenchmarkConfig config = RDFDataMgr.loadModel("faceted-browsing-benchmark-v2-config.ttl").getResource("http://project-hobbit.eu/resource/faceted-browsing-benchmark-v2-config").as(BenchmarkConfig.class);
}
