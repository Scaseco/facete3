package org.hobbit.core.service.api;

import org.apache.jena.ext.com.google.common.util.concurrent.MoreExecutors;
import org.hobbit.core.service.docker.DockerService;

@Deprecated
public class DockerServiceIdleServiceDelegate
	extends ServiceDelegate<DockerService>
	implements DockerService
{
	protected DockerService delegate;

	
	public DockerServiceIdleServiceDelegate(DockerService delegate) {
		super(delegate);
		//this.delegate = delegate;

		delegate.addListener(new Listener() {
			@Override
			public void terminated(State from) {
				stopAsync();
				
				super.terminated(from);
			}
		}, MoreExecutors.directExecutor());
	}

//	@Override
//	protected void doStart() throws Exception {
//		delegate.startAsync().awaitRunning();
//	}
//
//	@Override
//	protected void shutDown() throws Exception {
//		delegate.stopAsync().awaitTerminated();
//	}

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
		int result = delegate.getExitCode();
		return result;
	}
	
	
}
