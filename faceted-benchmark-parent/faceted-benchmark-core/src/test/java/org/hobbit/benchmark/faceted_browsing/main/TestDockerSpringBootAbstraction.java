package org.hobbit.benchmark.faceted_browsing.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hobbit.benchmark.faceted_browsing.config.DockerServiceFactoryUtilsSpringBoot;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.junit.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class TestDockerSpringBootAbstraction {

	public static class Context {
		@Bean
		public ApplicationRunner runner(Environment env) {
			return (args) -> {
				System.out.println("Hello " + env.getRequiredProperty("MSG"));
			};
		}
	}
	
	@Test
	public void test() {
        Map<String, Class<?>> imageNameToClass = new HashMap<>();
        imageNameToClass.put("myApp", Context.class);
        
        DockerServiceFactory<?> serviceFactory = DockerServiceFactoryUtilsSpringBoot.createDockerServiceFactoryForBootstrap(imageNameToClass,
        		() -> new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF));

        {
	        DockerService service = serviceFactory.create("myApp", Collections.singletonMap("MSG", "World1"));
	        service.startAsync().awaitRunning();
	        service.stopAsync().awaitTerminated();        
	        System.out.println("Service had id: " + service.getContainerId());
        }

        {
	        DockerService service = serviceFactory.create("myApp", Collections.singletonMap("MSG", "World2"));
	        service.startAsync().awaitRunning();
	        service.stopAsync().awaitTerminated();        
	        System.out.println("Service had id: " + service.getContainerId());
        }
	}
}
