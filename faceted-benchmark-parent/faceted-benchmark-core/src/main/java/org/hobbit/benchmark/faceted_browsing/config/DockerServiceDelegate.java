package org.hobbit.benchmark.faceted_browsing.config;

import java.util.function.Supplier;

import org.hobbit.core.service.docker.DockerService;

import com.google.common.util.concurrent.Service;

public class DockerServiceDelegate<S extends Service>
	extends ServiceDelegate<S>
	implements DockerService
{
	protected String imageName;
	protected Supplier<String> getContainerId;
	
	public DockerServiceDelegate(S service, String imageName, Supplier<String> getContainerId) {
		super(service);
		this.imageName = imageName;
		this.getContainerId = getContainerId;
	}

	protected String containerId;
	
	@Override
	public ServiceDelegate<S> startAsync() {
		this.containerId = getContainerId.get();
		
		return super.startAsync();
		//return this;
	}

	@Override
	public String getContainerId() {
		return containerId;
	}

	@Override
	public String getImageName() {
		return imageName;
	}

	@Override
	public int getExitCode() {
		return 0;
	}
}
