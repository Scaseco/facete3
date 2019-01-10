package org.hobbit.benchmark.faceted_browsing.main;

import org.hobbit.core.service.api.IdleServiceCapable;
import org.hobbit.core.service.docker.impl.spring_boot.ServiceSpringApplicationBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.AbstractIdleService;

public class TestServiceLaunch {
	
	@Qualifier("MainService")
	public static class ExampleIdleServiceCapable
		extends AbstractIdleService
	{
		private static final Logger logger = LoggerFactory.getLogger(ExampleIdleServiceCapable.class);

		@Override
		public void startUp() throws Exception {
			logger.info("ExampleIdleServiceCapable::startUp()");
		}

		@Override
		public void shutDown() throws Exception {
			logger.info("ExampleIdleServiceCapable::shutDown()");
		}
	
	}
	
	
	@Test
	public void testIdleServiceLaunch() {
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(ExampleIdleServiceCapable.class);

		ServiceSpringApplicationBuilder service = new ServiceSpringApplicationBuilder("testIdleServiceLaunch", appBuilder);
		service.startAsync().awaitRunning();
		service.stopAsync().awaitTerminated();
	}

	
	@Qualifier("MainService")
	public static class ExampleRunnableServiceCapable
		extends AbstractExecutionThreadService {

		private static final Logger logger = LoggerFactory.getLogger(ExampleRunnableServiceCapable.class);
		
		@Override
		public void startUp() throws Exception {
			logger.info("RunnableServiceCapable::startUp()");
		}

//		@Override
//		public void triggerShutdown()  {
//			logger.info("RunnableServiceCapable::triggerShutdown()");
//		}

		@Override
		public void run() throws Exception {
			logger.info("RunnableServiceCapable::run()");
			while(isRunning()) {
				logger.info("Running...");
				Thread.sleep(100);
			}
		}		
	}
	
	@Test
	public void testExecutionThreadServiceLaunch() {
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(ExampleRunnableServiceCapable.class);

		ServiceSpringApplicationBuilder service = new ServiceSpringApplicationBuilder("testExecutionThreadServiceLaunch", appBuilder);
		service.startAsync().awaitRunning();
		service.stopAsync().awaitTerminated();
	}
}
