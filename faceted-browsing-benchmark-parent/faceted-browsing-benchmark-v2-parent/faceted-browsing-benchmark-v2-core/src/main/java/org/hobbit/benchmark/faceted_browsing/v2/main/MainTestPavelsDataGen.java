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
import org.hobbit.benchmark.faceted_browsing.config.DockerServiceFactoryDockerClient;
import org.hobbit.benchmark.faceted_browsing.encoder.ConfigEncodersFacetedBrowsing;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.component.ServiceNoOp;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.ServiceSpringApplicationBuilder;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;

public class MainTestPavelsDataGen {
	private static final Logger logger = LoggerFactory.getLogger(MainTestPavelsDataGen.class);

	
	public static void main(String[] args) throws Exception {
		try (DockerServiceFactory<?> dsf = DockerServiceFactoryDockerClient.create(true, Collections.emptyMap(), Collections.emptySet())) {
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
			
			try {
				amqpService.startAsync().awaitRunning(10, TimeUnit.SECONDS);
	
				String amqpHost = amqpService.getContainerId();
				
				logger.info("AMQP server started and online at " + amqpHost);
				
				DockerService dbService = ComponentUtils.wrapSparqlServiceWithHealthCheck(
						dsf.create("git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/linkedgeodata-20180719-germany-building",
		//				dsf.create("tenforce/virtuoso",
								ImmutableMap.<String, String>builder()
								.put("SPARQL_UPDATE", "true")
								.build()),
						8890);
		
				try {
					dbService.startAsync().awaitRunning(60, TimeUnit.SECONDS);
					String sparqlApiBase = "http://" + dbService.getContainerId() + ":8890/";
					String sparqlEndpoint = sparqlApiBase + "sparql";
					
					logger.info("Sparql endpoint online at " + sparqlEndpoint);
			
					RDFConnection conn = RDFConnectionFactory.connect(sparqlEndpoint);
					// Set up a flow that transform insert requests of a collection of quads into
					// corresponding update requests
					PublishProcessor<Collection<Quad>> quadsInserter = PublishProcessor.create();
			
					// ... thereby remove old records once the data grows too large
					int expectedModelSize = 3;
					SimpleSparqlInsertRequestFactory insertHandler = new SimpleSparqlInsertRequestFactoryWindowedInMemory(expectedModelSize);
			
					quadsInserter
						//.map(SetDatasetGraph::new)
						.map(insertHandler::createUpdateRequest)
			//			.doOnNext(ur -> UpdateRequestUtils.applyWithIri(ur, "http://www.example.org/"))
			//			.doOnNext(ur -> System.out.println("Request: " + ur))
			//			.doOnNext(conn::update)
						.subscribe(ur -> {
							System.out.println("Request: " + ur);
							try {
								conn.update(ur);
							} catch(Exception e) {
								 logger.warn("Failed request", e);
							}
						}, t -> logger.warn("Failed update: " + t));
			
					
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
			
			//		HOUSES_COUNT=1
			//				DEVICES_PER_HOUSEHOLD_MIN=1
			//				DEVICES_PER_HOUSEHOLD_MAX=10
			//				SENSORS_PER_DEVICE=10
			//				ITERATIONS_LIMIT=10
			//				DATA_SENDING_PERIOD_MS=1000
			//				OUTPUT_FORMAT=RDF
			//				SPARQL_ENDPOINT_URL=http://10.67.1.30:8890/sparql
			
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
				//		flow.onne
				
						
				
						logger.info("TG started - obtained receiver " + flow);
						flow.subscribe(msg -> {
				//			System.out.println("Got message:");
				//			System.out.println(new String(msg.array(), StandardCharsets.UTF_8));	
				//			System.out.println();
				//			System.out.println();
				
							Model m = ModelFactory.createDefaultModel();
							RDFDataMgr.read(m, new ByteArrayInputStream(msg.array()), null, Lang.NTRIPLES);
							System.out.println("Got model with " + m.size() + " triples");
				
							// Convert the model to quads
							Dataset ds = DatasetFactory.createGeneral();
							ds.addNamedModel("http://www.example.org/", m);
							
							List<Quad> quads = Lists.newArrayList(ds.asDatasetGraph().find());
							quadsInserter.onNext(quads);
							
						});//RabbitMQUtils.readModel(msg.duplicate())));
						//flow.subscribe(msg -> System.out.println("Got message"));
						
						logger.info("DG starting ...");
						try {
							dgService.startAsync().awaitRunning(10, TimeUnit.SECONDS);
							
							
							logger.info("DG started");
										
							Subscriber<ByteBuffer> commandSender = (Subscriber<ByteBuffer>)tgService.getAppBuilder().context().getBean("commandSender");	
					
					        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATOR_START_SIGNAL}));
					
							
							logger.info("DG termination awaited");
							dgService.awaitTerminated(30, TimeUnit.SECONDS);

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
