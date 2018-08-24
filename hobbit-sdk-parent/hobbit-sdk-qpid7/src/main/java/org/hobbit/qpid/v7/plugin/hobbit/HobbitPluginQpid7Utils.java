package org.hobbit.qpid.v7.plugin.hobbit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.hobbit.core.component.ServiceNoOp;
import org.hobbit.core.service.docker.DockerServiceBuilderJsonDelegate;
import org.hobbit.core.service.docker.DockerServiceFactorySpringApplicationBuilder;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.hobbit.sdk.docker.registry.DockerServiceRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class HobbitPluginQpid7Utils {
	
	private static final Logger logger = LoggerFactory.getLogger(HobbitPluginQpid7Utils.class);

	
	public static void init() {
		logger.info("Loading hobbit plugin: " + HobbitPluginQpid7.class);
		
		Map<String, Supplier<SpringApplicationBuilder>> imageToAppBuilderSupplier = new LinkedHashMap<>();
				
		String imageName = "git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/hobbit-sdk-qpid7";
		imageToAppBuilderSupplier.put(imageName,
		() -> new SpringApplicationBuilder()
					.sources(ConfigQpidBroker.class, ServiceNoOp.class)
					//.sources(ApplicationRunnerQpidBroker.class)
					.bannerMode(Banner.Mode.OFF));
	
		// TODO Get rid of the registry for SpringApplicationBuilders which results in having to register images twice
		DockerServiceRegistryImpl.get().getServiceFactoryMap()
			.put(imageName,
					() -> DockerServiceBuilderJsonDelegate.create(new DockerServiceFactorySpringApplicationBuilder(imageToAppBuilderSupplier)::create).setImageName(imageName)
			);
	}
}
