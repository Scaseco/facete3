package org.hobbit.qpid.main;

import org.hobbit.qpid.config.ConfigQpidBroker;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class MainQpidBroker {
	public static void main(String[] args) {
		new SpringApplicationBuilder()
			.sources(ConfigQpidBroker.class)
			.sources(ApplicationRunnerQpidBroker.class)
			.run(args);
	}
}
