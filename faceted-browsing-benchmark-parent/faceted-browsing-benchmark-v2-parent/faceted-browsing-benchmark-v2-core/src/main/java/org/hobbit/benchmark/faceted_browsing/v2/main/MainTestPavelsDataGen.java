package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Collections;

import org.hobbit.benchmark.faceted_browsing.config.ComponentUtils;
import org.hobbit.benchmark.faceted_browsing.config.DockerServiceFactoryDockerClient;
import org.hobbit.benchmark.faceted_browsing.config.ServiceSpringApplicationBuilder;
import org.hobbit.benchmark.faceted_browsing.encoder.ConfigEncodersFacetedBrowsing;
import org.hobbit.core.Constants;
import org.hobbit.core.component.ServiceNoOp;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmarkMocha;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;
import com.spotify.docker.client.exceptions.DockerCertificateException;

public class MainTestPavelsDataGen {
	private static final Logger logger = LoggerFactory.getLogger(MainTestPavelsDataGen.class);

	
	public static void main(String[] args) throws DockerCertificateException {
		DockerServiceFactory<?> dsf = DockerServiceFactoryDockerClient.create(true, Collections.emptyMap(), Collections.emptySet());
		
		Service amqpService = new ServiceSpringApplicationBuilder("qpid-server", new SpringApplicationBuilder()
				// Add the amqp broker
				.properties(new ImmutableMap.Builder<String, Object>()
						.put("hostMode", true)
						.put(Constants.HOBBIT_SESSION_ID_KEY, "testsession" + "." + RabbitMqFlows.idGenerator.get())
						//.put(ConfigRabbitMqConnectionFactory.AMQP_VHOST, "default")
						.build())
				.sources(ConfigQpidBroker.class)
				.sources(ServiceNoOp.class))
				;

		amqpService.startAsync().awaitRunning();
		
		logger.info("AMQP server started");
		
		DockerService dbService = ComponentUtils.wrapSparqlServiceWithHealthCheck(
				dsf.create("git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/linkedgeodata-20180719-germany-building",
						ImmutableMap.<String, String>builder().build()
				), 8890);

		dbService.startAsync().awaitRunning();
		String sparqlEndpoint = "http://" + dbService.getContainerId() + ":8890/sparql";
		
		logger.info("Sparql endpoint online at " + sparqlEndpoint);
		
		
		
		
		DockerService dgService = dsf.create("git.project-hobbit.eu:4567/smirnp/grow-smarter-benchmark/datagen", ImmutableMap.<String, String>builder()
				.put(Constants.RABBIT_MQ_HOST_NAME_KEY, "localhost")
				.put(Constants.HOBBIT_SESSION_ID_KEY, "local-test-session")
				.put(Constants.DATA_QUEUE_NAME_KEY, "my queue")
				.put("HOUSES_COUNT", "1000")
				.put("DEVICES_PER_HOUSEHOLD_MIN", "5")
				.put("DEVICES_PER_HOUSEHOLD_MAX", "20")
				.put("SENSORS_PER_DEVICE", "5")
				.put("ITERATIONS_LIMIT", "1000")
				.put("DATA_SENDING_PERIOD_MS", "1000")
				.put("SPARQL_ENDPOINT_URL", sparqlEndpoint)
				.build());


		ComponentUtils.createComponentBaseConfig("tg", Constants.CONTAINER_TYPE_BENCHMARK)
			.child(ConfigEncodersFacetedBrowsing.class) //, ConfigTaskGenerator.class, ConfigTaskGeneratorFacetedBenchmark.class)
				.child(TaskGeneratorFacetedBenchmarkMocha.class)
				.run();
			

		
		dgService.startAsync().awaitTerminated();
	}
}
