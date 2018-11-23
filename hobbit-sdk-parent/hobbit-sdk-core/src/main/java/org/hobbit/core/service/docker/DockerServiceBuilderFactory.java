package org.hobbit.core.service.docker;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.boot.builder.SpringApplicationBuilder;

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
	
	
	/**
	 * Creates a DockerServiceBuilderFactory from a Supplier<B>.
	 * A down cast is performed (instead of wrapping) if this supplier is already a DockerServiceBuilderFactory
	 * 
	 * 
	 * @param builderSupplier
	 * @return
	 */
	public static <B extends DockerServiceBuilder<? extends DockerService>> DockerServiceBuilderFactory<B> from(Supplier<B> builderSupplier) {
		DockerServiceBuilderFactory<B> result = builderSupplier instanceof DockerServiceBuilderFactory
				? (DockerServiceBuilderFactory<B>)builderSupplier
				: () -> builderSupplier.get()
				;

		return result;
	}
}
