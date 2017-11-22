package org.hobbit.benchmark.faceted_browsing.config;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.Service;

public class ServiceDelegate<S extends Service>
	implements Service
{
	protected S delegate;
	
	public ServiceDelegate(S delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public ServiceDelegate<S> startAsync() {
		delegate.startAsync();
		return this;
	}

	@Override
	public boolean isRunning() {
		return delegate.isRunning();
	}

	@Override
	public State state() {
		return delegate.state();
	}

	@Override
	public ServiceDelegate<S> stopAsync() {
		delegate.stopAsync();
		return this;
	}

	@Override
	public void awaitRunning() {
		delegate.awaitRunning();
	}

	@Override
	public void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
		delegate.awaitRunning(timeout, unit);
	}

	@Override
	public void awaitTerminated() {
		delegate.awaitTerminated();
	}

	@Override
	public void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
		delegate.awaitTerminated(timeout, unit);
	}

	@Override
	public Throwable failureCause() {
		return delegate.failureCause();
	}

	@Override
	public void addListener(Listener listener, Executor executor) {
		delegate.addListener(listener, executor);
	}
}


//@Override
//protected void doStart() {
//	delegate.addListener(new Listener() {
//		@Override
//		public void terminated(State from) {
//			ServiceDelegate.this.stopAsync();
//		}
//	}, MoreExecutors.directExecutor());
//	
//	delegate.startAsync().awaitRunning();
//}
//
//@Override
//protected void doStop() {
//	delegate.stopAsync().awaitTerminated();
//}
