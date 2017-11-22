package org.hobbit.benchmark.faceted_browsing.config;

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;

public class ServiceDelegate<S extends Service>
	extends AbstractService
{
	protected S delegate;
	
	public ServiceDelegate(S delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	protected void doStart() {
		delegate.addListener(new Listener() {
			@Override
			public void terminated(State from) {
				ServiceDelegate.this.stopAsync();
			}
		}, MoreExecutors.directExecutor());
		
		delegate.startAsync().awaitRunning();
	}

	@Override
	protected void doStop() {
		delegate.stopAsync().awaitTerminated();
	}
}
