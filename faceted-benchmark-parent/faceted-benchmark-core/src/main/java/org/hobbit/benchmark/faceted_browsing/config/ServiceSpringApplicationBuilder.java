package org.hobbit.benchmark.faceted_browsing.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import com.google.common.util.concurrent.AbstractExecutionThreadService;


/**
 * Wrap a SpringApplicationBuilder as a service
 * @author raven Nov 22, 2017
 *
 */
public class ServiceSpringApplicationBuilder
	extends AbstractExecutionThreadService
{	
	private static final Logger logger = LoggerFactory.getLogger(ServiceSpringApplicationBuilder.class);

	
	protected SpringApplicationBuilder appBuilder;
	protected String[] args;

	protected ConfigurableApplicationContext ctx = null;

	
	public ServiceSpringApplicationBuilder(SpringApplicationBuilder appBuilder) {
		this(appBuilder, new String[]{});
	}
	
	public ServiceSpringApplicationBuilder(SpringApplicationBuilder appBuilder, String[] args) {
		super();
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
						shutDown();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
	}

	@Override
	protected void run() throws Exception {
		logger.info("SpringApplicationBuilder as service: launching, builderHash: " + appBuilder.hashCode());
		ctx = appBuilder.run(args);
		logger.info("SpringApplicationBuilder as service: context terminated, builderHash:" + appBuilder.hashCode());
	}
	
	@Override
	protected void shutDown() throws Exception {
		if(ctx != null) {
			ctx.close();
		}
	}
}
