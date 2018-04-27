package org.hobbit.core.service.api;

import org.hobbit.core.service.docker.DockerService;

public class DockerServiceDelegate<S extends DockerService>
	extends ServiceDelegate<S>
	implements DockerService
{
	public DockerServiceDelegate(S delegate) {
		super(delegate);
	}

	@Override
	public String getContainerId() {
		String result = delegate.getContainerId();
		return result;
	}

	@Override
	public String getImageName() {
		String result = delegate.getImageName();
		return result;
	}

	@Override
	public Integer getExitCode() {
		Integer result = delegate.getExitCode();
		return result;
	}
}
