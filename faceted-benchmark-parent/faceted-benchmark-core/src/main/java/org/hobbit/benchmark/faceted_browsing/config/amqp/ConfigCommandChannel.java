package org.hobbit.benchmark.faceted_browsing.config.amqp;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.hobbit.core.Constants;
import org.hobbit.core.config.CommunicationWrapper;
import org.hobbit.core.config.RabbitMqFlows;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.reactivex.Flowable;

public class ConfigCommandChannel {

		@Bean
		public Channel commandChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
		
		@Bean
		public Flowable<ByteBuffer> commandReceiver(
				Channel channel, CommunicationWrapper<ByteBuffer> wrapper, @Value("${componentName:anonymous}") String componentName) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {

			//System.out.println("COMPONENT NAME: " + componentName);
			Flowable<ByteBuffer> result = RabbitMqFlows.createFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "cmd" + "." + componentName, wrapper::wrapReceiver);
//					.flatMap(msg -> Flowable.fromIterable(wrapper.wrapReceiver(msg)));
			return result;
		}
		
		@Bean
		public Subscriber<ByteBuffer> commandSender(
				Channel channel,
				CommunicationWrapper<ByteBuffer> wrapper
				) throws IOException {
				//@Autowired(required=false) @Qualifier("foo") Function<ByteBuffer, ByteBuffer> transformer) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {
			
			
			Subscriber<ByteBuffer> result = RabbitMqFlows.createFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, wrapper::wrapSender);
			return result;
			//return //RabbitMqFlows.createReplyableFanoutSender(channel, exchangeName, transformer)
		}
	}