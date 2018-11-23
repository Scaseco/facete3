package org.hobbit.sdk.docker.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.hobbit.core.service.docker.ServiceSpringApplicationBuilder;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class DockerServiceRegistryImpl
	implements DockerServiceRegistry
{
	protected Map<String, DockerServiceBuilderFactory<?>> serviceFactoryMap = new LinkedHashMap<>();
	
	@Override
	public Map<String, DockerServiceBuilderFactory<?>> getServiceFactoryMap() {
		return serviceFactoryMap;
	}
	
	
	private static DockerServiceRegistry defaultDockerServiceRegistry;
	

	// TODO provide another data structure for applying overrides and modifiers
	
	public static DockerServiceRegistry get() {
		if(defaultDockerServiceRegistry == null) {
			defaultDockerServiceRegistry = new DockerServiceRegistryImpl();
		}
		
		return defaultDockerServiceRegistry;
	}

	
	public static DockerServiceRegistry registerSpringApplications(DockerServiceRegistry registry, Map<String, Supplier<SpringApplicationBuilder>> map) {
		Map<String, DockerServiceBuilderFactory<?>> m = ServiceSpringApplicationBuilder.convert(map);

		registry.getServiceFactoryMap().putAll(m);
		return registry;
	}
}
