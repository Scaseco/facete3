package org.hobbit.core.service.api;

import java.util.function.Supplier;

import org.hobbit.core.service.docker.DockerService;

import com.google.common.util.concurrent.Service;

/**
 * Wrapper which treats a non-DockerService as a DockerService by allocating a container ID
 * on startup
 * 
 * @author raven Dec 6, 2017
 *
 * @param <S>
 */
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
