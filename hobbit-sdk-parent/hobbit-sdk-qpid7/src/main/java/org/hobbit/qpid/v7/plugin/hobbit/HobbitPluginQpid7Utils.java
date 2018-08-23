package org.hobbit.qpid.v7.plugin.hobbit;

import java.util.function.Supplier;

import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.hobbit.sdk.docker.registry.DockerServiceRegistryImpl;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class HobbitPluginQpid7Utils {
	public static void init() {
		Supplier<SpringApplicationBuilder> appBuilderSupplier = () -> new SpringApplicationBuilder()
				.sources(ConfigQpidBroker.class)
				//.sources(ApplicationRunnerQpidBroker.class)
				.bannerMode(Banner.Mode.OFF);
		
		
		DockerServiceRegistryImpl.get().getServiceFactoryMap()
			.put("git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/hobbit-sdk-qpid7",
			null
					//() -> new Docker					
			);
	}
}
