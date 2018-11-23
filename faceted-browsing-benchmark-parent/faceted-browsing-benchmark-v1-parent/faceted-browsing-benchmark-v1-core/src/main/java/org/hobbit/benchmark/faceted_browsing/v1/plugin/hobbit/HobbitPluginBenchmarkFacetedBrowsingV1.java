package org.hobbit.benchmark.faceted_browsing.v1.plugin.hobbit;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.hobbit.benchmark.faceted_browsing.config.BenchmarkConfig;
import org.hobbit.benchmark.faceted_browsing.v1.config.ConfigVirtualDockerServiceFactoryV1;
import org.hobbit.benchmark.faceted_browsing.v1.config.FacetedBrowsingBenchmarkV1Constants;
import org.hobbit.sdk.docker.registry.DockerServiceRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HobbitPluginBenchmarkFacetedBrowsingV1
	implements JenaSubsystemLifecycle
{
	private static final Logger logger = LoggerFactory.getLogger(HobbitPluginBenchmarkFacetedBrowsingV1.class);
	
	public void start() {
		init();
	}
	
	@Override
	public void stop() {
	}

	@Override
	public int level() {
		// Make sure to init after registration of {@link JenaPluginHobbitSdk}
		return 15000;
	}
	
	public static void init() {
		logger.info("Loading hobbit plugin: " + HobbitPluginBenchmarkFacetedBrowsingV1.class);

		// If we reference FacetedBrowsingBenchmarkV1Constants.config directly, we create a cyclic dependency:
		// referencing the object triggers init of jena, jena loads the plugins, this plugin accesses the object which is still null...

		//BenchmarkConfig config = FacetedBrowsingBenchmarkV1Constants.config;
		BenchmarkConfig config = RDFDataMgr.loadModel("faceted-browsing-benchmark-v1-config.ttl").getResource("http://project-hobbit.eu/resource/faceted-browsing-benchmark-v1-config").as(BenchmarkConfig.class);	
		
		DockerServiceRegistryImpl.registerSpringApplications(
				DockerServiceRegistryImpl.get(),
				ConfigVirtualDockerServiceFactoryV1.getDockerServiceFactoryOverrides(config));
	}	
}

