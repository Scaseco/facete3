package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.nio.ByteBuffer;
import java.util.Collections;

import org.hobbit.benchmark.faceted_browsing.config.ComponentUtils;
import org.hobbit.benchmark.faceted_browsing.config.ConfigTaskGenerator;
import org.hobbit.benchmark.faceted_browsing.config.DockerServiceFactoryDockerClient;
import org.hobbit.benchmark.faceted_browsing.encoder.ConfigEncodersFacetedBrowsing;
import org.hobbit.core.Constants;
import org.hobbit.core.component.ServiceNoOp;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.ServiceSpringApplicationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.exceptions.DockerCertificateException;

import io.reactivex.Flowable;

public class MainTestPavelsDataGen {
	private static final Logger logger = LoggerFactory.getLogger(MainTestPavelsDataGen.class);

	
	public static void main(String[] args) throws DockerCertificateException {
		DockerServiceFactory<?> dsf = DockerServiceFactoryDockerClient.create(true, Collections.emptyMap(), Collections.emptySet());
		
		String amqpHost = "localhost";
		String amqpFromContainer = "10.128.128.128";
		String sessionId = "testsession" + "." + RabbitMqFlows.idGenerator.get();

		
		DockerService amqpService = dsf.create("git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/hobbit-sdk-qpid7",
						ImmutableMap.<String, String>builder().build());
//		Service amqpService = new ServiceSpringApplicationBuilder("qpid-server", new SpringApplicationBuilder()
//				// Add the amqp broker
//				.properties(new ImmutableMap.Builder<String, Object>()
//						.put("hostMode", true)
//						.put(Constants.HOBBIT_SESSION_ID_KEY, sessionId)
//						//.put(ConfigRabbitMqConnectionFactory.AMQP_VHOST, "default")
//						.build())
//				.sources(ConfigQpidBroker.class)
//				.sources(ServiceNoOp.class))
//				;

		logger.info("AMQP server starting ...");
		amqpService.startAsync().awaitRunning();
		
		logger.info("AMQP server started");
		
		DockerService dbService = ComponentUtils.wrapSparqlServiceWithHealthCheck(
				dsf.create("git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/linkedgeodata-20180719-germany-building",
						ImmutableMap.<String, String>builder().build()),
				8890);

		dbService.startAsync().awaitRunning();
		String sparqlEndpoint = "http://" + dbService.getContainerId() + ":8890/sparql";
		
		logger.info("Sparql endpoint online at " + sparqlEndpoint);

		
		DockerService dgService = dsf.create("git.project-hobbit.eu:4567/smirnp/grow-smarter-benchmark/datagen", ImmutableMap.<String, String>builder()
				.put(Constants.RABBIT_MQ_HOST_NAME_KEY, amqpFromContainer)
				.put(Constants.HOBBIT_SESSION_ID_KEY, sessionId)
				.put(Constants.DATA_QUEUE_NAME_KEY, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME)
				.put("HOUSES_COUNT", "1000")
				.put("DEVICES_PER_HOUSEHOLD_MIN", "5")
				.put("DEVICES_PER_HOUSEHOLD_MAX", "20")
				.put("SENSORS_PER_DEVICE", "5")
				.put("ITERATIONS_LIMIT", "1000")
				.put("DATA_SENDING_PERIOD_MS", "1000")
				.put("SPARQL_ENDPOINT_URL", sparqlEndpoint)
				.build());


		ServiceSpringApplicationBuilder tgService = new ServiceSpringApplicationBuilder("tg", ComponentUtils.createComponentBaseConfig("tg", Constants.CONTAINER_TYPE_BENCHMARK)
				.properties(ImmutableMap.<String, Object>builder()
						.put(Constants.RABBIT_MQ_HOST_NAME_KEY, amqpHost)
						.put(Constants.HOBBIT_SESSION_ID_KEY, sessionId)
						.build())
				.child(ConfigEncodersFacetedBrowsing.class, ConfigTaskGenerator.class) // ConfigTaskGeneratorFacetedBenchmark.class)
				.child(ServiceNoOp.class));
		
		logger.info("TG starting ...");
		tgService.startAsync().awaitRunning();

		Flowable<ByteBuffer> flow = (Flowable<ByteBuffer>)tgService.getAppBuilder().context().getBean("dg2tgReceiver");	

		logger.info("TG started - obtained receiver " + flow);
		flow.subscribe(msg -> System.out.println("Got message: " + RabbitMQUtils.readString(msg)));
		
		logger.info("DG starting ...");
		dgService.startAsync().awaitRunning();
		
		
		logger.info("DG started");
		
		
		logger.info("TG termination awaited");
		dgService.awaitTerminated();
		
		logger.info("Done");
	}
}
