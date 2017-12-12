package org.hobbit.benchmark.faceted_browsing.main;


import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hobbit.benchmark.faceted_browsing.main.TestBenchmark.ConfigDockerServiceFactory;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;


/**
 * Command line runner for launching a (virtual) docker service
 * 
 * @author raven Dec 12, 2017
 *
 */
public class MainDockerServiceLauncher {
	
	@Inject
	protected Environment env;
	
	public static class Config {
		ApplicationRunner appRunner(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
			return args -> {
				List<String> nonOptionArgs = args.getNonOptionArgs();
				if(nonOptionArgs.size() != 1) {
					throw new RuntimeException("Exactly 1 argument expected which is the name of a (virtual) docker image to launch");
				}
				
				String imageName = nonOptionArgs.get(0);

				Map<String, String> localEnv = null;
				DockerService dockerService = dockerServiceBuilderFactory.get()
					.setImageName(imageName)
					.setLocalEnvironment(localEnv)
					.get();
				
				dockerService.startAsync().awaitTerminated();
			};
		}
	}
	
	
	public static void main(String[] args) {
		try(ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
			.sources(ConfigDockerServiceFactory.class)
				.child(Config.class)
				.run()) {			
		}
	}
}
