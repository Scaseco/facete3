package org.hobbit.core.service.docker;

import java.util.function.Supplier;

/**
 * A convenience interface which can be used e.g. in dependency injection.
 * The get() method should be thread safe and returned builders should be independent of each other.
 * 
 * @author raven Nov 21, 2017
 *
 * @param <T>
 */
public interface DockerServiceBuilderFactory<B extends DockerServiceBuilder<? extends DockerService>>
	extends Supplier<B>
{
}
