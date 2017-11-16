package org.hobbit.core.service.docker;

import java.util.function.Supplier;

public interface DockerServiceBuilderFactory<T extends DockerService>
	extends Supplier<DockerServiceBuilder<T>>
{
}
