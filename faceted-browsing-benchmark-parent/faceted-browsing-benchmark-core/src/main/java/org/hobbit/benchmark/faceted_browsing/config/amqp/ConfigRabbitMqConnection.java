package org.hobbit.benchmark.faceted_browsing.config.amqp;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.context.annotation.Bean;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ConfigRabbitMqConnection {
	@Bean
	public Connection commandConnection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
		return connectionFactory.newConnection();
	}
}