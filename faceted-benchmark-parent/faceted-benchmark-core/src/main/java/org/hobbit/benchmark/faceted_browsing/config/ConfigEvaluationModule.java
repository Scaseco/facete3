package org.hobbit.benchmark.faceted_browsing.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.inject.Inject;

import org.hobbit.benchmark.faceted_browsing.config.amqp.DataQueueFactory;
import org.hobbit.core.Constants;
import org.hobbit.core.config.RabbitMqFlows;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.reactivex.Flowable;

public class ConfigEvaluationModule {

        @Inject
        protected DataQueueFactory dataQueueFactory;

		@Bean
		public Channel em2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		@Bean
		public Channel es2emChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		@Bean
	    public Function<ByteBuffer, CompletableFuture<ByteBuffer>> em2esClient(
	    		@Qualifier("em2esChannel") Channel em2esChannel,
	    		@Qualifier("es2emChannel") Channel es2emChannel,
	    		@Qualifier("queueNameMapper") Function<String, String> queueNameMapper) throws Exception {

			String em2esQueueName = queueNameMapper.apply(Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
			String es2emQueueName = queueNameMapper.apply(Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);

			Subscriber<ByteBuffer> sender = RabbitMqFlows.createDataSender(em2esChannel, em2esQueueName, es2emQueueName);
			Flowable<ByteBuffer> receiver = RabbitMqFlows.createDataReceiver(es2emChannel, es2emQueueName);
			
			Function<ByteBuffer, CompletableFuture<ByteBuffer>> result = RabbitMqFlows.wrapAsFunction(sender, receiver);

			return result;
		}

	}