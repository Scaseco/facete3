package org.hobbit.benchmark.faceted_browsing.v2.plugin.hobbit;

import java.util.Collections;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.hobbit.benchmark.faceted_browsing.config.BenchmarkConfig;
import org.hobbit.benchmark.faceted_browsing.v2.config.ConfigVirtualDockerServiceFactoryV2;
import org.hobbit.core.service.docker.impl.spring_boot.util.DockerServiceRegistrySpringBootUtils;
import org.hobbit.sdk.docker.registry.DockerServiceRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HobbitPluginBenchmarkFacetedBrowsingV2
	implements JenaSubsystemLifecycle
{
	private static final Logger logger = LoggerFactory.getLogger(HobbitPluginBenchmarkFacetedBrowsingV2.class);
	
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
		logger.info("Loading hobbit plugin: " + HobbitPluginBenchmarkFacetedBrowsingV2.class);

		// If we reference FacetedBrowsingBenchmarkV1Constants.config directly, we create a cyclic dependency:
		// referencing the object triggers init of jena, jena loads the plugins, this plugin accesses the object which is still null...

		//BenchmarkConfig config = FacetedBrowsingBenchmarkV1Constants.config;
		BenchmarkConfig config = RDFDataMgr.loadModel("faceted-browsing-benchmark-v2-config.ttl").getResource("http://project-hobbit.eu/resource/faceted-browsing-benchmark-v2-config").as(BenchmarkConfig.class);	
		
		DockerServiceRegistrySpringBootUtils.registerSpringApplications(
				DockerServiceRegistryImpl.get(),
				ConfigVirtualDockerServiceFactoryV2.getDockerServiceFactoryOverrides(config, Collections.emptyMap()));
	}	
}

