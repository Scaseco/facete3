package org.hobbit.benchmark.faceted_browsing.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import com.google.common.util.concurrent.AbstractIdleService;


/**
 * Wrap a SpringApplicationBuilder as a service
 * @author raven Nov 22, 2017
 *
 */
public class ServiceSpringApplicationBuilder
	extends AbstractIdleService//AbstractExecutionThreadService
{	
	private static final Logger logger = LoggerFactory.getLogger(ServiceSpringApplicationBuilder.class);

	
	protected String appName;
	protected SpringApplicationBuilder appBuilder;
	protected String[] args;

	protected ConfigurableApplicationContext ctx = null;

	
	public ServiceSpringApplicationBuilder(String appName, SpringApplicationBuilder appBuilder) {
		this(appName, appBuilder, new String[]{});
	}
	
	public ServiceSpringApplicationBuilder(String appName, SpringApplicationBuilder appBuilder, String[] args) {
		super();
		this.appName = appName;
		this.appBuilder = appBuilder;
		this.args = args;
	}

	@Override
	protected void startUp() throws Exception {
		appBuilder.listeners(new ApplicationListener<ApplicationEvent>() {
			@Override
			public void onApplicationEvent(ApplicationEvent event) {
				if(event instanceof ContextClosedEvent) {
					try {
						logger.info("Context closed ; terminating service " + appName);
						shutDown();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		
//		logger.info("Launching " );
		logger.info("ServiceSpringApplicationBuilder::startUp [begin] " + appName + ", builderHash: " + appBuilder.hashCode());
		ctx = appBuilder.run(args);
		logger.info("ServiceSpringApplicationBuilder::startUp [end] " + appName + ", builderHash: " + appBuilder.hashCode());
//		logger.info("ServiceSpringApplicationBuilder: context terminated, builderHash:" + appBuilder.hashCode());
	}

//	@Override
//	protected void run() throws Exception {
//	}
	
	@Override
	protected void shutDown() {
		logger.info("ServiceSpringApplicationBuilder: Shutdown invoked");
		ctx = appBuilder.context();
		if(ctx != null) {
			ctx.close();
		}
	}
}
