package org.hobbit.core.service.api;

import java.util.function.Supplier;

import org.hobbit.core.service.docker.DockerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Service;

import jersey.repackaged.com.google.common.util.concurrent.MoreExecutors;

/**
 * Wrapper which treats a non-DockerService as a DockerService by allocating a container ID
 * on startup
 * 
 * @author raven Dec 6, 2017
 *
 * @param <S>
 */
public class DockerServicePseudoDelegate<S extends Service>
	extends ServiceDelegate<S>
	implements DockerService
{
	private static final Logger logger = LoggerFactory.getLogger(DockerServicePseudoDelegate.class);


	protected String imageName;
	protected Supplier<String> getContainerId;
	
	public DockerServicePseudoDelegate(S service, String imageName, Supplier<String> getContainerId) {
		super(service);
		this.imageName = imageName;
		this.getContainerId = getContainerId;
		
		// Register a listener for updating the exit code
		// This is registration happens here at the earliest point in the ctor
		// so that further listeners can access the exit code
		delegate.addListener(new Listener() {
			@Override
			public void failed(State from, Throwable failure) {
				exitCode = 1;
			}
			
			@Override
			public void terminated(State from) {
				exitCode = 0;
			}
		}, MoreExecutors.directExecutor());

	}

	protected String containerId;
	protected Integer exitCode;
	
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
	public Integer getExitCode() {
		return exitCode;
	}
}
