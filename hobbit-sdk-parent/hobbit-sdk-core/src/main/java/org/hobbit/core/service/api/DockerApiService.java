package org.hobbit.core.service.api;

import org.hobbit.core.service.docker.DockerService;

public abstract class DockerApiService<A>
	extends DockerServiceIdleServiceDelegate
{
	public DockerApiService(DockerService delegate) {
		super(delegate);
	}

	public abstract A getApi();
}
