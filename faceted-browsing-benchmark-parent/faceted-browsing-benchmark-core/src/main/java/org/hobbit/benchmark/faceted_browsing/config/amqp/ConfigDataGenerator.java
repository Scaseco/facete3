package org.hobbit.benchmark.faceted_browsing.config.amqp;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.hobbit.core.Constants;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class ConfigDataGenerator {
	
    @Inject
    protected DataQueueFactory dataQueueFactory;

	@Bean
	public Channel dg2tgChannel(Connection connection) throws IOException {
		return connection.createChannel();
	}
	
    @Bean
    public Subscriber<ByteBuffer> dg2tgSender(@Qualifier("dg2tgChannel") Channel channel) throws Exception {
        return dataQueueFactory.createSender(channel, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
    }

    
	@Bean
	public Channel dg2saChannel(Connection connection) throws IOException {
		return connection.createChannel();
	}

    @Bean
    public Subscriber<ByteBuffer> dg2saSender(@Qualifier("dg2saChannel") Channel channel) throws Exception {
        return dataQueueFactory.createSender(channel, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
    }

}