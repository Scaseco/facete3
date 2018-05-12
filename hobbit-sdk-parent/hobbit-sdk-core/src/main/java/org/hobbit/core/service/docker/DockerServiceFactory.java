package org.hobbit.core.service.docker;

import java.util.Map;

/**
 * In constrast to the builder, the service factory only provides a single create method.
 * 
 * @author raven Nov 18, 2017
 *
 */
public interface DockerServiceFactory<T extends DockerService> {
	T create(String imageName, Map<String, String> env);
}
