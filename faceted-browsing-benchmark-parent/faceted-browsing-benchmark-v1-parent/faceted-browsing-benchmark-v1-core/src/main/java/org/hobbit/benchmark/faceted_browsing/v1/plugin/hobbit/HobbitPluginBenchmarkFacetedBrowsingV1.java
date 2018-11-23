package org.hobbit.benchmark.faceted_browsing.v1.plugin.hobbit;

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

	
	public static void init() {
		logger.info("Loading hobbit plugin: " + HobbitPluginBenchmarkFacetedBrowsingV1.class);
		org.hobbit.sdk.sys.JenaPluginHobbitSdk.init();

		BenchmarkConfig config = FacetedBrowsingBenchmarkV1Constants.config;
						
		DockerServiceRegistryImpl.registerSpringApplications(
				DockerServiceRegistryImpl.get(),
				ConfigVirtualDockerServiceFactoryV1.getDockerServiceFactoryOverrides(config));
	}	
}

