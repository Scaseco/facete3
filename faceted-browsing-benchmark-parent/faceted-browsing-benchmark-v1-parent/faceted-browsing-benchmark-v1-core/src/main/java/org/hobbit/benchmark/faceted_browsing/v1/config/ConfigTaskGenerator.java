package org.hobbit.benchmark.faceted_browsing.v1.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.inject.Inject;

import org.hobbit.benchmark.faceted_browsing.config.amqp.DataQueueFactory;
import org.hobbit.core.Constants;
import org.hobbit.core.config.RabbitMqFlows;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.reactivex.Flowable;

public class ConfigTaskGenerator {

    @Inject
    protected DataQueueFactory dataQueueFactory;

	/*
	 * Reception from dg 
	 */

	@Bean
	public Channel dg2tgChannel(Connection connection) throws IOException {
		return connection.createChannel();
	}
	
    @Bean
    public Flowable<ByteBuffer> dg2tgReceiver(@Qualifier("dg2tgChannel") Channel channel) throws Exception {
        return dataQueueFactory.createReceiver(channel, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
    }

	/*
	 * Transfer to sa 
	 */	    
    
	@Bean
	public Channel tg2saChannel(Connection connection) throws IOException {
		return connection.createChannel();
	}

	
    @Bean
    public Subscriber<ByteBuffer> tg2saSender(@Qualifier("tg2saChannel") Channel channel) throws Exception {
        return dataQueueFactory.createSender(channel, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
    }


	/*
	 * Transfer to es 
	 */	    
    
	@Bean
	public Channel tg2esChannel(Connection connection) throws IOException {
		return connection.createChannel();
	}

    @Bean
    public Subscriber<ByteBuffer> tg2esSender(@Qualifier("tg2esChannel") Channel channel) throws Exception {
        return dataQueueFactory.createSender(channel, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
    }
    
    
    /*
     * Reception of task acknowledgements from es
     */

	@Bean
	public Channel ackChannel(Connection connection) throws IOException {
		return connection.createChannel();
	}

    @Bean
    public Flowable<ByteBuffer> taskAckReceiver(@Qualifier("ackChannel") Channel channel, @Value("${componentName:anonymous}") String componentName, @Qualifier("queueNameMapper") Function<String, String> queueNameMapper) throws IOException, TimeoutException {
    	String queueName = queueNameMapper.apply(Constants.HOBBIT_ACK_EXCHANGE_NAME);
    	return RabbitMqFlows.createFanoutReceiver(channel, queueName, "ack" + "." + componentName, x -> Collections.singletonList(x));
    }
}