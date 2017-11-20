package org.hobbit.benchmark.faceted_browsing.config;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class DockerServiceFactoryUtilsSpringBoot {
	/**
	 * This function yields a DockerServiceFactory that
	 * capable of launching classes using SpringBoot's ApplicationBuilder.
	 * 
	 * 
	 * @param imageNameToClass
	 * @param baseAppBuilder
	 * @return
	 */
	public static DockerServiceFactory<?> createDockerServiceFactoryForBootstrap(Map<String, Supplier<SpringApplicationBuilder>> imageNameToApplicationBuilderSupplier) {

		DockerServiceFactory<DockerService> result = new DockerServiceFactoryGeneric<
					Supplier<SpringApplicationBuilder>,
					Entry<Map<String, String>, Supplier<SpringApplicationBuilder>>,
					ConfigurableApplicationContext>
			(					
					imageNameToApplicationBuilderSupplier,
				(env, appBuilderSupplier) -> new SimpleEntry<>(env, appBuilderSupplier),
				e -> {
					Map<String, Object> env = e.getKey().entrySet().stream().collect(Collectors.toMap(Entry::getKey, x -> (Object)x.getValue()));

					SpringApplicationBuilder appBuilder = e.getValue().get()
						.properties(env);
						
//					
//					ConfigurableEnvironment cenv = new StandardEnvironment();
//					cenv.getPropertySources().addFirst(new MapPropertySource("myPropertySource", env));

					
					ConfigurableApplicationContext r = appBuilder.run();
					return r;
				},
				(appBuilder, ctx) -> { if(ctx != null) try { ctx.close(); } catch(Exception e) { throw new RuntimeException(e); } }
			);
		return result;
	}

}
