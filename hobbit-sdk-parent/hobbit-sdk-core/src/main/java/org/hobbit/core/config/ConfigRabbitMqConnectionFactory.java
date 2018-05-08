package org.hobbit.core.config;

import org.hobbit.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.rabbitmq.client.ConnectionFactory;


public class ConfigRabbitMqConnectionFactory
	implements EnvironmentAware
{	
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigRabbitMqConnectionFactory.class);

	protected Environment env;
	
	
	public static final String AMQP_VHOST = "AMQP_VHOST";
	
	@Bean
	public ConnectionFactory connectionFactory() {

        String rabbitMQHostName = env.getProperty(Constants.RABBIT_MQ_HOST_NAME_KEY, "localhost");
        //int port = env.getProperty(Constants.po)

        String amqpVHost = env.getProperty(AMQP_VHOST);

        logger.info("Expecting AMQP Server to be reachable at " + rabbitMQHostName + " with vhost " + amqpVHost);
        
		ConnectionFactory result = new ConnectionFactory();
        result.setHost(rabbitMQHostName);
        result.setPort(5672);
        result.setAutomaticRecoveryEnabled(true);
        
        if(amqpVHost != null) {
            result.setVirtualHost(amqpVHost);
        }
        
        // attempt recovery every 10 seconds
        result.setNetworkRecoveryInterval(10000);

        return result;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.env = environment;
	}
}
