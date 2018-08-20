package org.hobbit.benchmark.faceted_browsing.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.hobbit.benchmark.faceted_browsing.config.DockerServiceFactorySpringApplicationBuilder;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service;

public class TestDockerSpringBootAbstraction {

	public static class Context {
		@Bean
		@Qualifier("MainService")
		public Service runner(Environment env) {
			return new AbstractExecutionThreadService() {
				@Override
				protected void run() throws Exception {
					System.out.println("Hello " + env.getRequiredProperty("MSG"));
				}
			};
		}
	}
	
	@Test
	public void test() {
        Map<String, Supplier<SpringApplicationBuilder>> imageNameToAppBuilder = new HashMap<>();
        imageNameToAppBuilder.put("myFirstImage", () -> new SpringApplicationBuilder(Context.class).bannerMode(Banner.Mode.OFF));
        imageNameToAppBuilder.put("mySecondImage", () -> new SpringApplicationBuilder(Context.class).bannerMode(Banner.Mode.OFF));
        
        DockerServiceFactory<?> serviceFactory = new DockerServiceFactorySpringApplicationBuilder(imageNameToAppBuilder);

        {
	        DockerService service = serviceFactory.create("myFirstImage", Collections.singletonMap("MSG", "World1"));
	        service.startAsync().awaitRunning();
	        service.stopAsync().awaitTerminated();        
	        System.out.println("Service had id: " + service.getContainerId());
        }

        {
	        DockerService service = serviceFactory.create("myFirstImage", Collections.singletonMap("MSG", "World2"));
	        service.startAsync().awaitRunning();
	        service.stopAsync().awaitTerminated();        
	        System.out.println("Service had id: " + service.getContainerId());
        }

        {
	        DockerService service = serviceFactory.create("mySecondImage", Collections.singletonMap("MSG", "World3"));
	        service.startAsync().awaitRunning();
	        service.stopAsync().awaitTerminated();        
	        System.out.println("Service had id: " + service.getContainerId());
        }
	}
}
