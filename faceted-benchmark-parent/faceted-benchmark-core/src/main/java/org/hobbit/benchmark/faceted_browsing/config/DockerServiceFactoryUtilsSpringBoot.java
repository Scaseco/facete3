package org.hobbit.benchmark.faceted_browsing.config;

import java.io.Closeable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

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
	public static DockerServiceFactory<?> createDockerServiceFactoryForBootstrap(Map<String, Class<?>> imageNameToClass, Supplier<SpringApplicationBuilder> baseAppBuilder) {

		DockerServiceFactory<DockerService> result = new DockerServiceFactoryGeneric<
					Class<?>,
					Entry<Map<String, String>, SpringApplicationBuilder>,
					ConfigurableApplicationContext>
			(					
				imageNameToClass,
				(env, clazz) -> new SimpleEntry<>(env, baseAppBuilder.get().sources(clazz)),
				e -> {
					Map<String, Object> env = e.getKey().entrySet().stream().collect(Collectors.toMap(Entry::getKey, x -> (Object)x.getValue()));
					
					ConfigurableEnvironment cenv = new StandardEnvironment();
					cenv.getPropertySources().addFirst(new MapPropertySource("myPropertySource", env));

					ConfigurableApplicationContext r = e.getValue().environment(cenv).build().run();
					return r;
				},
				(appBuilder, ctx) -> { if(ctx != null) try { ctx.close(); } catch(Exception e) { throw new RuntimeException(e); } }
			);
		return result;
	}

}
