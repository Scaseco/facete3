package org.hobbit.benchmark.faceted_browsing.main;

import org.hobbit.benchmark.faceted_browsing.config.ServiceSpringApplicationBuilder;
import org.hobbit.core.service.api.IdleServiceCapable;
import org.hobbit.core.service.api.RunnableServiceCapable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class TestServiceLaunch {
	
	
	public static class ExampleIdleServiceCapable
		implements IdleServiceCapable
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
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(ExampleIdleServiceCapable.class, LauncherServiceCapable.class);

		ServiceSpringApplicationBuilder service = new ServiceSpringApplicationBuilder("testIdleServiceLaunch", appBuilder);
		service.startAsync().awaitRunning();
		service.stopAsync().awaitTerminated();
	}

	
	public static class ExampleRunnableServiceCapable
		implements RunnableServiceCapable {

		private static final Logger logger = LoggerFactory.getLogger(ExampleRunnableServiceCapable.class);
		
		protected boolean isShutDown = false;
		
		@Override
		public void startUp() throws Exception {
			logger.info("RunnableServiceCapable::startUp()");
		}

		@Override
		public void shutDown() throws Exception {
			logger.info("RunnableServiceCapable::shutDown()");
			isShutDown = true;			
		}

		@Override
		public void run() throws Exception {
			logger.info("RunnableServiceCapable::run()");
			while(!isShutDown) {
				logger.info("Running...");
				Thread.sleep(100);
			}
		}		
	}
	
	@Test
	public void testExecutionThreadServiceLaunch() {
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(ExampleRunnableServiceCapable.class, LauncherServiceCapable.class);

		ServiceSpringApplicationBuilder service = new ServiceSpringApplicationBuilder("testExecutionThreadServiceLaunch", appBuilder);
		service.startAsync().awaitRunning();
		service.stopAsync().awaitTerminated();
	}
}
