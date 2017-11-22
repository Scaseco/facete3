package org.hobbit.benchmark.faceted_browsing.config;

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
	extends AbstractIdleService
{
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
		ctx = appBuilder.run(args);
	}

	@Override
	protected void shutDown() throws Exception {
		if(ctx != null) {
			ctx.close();
		}
	}
}
