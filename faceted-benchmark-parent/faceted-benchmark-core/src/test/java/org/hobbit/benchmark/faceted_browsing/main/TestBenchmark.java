package org.hobbit.benchmark.faceted_browsing.main;

import org.hobbit.benchmark.faceted_browsing.config.ConfigDockerServiceFactoryHobbitFacetedBenchmarkLocal;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.service.docker.DockerServiceManagerServerComponent;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;


public class TestBenchmark {
	
	class ConfigDockerServiceManagerServiceComponent {
		@Bean
		public DockerServiceManagerServerComponent dockerServiceManagerServer() {
			
		}
	}

	class ConfigRabbitMqConnection {
		
	}
	
	class ConfigChannelRabbitMq {
		
	}
	
	class ConfigCommandReceivingComponentRabbitMq {
		@Bean
		public Connection(ConnectionFactory connectionFactory) {
			return connectionFactory.newConnection();
		}

	}
	
	class ConfigDockerServiceManagerClientComponent {
		@Bean
		public DockerServiceManagerServerComponent dockerServiceManagerClient() {
			
		}
	}
	
	
	@Import
	class ConfigHobbitFacetedBenchmarkController {
		@Bean
		public benchmarkController() {
			
		}
	}
	
	@Test
	public void testBenchmark() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder()
				// Add the amqp broker
				.sources(ConfigQpidBroker.class)
					.child(ConfigRabbitMqConnectionFactory.class)
						// Connect the docker service factory to the amqp infrastructure 
						.child(ConfigDockerServiceFactoryHobbitFacetedBenchmarkLocal.class, ConfigDockerServiceManagerServiceComponent.class) // Connect the local docker service factory to the rabbit mq channels
						// Add the benchmark component
						.sibling(ConfigBenchmarkControllerChannels.class, ConfigDockerServiceManagerClientComponent.class, ConfigHobbitFacetedBenchmarkController.class);

		try(ConfigurableApplicationContext ctx = builder.run()) {}

	}
}
