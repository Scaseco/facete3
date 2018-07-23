package org.hobbit.benchmark.faceted_browsing.config.amqp;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.hobbit.core.Constants;
import org.hobbit.core.config.CommunicationWrapper;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.reactivestreams.Subscriber;
import org.springframework.context.annotation.Bean;

import com.rabbitmq.client.Channel;

import io.reactivex.Flowable;

/**
 * Creates replyable fanout sender and receiver beans over a channel
 * 
 * @author raven Nov 20, 2017
 *
 */
public class ConfigReplyableCommandChannel {
	
	@Bean
	public Flowable<SimpleReplyableMessage<ByteBuffer>> replyableCommandReceiver(				
			Channel channel,
			CommunicationWrapper<ByteBuffer> wrapper) throws IOException {
			//@Value("commandExchange") String commandExchange) throws IOException {
		Flowable<SimpleReplyableMessage<ByteBuffer>> result = RabbitMqFlows.createReplyableFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "replyableCmd", wrapper::wrapReceiver);

		return result;
	}
	
	@Bean
	public Subscriber<ByteBuffer> replyableCommandSender(
			Channel channel,
			CommunicationWrapper<ByteBuffer> wrapper
			) throws IOException {
			//@Value("commandExchange") String commandExchange) throws IOException {
		Subscriber<ByteBuffer> result = RabbitMqFlows.createFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, wrapper::wrapSender);
		return result;
		
		//return //RabbitMqFlows.createReplyableFanoutSender(channel, exchangeName, transformer)
	}
}