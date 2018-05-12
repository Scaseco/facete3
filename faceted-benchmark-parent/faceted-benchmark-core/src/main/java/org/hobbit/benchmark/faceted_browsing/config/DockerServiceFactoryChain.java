package org.hobbit.benchmark.faceted_browsing.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerServiceFactoryChain
	implements DockerServiceFactory<DockerService>
{	
	private static final Logger logger = LoggerFactory.getLogger(DockerServiceFactoryChain.class);
	
	protected List<DockerServiceFactory<?>> factories = new ArrayList<>();

	public DockerServiceFactoryChain() {		
	}
	
	public DockerServiceFactoryChain(DockerServiceFactory<?> ... _factories) {
		factories.addAll(Arrays.asList(_factories));
	}
	
	
	@Override
	public DockerService create(String imageName, Map<String, String> env) {
		
		DockerService result = null;
		for(DockerServiceFactory<?> factory : factories) {
			try {
				result = factory.create(imageName, env);
				break;
			} catch(UnsupportedOperationException e) {
				logger.info("Service factory did not support " + imageName + ", trying next one");
			}
		}
		
		if(result == null) {
			throw new UnsupportedOperationException("No image " + imageName + " registered with this docker service factory");
		}

		return result;
	}
	
	public void addFactory(DockerServiceFactory<?> factory) {
		factories.add(factory);
	}
	
	public List<DockerServiceFactory<?>> getFactories() {
		return factories;
	}
}
