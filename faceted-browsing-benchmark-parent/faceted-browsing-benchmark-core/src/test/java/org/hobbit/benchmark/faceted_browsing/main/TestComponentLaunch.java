package org.hobbit.benchmark.faceted_browsing.main;

import org.hobbit.core.config.ConfigGson;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceBuilder;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderFactory;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

public class TestComponentLaunch
{
	public static class Context {
		
		
		@Bean
		public ApplicationRunner runner(DockerServiceBuilderFactory<?> serviceBuilderFactory) {
			return (args) -> {
				DockerServiceBuilder<?> builder = serviceBuilderFactory.get();
				//builder.setImageName(imageName)
				DockerService service = builder.get();
				
				service.startAsync().awaitRunning();
				
				
				System.out.println("here");
			};
		}
	}
	
	//@Test
	public void testDockerCommunication() throws InterruptedException {
		try(ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
				.sources(ConfigQpidBroker.class)
				.sources(ConfigGson.class)
				.sources(ConfigRabbitMqConnectionFactory.class)
//				.sources(TestDockerCommunication.class)
				.sources(Context.class)
				.run()) {
		}
	}
}
