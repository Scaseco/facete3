package org.hobbit.benchmark.faceted_browsing.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;

import javax.inject.Inject;

import org.hobbit.benchmark.faceted_browsing.config.amqp.DataQueueFactory;
import org.hobbit.core.Constants;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.reactivex.Flowable;

public class ConfigEvaluationStorage {

        @Inject
        protected DataQueueFactory dataQueueFactory;

		@Bean
		public Channel tg2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> tg2esReceiver(@Qualifier("tg2esChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createReceiver(channel, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }

		@Bean
		public Channel sa2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> sa2esReceiver(@Qualifier("sa2esChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createReceiver(channel, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
		

		@Bean
		public Channel es2emChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

//	    @Bean
//	    public Subscriber<ByteBuffer> es2emSender(@Qualifier("es2emChannel") Channel channel) throws Exception {
//	        return dataQueueFactory.createSender(channel, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
//	    }
	    
	    
		@Bean
		public Channel em2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
	    
//	    @Bean
//	    public Flowable<ByteBuffer> em2esReceiver(@Qualifier("em2esChannel") Channel channel) throws Exception {
//	        return dataQueueFactory.createReceiver(channel, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
//	    }

		
		//@Qualifier("requestQueueFactory")
	    @Bean
	    public Flowable<SimpleReplyableMessage<ByteBuffer>> es2emServer(
	    		@Qualifier("em2esChannel") Channel channel,
	    		@Qualifier("queueNameMapper") Function<String, String> queueNameMapper,
	            @Value("${" + Constants.EVAL_MODULE_2_EVAL_STORAGE_QUEUE_NAME_KEY + ":" + Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME + "}") String baseQueueName
	    		) throws Exception {
            String queueName = queueNameMapper.apply(baseQueueName);
	    	
            channel.queueDeclare(queueName, false, false, true, null);
            return RabbitMqFlows.createFlowableForQueue(() -> channel, c -> queueName);            
	    }

	    
		@Bean
		public Channel ackChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		@Bean
	    public Subscriber<ByteBuffer> taskAckSender(@Qualifier("ackChannel") Channel channel, @Qualifier("queueNameMapper") Function<String, String> queueNameMapper) throws IOException {
			String queueName = queueNameMapper.apply(Constants.HOBBIT_ACK_EXCHANGE_NAME);
            channel.exchangeDeclare(queueName, "fanout", false, true, null);
            
            return RabbitMqFlows.wrapPublishAsSubscriber(
            		RabbitMqFlows.wrapPublishAsConsumer(channel, queueName, "", null),
            		() -> 0);
			
			//return RabbitMqFlows.createFanoutSender(channel, queueName, null);
	    }		
	}