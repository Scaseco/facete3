package org.hobbit.qpid.v7.main;

import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class MainQpidBroker {
	public static void main(String[] args) {
		// Note: The Broker bean is already started
		new SpringApplicationBuilder()
			.sources(ConfigQpidBroker.class)
			//.sources(ApplicationRunnerQpidBroker.class)
			.bannerMode(Banner.Mode.OFF)
			.run(args);
	}
}
