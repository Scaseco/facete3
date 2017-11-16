package org.hobbit.core.config;

import javax.inject.Inject;

import org.hobbit.core.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.rabbitmq.client.ConnectionFactory;

public class ConfigRabbitMqConnectionFactory {
	
	@Inject
	protected Environment env;
	
	@Bean
	public ConnectionFactory connectionFactory() {

        String rabbitMQHostName = env.getProperty(Constants.RABBIT_MQ_HOST_NAME_KEY, "localhost");
        
		ConnectionFactory result = new ConnectionFactory();
        result.setHost(rabbitMQHostName);
        result.setPort(5672);
        result.setAutomaticRecoveryEnabled(true);
        result.setVirtualHost("default");
        // attempt recovery every 10 seconds
        result.setNetworkRecoveryInterval(10000);
                
        return result;
	}

}
