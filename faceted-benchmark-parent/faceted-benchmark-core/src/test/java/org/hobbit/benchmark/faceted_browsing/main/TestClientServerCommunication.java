package org.hobbit.benchmark.faceted_browsing.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigCommunicationWrapper;
import org.hobbit.core.Constants;
import org.hobbit.core.config.CommunicationWrapper;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.junit.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.reactivex.Flowable;

public class TestClientServerCommunication {
	
	protected static final String commandExchange = Constants.HOBBIT_COMMAND_EXCHANGE_NAME;

	public static class CommonContext {
		@Bean
		public Connection connection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
			return connectionFactory.newConnection();
		}

		@Bean
		public Channel channel(Connection connection) throws IOException {
			return connection.createChannel();
		}
	}
	
	@Import(CommonContext.class)
	public static class ServerContext {
		@Bean
		public Flowable<SimpleReplyableMessage<ByteBuffer>> serverRequests(Channel channel, CommunicationWrapper<ByteBuffer> wrapper) throws IOException, TimeoutException {
			Flowable<SimpleReplyableMessage<ByteBuffer>> result = RabbitMqFlows.createReplyableFanoutReceiver(channel, commandExchange, "server", wrapper::wrapReceiver);

			// TODO Ideally move the handler somewhere else
			result.subscribe(x -> {
				ByteBuffer b = x.getValue().duplicate();
				//System.out.println("[STATUS] Received request; " + Arrays.toString(b.array()) + " replier: " + x.getReplyConsumer());
				
				x.getReplyConsumer().accept(ByteBuffer.wrap(("Hello: " + new String(b.array(), StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8)));
			});
			
			return result;
		}
	}
	
	@Import(CommonContext.class)
	public static class ClientContext {
		@Bean
		public Function<ByteBuffer, CompletableFuture<ByteBuffer>> client(Channel channel, CommunicationWrapper<ByteBuffer> wrapper) throws IOException, TimeoutException {
			return RabbitMqFlows.createReplyableFanoutSender(channel, commandExchange, "client", wrapper::wrapSender, x -> Collections.singletonList(x));//wrapper::wrapReceiver);
		}
	}

	public static class AppContext {
		@Bean
		public ApplicationRunner appRunner(Function<ByteBuffer, CompletableFuture<ByteBuffer>> client) {
			return (args) -> {
				Function<String, String> sendRequest = s -> {
					ByteBuffer b;
					try { 
						ByteBuffer a = ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));
						b = client.apply(a).get();
					} catch(Exception e) {
						throw new RuntimeException(e);
					}
					String r = new String(b.array(), StandardCharsets.UTF_8);
					return r;
				};

				System.out.println("[STATUS] Received final response: " + sendRequest.apply("Anne"));
				System.out.println("[STATUS] Received final response: " + sendRequest.apply("Bob"));
			};
		}
	}

	@Test
	public void testDockerCommunication() {
		
		// NOTE The broker shuts down when the context is closed
		
		SpringApplicationBuilder builder = new SpringApplicationBuilder()
				.properties(new ImmutableMap.Builder<String, Object>()
						.put("hostMode", true)
						.put(Constants.HOBBIT_SESSION_ID_KEY, "testsession" + "." + RabbitMqFlows.idGenerator.get())
						.build())
				.sources(ConfigQpidBroker.class)
					.child(ConfigCommunicationWrapper.class, ConfigRabbitMqConnectionFactory.class, ServerContext.class)
					.sibling(ConfigCommunicationWrapper.class, ConfigRabbitMqConnectionFactory.class, ClientContext.class, AppContext.class);

		try(ConfigurableApplicationContext ctx = builder.run()) {}
	}

//	@Test
//	public void testClientServerCommunication() {		
//		try(
//				ConfigurableApplicationContext ctxBroker = new SpringApplicationBuilder(ConfigQpidBroker.class).run();
//				ConfigurableApplicationContext ctxServer = new SpringApplicationBuilder(ConfigRabbitMqConnectionFactory.class, ServerContext.class).run();
//				ConfigurableApplicationContext ctxClient = new SpringApplicationBuilder(ConfigRabbitMqConnectionFactory.class, ClientContext.class, AppContext.class).run()) {
//		}
//	}

}
