package org.hobbit.benchmark.faceted_browsing.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.aksw.commons.service.core.BeanWrapperService;
import org.hobbit.core.Constants;
import org.hobbit.core.config.CommunicationWrapper;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.hobbit.core.service.docker.DockerServiceManagerServerComponent;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceBuilder;
import org.hobbit.core.service.docker.api.DockerServiceFactory;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderJsonDelegate;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.spotify.docker.client.exceptions.DockerCertificateException;

import io.reactivex.Flowable;

public class ConfigDockerServiceManagerServer {
	
	// TODO: Make use of a docker service factory
	
	@Bean
	public Flowable<SimpleReplyableMessage<ByteBuffer>> dockerServiceManagerConnectionServer(
			Channel channel, 
			CommunicationWrapper<ByteBuffer> wrapper) throws IOException, TimeoutException {
		Flowable<SimpleReplyableMessage<ByteBuffer>> result = RabbitMqFlows.createReplyableFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "dockerServiceManagerServerComponent", wrapper::wrapReceiver);
				//.doOnNext(x -> System.out.println("[STATUS] Received request; " + Arrays.toString(x.getValue().array()) + " replier: " + x.getReplyConsumer()));
		return result;
	}

	@Bean
	public BeanWrapperService<?> dockerServiceManagerServer(
		//Supplier<? extends DockerServiceBuilder<? extends DockerService>> delegateSupplier,
		@Qualifier("commandReceiver") Flowable<ByteBuffer> commandReceiver,
		@Qualifier("commandSender") Subscriber<ByteBuffer> commandSender,
		@Qualifier("dockerServiceManagerConnectionServer") Flowable<SimpleReplyableMessage<ByteBuffer>> requestsFromClients,
		DockerServiceFactory<?> dockerServiceFactory,
		Gson gson
	) throws DockerCertificateException {
        
        // Create a supplier that yields preconfigured builders
        Supplier<DockerServiceBuilder<? extends DockerService>> builderSupplier = () -> {
        	DockerServiceBuilder<?> r = DockerServiceBuilderJsonDelegate.create(dockerServiceFactory::create);
        	return r;
        };
        
        
        DockerServiceManagerServerComponent service =
        		new DockerServiceManagerServerComponent(
        				builderSupplier,
        				commandSender,
        				commandReceiver,
        				requestsFromClients,
        				gson        				
        				);
        //result.startAsync().awaitRunning();

        return new BeanWrapperService<>(service);
        
        //return result;
	}
	
}