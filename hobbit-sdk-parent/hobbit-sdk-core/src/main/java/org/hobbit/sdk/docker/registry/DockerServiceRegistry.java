package org.hobbit.sdk.docker.registry;

import java.util.Map;

import org.hobbit.core.service.docker.DockerServiceBuilderFactory;

public interface DockerServiceRegistry {
	Map<String, DockerServiceBuilderFactory<?>> getServiceFactoryMap();

//	default DockerServiceFactory<?> asDockerServiceFactory() {
//		
//	}
}
