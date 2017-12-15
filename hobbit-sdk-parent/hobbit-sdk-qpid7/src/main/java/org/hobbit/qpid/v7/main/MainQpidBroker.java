package org.hobbit.qpid.v7.main;

import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class MainQpidBroker {
	public static void main(String[] args) {
		// NOTE The Broker bean is already started
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
			.sources(ConfigQpidBroker.class)
			//.sources(ApplicationRunnerQpidBroker.class)
			.bannerMode(Banner.Mode.OFF)
			.run(args);
		
		// NOTE The context is not closed deliberately in order to keep the amqp server running
	}
}
