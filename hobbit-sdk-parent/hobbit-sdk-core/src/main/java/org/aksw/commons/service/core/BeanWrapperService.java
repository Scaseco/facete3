package org.aksw.commons.service.core;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

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
public class BeanWrapperService<T extends Service>
	implements InitializingBean, DisposableBean
{
	protected T service;

	public BeanWrapperService(T service) {
		super();
		this.service = service;
	}
	
	public T getService() {
		return service;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println(service + ": AWAITING RUNNING STATE");
		service.startAsync().awaitRunning();
		System.out.println(service + ": RUNNING");
	}

	@Override
	public void destroy() throws Exception {
		service.stopAsync().awaitTerminated();
	}


//
//	@Override
//	public void start() {
//		service.startAsync().awaitRunning();
//	}
//
//	@Override
//	public void stop() {
//		service.stopAsync().awaitTerminated();
//	}
//
//	@Override
//	public boolean isRunning() {
//		return service.isRunning();
//	}

	@Override
	public String toString() {
		return "BeanWrapper [service=" + service + "]";
	}
	
}
