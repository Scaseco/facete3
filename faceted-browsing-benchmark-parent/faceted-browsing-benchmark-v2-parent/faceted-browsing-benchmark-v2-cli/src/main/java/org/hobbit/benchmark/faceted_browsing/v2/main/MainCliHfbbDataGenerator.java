package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.hobbit.benchmark.faceted_browsing.config.ComponentUtils;
import org.hobbit.benchmark.faceted_browsing.config.ConfigTaskGenerator;
import org.hobbit.benchmark.faceted_browsing.encoder.ConfigEncodersFacetedBrowsing;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.component.ServiceNoOp;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceFactory;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;
import org.hobbit.core.service.docker.impl.spring_boot.ServiceSpringApplicationBuilder;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;

/**
 * 
 * 
 * @author Claus Stadler, Feb 18, 2019
 */
public class MainCliHfbbDataGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(MainCliHfbbDataGenerator.class);

	
	public static void main(String[] args) throws Exception {
		try (DockerServiceFactory<?> dsf = DockerServiceSystemDockerClient.create(true, Collections.emptyMap(), Collections.emptySet())) {
			// Create a session id (used in naming of the amqp communication
			// channels to avoid conflicts between different sessions)
			String sessionId = "testsession" + "." + RabbitMqFlows.idGenerator.get();
	
			// Configure the AMQP server
			DockerService amqpService = dsf
					.create("git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/hobbit-sdk-qpid7",
							ImmutableMap.<String, String>builder().build());
	
			logger.info("AMQP server starting ...");
			
			try {
				// Start it and obtain the host name
				amqpService.startAsync().awaitRunning(10, TimeUnit.SECONDS);
				String amqpHost = amqpService.getContainerId();
				
				logger.info("AMQP server started and online at " + amqpHost);
				
				// Set up the container for the preloaded SPARQL enpdoint
				// Also, add a health check to ensure that the 'runnig' state means that
				// the SPARQL enppoint is up - rather than only the container
				DockerService dbService = ComponentUtils.wrapSparqlServiceWithHealthCheck(
						dsf.create("git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/linkedgeodata-20180719-germany-building",
								ImmutableMap.<String, String>builder()
								.put("SPARQL_UPDATE", "true")
								.build()),
						8890);
		
				try {
					// Start up the SPARQL endpoint
					dbService.startAsync().awaitRunning(60, TimeUnit.SECONDS);
					String sparqlApiBase = "http://" + dbService.getContainerId() + ":8890/";
					String sparqlEndpoint = sparqlApiBase + "sparql";
					
					logger.info("Sparql endpoint online at " + sparqlEndpoint);
			
					// Configure a connection to the SPARQL endpoint
					RDFConnection conn = RDFConnectionFactory.connect(sparqlEndpoint);

					// Set up a flow that transforms SPARQL insert requests of a collection
					// of quads into corresponding update requests
					PublishProcessor<Collection<Quad>> quadsInserter = PublishProcessor.create();
			
					// ... thereby remove old records once the data grows too large
					int expectedModelSize = 3;

					SimpleSparqlInsertRequestFactory insertHandler = new SimpleSparqlInsertRequestFactoryWindowedInMemory(expectedModelSize);
			
					Disposable disposable = quadsInserter
						.map(insertHandler::createUpdateRequest)
						.subscribe(ur -> {
							System.out.println("Request: " + ur);
							try {
								conn.update(ur);
							} catch(Exception e) {
								 logger.warn("Failed request", e);
							}
						}, t -> logger.warn("Failed update: " + t));
			
					
					// Configure the data generator container
					DockerService dgService = dsf.create("git.project-hobbit.eu:4567/smirnp/grow-smarter-benchmark/datagen", ImmutableMap.<String, String>builder()
							.put(Constants.RABBIT_MQ_HOST_NAME_KEY, amqpHost)
							.put(Constants.HOBBIT_SESSION_ID_KEY, sessionId)
							.put(Constants.DATA_QUEUE_NAME_KEY, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME)
							.put(Constants.GENERATOR_ID_KEY, "1")
							.put(Constants.GENERATOR_COUNT_KEY, "1")
							.put("HOUSES_COUNT", "1")
							.put("DEVICES_PER_HOUSEHOLD_MIN", "1")
							.put("DEVICES_PER_HOUSEHOLD_MAX", "10")
							.put("SENSORS_PER_DEVICE", "10")
							.put("ITERATIONS_LIMIT", "10")
							.put("DATA_SENDING_PERIOD_MS", "1000")
							.put("OUTPUT_FORMAT", "RDF")
							.put("SPARQL_ENDPOINT_URL", sparqlEndpoint)
							.build());
			
					// Create a dummy task generator image
					// Note: ServiceNoOp is a service that does nothing by itself - however
					// the life cycle of the spring context is bound to that of the service
					// Hence, stopping the service tears down the context and consequently
					// frees all associated resources, such as AMQP connections
					ServiceSpringApplicationBuilder tgService = new ServiceSpringApplicationBuilder("tg", ComponentUtils.createComponentBaseConfig("tg", Constants.CONTAINER_TYPE_BENCHMARK)
							.properties(ImmutableMap.<String, Object>builder()
									.put(Constants.RABBIT_MQ_HOST_NAME_KEY, amqpHost)
									.put(Constants.HOBBIT_SESSION_ID_KEY, sessionId)
									.build())
							.child(ConfigEncodersFacetedBrowsing.class, ConfigTaskGenerator.class) // ConfigTaskGeneratorFacetedBenchmark.class)
							.child(ServiceNoOp.class));
					
					logger.info("TG starting ...");
					try {
						tgService.startAsync().awaitRunning(10, TimeUnit.SECONDS);
						
						Flowable<ByteBuffer> flow = (Flowable<ByteBuffer>)tgService.getAppBuilder().context().getBean("dg2tgReceiver");	
						System.out.println("Flow " + flow);
				
						logger.info("TG started - obtained receiver " + flow);
						flow.subscribe(msg -> {
							// Parse messages as RDF models and pass them to the inserter
							
							Model m = ModelFactory.createDefaultModel();
							RDFDataMgr.read(m, new ByteArrayInputStream(msg.array()), null, Lang.NTRIPLES);
							System.out.println("Got model with " + m.size() + " triples");
				
							// Convert the model to quads
							Dataset ds = DatasetFactory.createGeneral();
							ds.addNamedModel("http://www.example.org/", m);
							
							List<Quad> quads = Lists.newArrayList(ds.asDatasetGraph().find());
							quadsInserter.onNext(quads);
							
						});

						// Start the data generator
						logger.info("DG starting ...");
						try {
							dgService.startAsync().awaitRunning(10, TimeUnit.SECONDS);
							
							
							logger.info("DG started");
										
							// Obtain the command sender from the spring context ...
							Subscriber<ByteBuffer> commandSender = (Subscriber<ByteBuffer>)tgService.getAppBuilder().context().getBean("commandSender");	
					
							// ... and send the the command to start data generation
					        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATOR_START_SIGNAL}));
												        
							logger.info("DG termination awaited");
							dgService.awaitTerminated(30, TimeUnit.SECONDS);

							// Done - tear down everything in order
							// E.g. the amqp server has to shut down last, so that components
							// can inform each other about them shutting down

						} finally {
							dgService.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
						}
					} finally {
						tgService.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
					}
				} finally {
					dbService.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
				}
			} finally {
				amqpService.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
			}
		}

		logger.info("Done");
	}
}
