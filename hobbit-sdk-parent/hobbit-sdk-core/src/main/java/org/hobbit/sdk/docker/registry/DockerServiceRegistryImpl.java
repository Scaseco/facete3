package org.hobbit.sdk.docker.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hobbit.core.service.docker.DockerServiceBuilderFactory;

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

}
