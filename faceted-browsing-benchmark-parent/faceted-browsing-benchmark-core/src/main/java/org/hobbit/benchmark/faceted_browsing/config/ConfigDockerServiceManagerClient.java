package org.hobbit.benchmark.faceted_browsing.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.aksw.commons.service.core.BeanWrapperService;
import org.hobbit.core.Constants;
import org.hobbit.core.config.CommunicationWrapper;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.service.docker.DockerServiceManagerClientComponent;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceBuilder;
import org.hobbit.core.service.docker.api.DockerServiceFactory;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderFactory;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderJsonDelegate;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;

import io.reactivex.Flowable;

public class ConfigDockerServiceManagerClient
	implements EnvironmentAware
{		
	private static final Logger logger = LoggerFactory
			.getLogger(ConfigDockerServiceManagerClient.class);

	
	protected Environment env;

	
	@Bean
	public Function<ByteBuffer, CompletableFuture<ByteBuffer>> dockerServiceManagerConnectionClient(
			Channel channel,
			CommunicationWrapper<ByteBuffer> wrapper
	) throws IOException, TimeoutException {
		
		
		return RabbitMqFlows.createReplyableFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "dockerServiceManagerClient", wrapper::wrapSender, x -> Collections.singletonList(x)); //wrapper::wrapReceiver);
	}

	
	//@Bean(initMethod="startUp", destroyMethod="shutDown")
	//public DockerServiceManagerClientComponent dockerServiceManagerClientCore(
	@Bean
	public BeanWrapperService<DockerServiceManagerClientComponent> dockerServiceManagerClientCore(
			@Qualifier("commandReceiver") Flowable<ByteBuffer> commandReceiver,
			@Qualifier("commandSender") Subscriber<ByteBuffer> commandSender,
			@Qualifier("dockerServiceManagerConnectionClient") Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer,
			Gson gson,
            @Value("${" + Constants.CONTAINER_NAME_KEY + ":no-requester-container-id-set}") String requesterContainerId,
            @Value("${" + ComponentUtils.DEFAULT_REQUESTED_CONTAINER_TYPE_KEY + ":no-default-requested-container-type-set}") String defaultRequestedContainerType				
	) throws Exception {			
		
		DockerServiceManagerClientComponent core =
				new DockerServiceManagerClientComponent(
						commandReceiver,
						commandSender,
						requestToServer,
						gson,
						requesterContainerId,
						defaultRequestedContainerType
				);
		
		BeanWrapperService<DockerServiceManagerClientComponent> result = new BeanWrapperService<>(core);
		return result;
	}
		
	//ServiceDelegate<DockerServiceManagerClientComponent>
	@Bean
	public DockerServiceBuilderFactory<?> dockerServiceManagerClient(
			//DockerServiceManagerClientComponent core
			BeanWrapperService<DockerServiceManagerClientComponent> tmp,
            @Value("${" + Constants.HOBBIT_SESSION_ID_KEY + ":" + Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS + "}") String hobbitSessionId,                
            @Value("${" + Constants.RABBIT_MQ_HOST_NAME_KEY + ":localhost}") String amqpHost                
			
	) throws Exception {
		DockerServiceManagerClientComponent tmpCore = tmp.getService();
		
		// Apply client side service wrappers
		DockerServiceFactory<?> core = ComponentUtils.applyServiceWrappers(tmpCore);
		
		DockerServiceBuilderFactory<DockerServiceBuilder<DockerService>> result =
				() -> {
		            //envVariables[envVariables.length - 2] = Constants.RABBIT_MQ_HOST_NAME_KEY + "=" + rabbitMQHostName;
		            //envVariables[envVariables.length - 1] = Constants.HOBBIT_SESSION_ID_KEY + "=" + getHobbitSessionId();

					DockerServiceBuilderJsonDelegate<DockerService> r = DockerServiceBuilderJsonDelegate.create(core::create);
					//r.getBaseEnvironment().put(Constants.HOBBIT_SESSION_ID_KEY, env.getProperty(Constants.HOBBIT_SESSION_ID_KEY, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS));
					r.getBaseEnvironment().put(Constants.HOBBIT_SESSION_ID_KEY, hobbitSessionId);
                    r.getBaseEnvironment().put(Constants.RABBIT_MQ_HOST_NAME_KEY, amqpHost);
					
					
					logger.info("Prepared docker service builder with base configuration: " + r.getBaseEnvironment());
					
					return r;
				};
		
		return result;
	}


	@Override
	public void setEnvironment(Environment environment) {
		this.env = environment;
	}
}