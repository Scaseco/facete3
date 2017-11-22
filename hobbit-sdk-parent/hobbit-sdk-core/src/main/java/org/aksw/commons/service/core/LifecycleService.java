package org.aksw.commons.service.core;

import org.springframework.context.Lifecycle;

import com.google.common.util.concurrent.Service;


/**
 * Wrapper which allows a guava service's life cycle to be managed by spring
 * 
 * <pre>
 * {@code
 * @Bean
 * public LifeCycle myService() {
 *   return new LifecyleService(guavaService);
 * }
 * <pre>
 * 
 * 
 * @author raven Nov 21, 2017
 *
 */
public class LifecycleService<T extends Service>
	implements Lifecycle
{
	protected T service;

	public LifecycleService(T service) {
		super();
		this.service = service;
	}
	
	public T getService() {
		return service;
	}

	@Override
	public void start() {
		service.startAsync().awaitRunning();
	}

	@Override
	public void stop() {
		service.stopAsync().awaitTerminated();
	}

	@Override
	public boolean isRunning() {
		return service.isRunning();
	}

	@Override
	public String toString() {
		return "LifecycleService [service=" + service + "]";
	}
	
}
