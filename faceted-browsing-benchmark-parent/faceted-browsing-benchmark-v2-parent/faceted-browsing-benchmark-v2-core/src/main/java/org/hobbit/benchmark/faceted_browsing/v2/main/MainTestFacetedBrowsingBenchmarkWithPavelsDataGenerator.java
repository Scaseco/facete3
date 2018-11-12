package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.core.utils.DatasetGraphQuadsImpl;
import org.aksw.jena_sparql_api.core.utils.RDFDataMgrRx;
import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoSystemService;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.impl.bidirectional.ConceptPathFinderSystemBidirectional;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.util.iterator.ClosableIterator;
import org.hobbit.benchmark.faceted_browsing.config.ComponentUtils;
import org.hobbit.benchmark.faceted_browsing.config.ConfigTaskGenerator;
import org.hobbit.benchmark.faceted_browsing.config.DockerServiceFactoryDockerClient;
import org.hobbit.benchmark.faceted_browsing.encoder.ConfigEncodersFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
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

import com.github.davidmoten.rx2.flowable.Transformers;
import com.github.jsonldjava.shaded.com.google.common.collect.ImmutableMap;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;

/**
 * This class eventually launches a data generator (DG) and processes the emitted messages.
 * Setting of the DG requires - among other things -
 * launching an AMQP server, a SPARQL endpoint, and a task generator to receive the messages.
 * 
 * 
 * @param args
 * @throws Exception
 */
public class MainTestFacetedBrowsingBenchmarkWithPavelsDataGenerator {
	private static final Logger logger = LoggerFactory.getLogger(MainTestFacetedBrowsingBenchmarkWithPavelsDataGenerator.class);

	
	// Substitute a space in e.g. 2018-10-30 09:41:53 with T - i.e. 2018-10-30T09:41:53
	public static String substituteSpaceWithTInTimestamps(String str) {
		Pattern p = Pattern.compile("(\\d+-\\d{1,2}-\\d{1,2}) (\\d{1,2}:\\d{1,2}:\\d{1,2})");
		String result = p.matcher(str).replaceAll("$1T$2");
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		testPathFinder();
	}
	
	public static void testPathFinder() {
		Dataset raw = DatasetFactory.create();
		RDFDataMgrRx.createFlowableDatasets(
			() -> new FileInputStream("/home/raven/Projects/Data/Hobbit/hobbit-sensor-stream-150k.trig"),
			Lang.TRIG,
			"http://www.example.org/")
		.limit(10)
		.forEach(d -> Streams.stream(d.asDatasetGraph().find()).forEach(raw.asDatasetGraph()::add));
			
		Dataset ds = DatasetFactory.wrap(raw.getUnionModel());
		
		
		RDFConnection dataConnection = RDFConnectionFactory.connect(ds);
		
		// Set up a path finding system
		ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();
		
			

		// Use the system to compute a data summary
		// Note, that the summary could be loaded from any place, such as a file used for caching
		Model dataSummary = system.computeDataSummary(dataConnection).blockingGet();
		
		RDFDataMgr.write(System.out, dataSummary, RDFFormat.TURTLE_PRETTY);
		
		// Build a path finder; for this, first obtain a factory from the system
		// set its attributes and eventually build the path finder.
		ConceptPathFinder pathFinder = system.newPathFinderBuilder()
			.setDataSummary(dataSummary)
			.setDataConnection(dataConnection)
			.setShortestPathsOnly(false)
			.build();
				
		
		//Concept.parse("?s | ?s ?p [ a eg:D ]", PrefixMapping.Extended),
		
		// Create search for paths between two given sparql concepts
		PathSearch<SimplePath> pathSearch = pathFinder.createSearch(
			Concept.parse("?s | ?s a <http://www.agtinternational.com/ontologies/lived#CurrentObservation>", PrefixMapping.Extended),
			Concept.parse("?s | ?s <http://www.w3.org/ns/ssn/#hasValue> ?o", PrefixMapping.Extended)
		);
			//Concept.parse("?s | ?s a eg:A", PrefixMapping.Extended));
		
		// Set parameters on the search, such as max path length and the max number of results
		// Invocation of .exec() executes the search and yields the flow of results
		List<SimplePath> actual = pathSearch
				.setMaxLength(7)
				//.setMaxResults(100)
				.exec()
				.toList().blockingGet();

		System.out.println("Paths");
		actual.forEach(System.out::println);
	}
	
	public static void performTestRun() throws Exception {
		String excerptQuery = "PREFIX lgdo: <http://linkedgeodata.org/ontology/>\n" + 
				"PREFIX ogc: <http://www.opengis.net/ont/geosparql#>\n" + 
				"PREFIX geom: <http://geovocab.org/geometry#>\n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
				"CONSTRUCT {\n" + 
				"  ?s a lgdo:BuildingResidential ; geom:geometry ?g .\n" +
				"  ?g ogc:asWKT ?w .\n" + 
				"  ?s rdfs:label ?l .\n" +
				"}\n" +
				"{\n" + 
				"  ?s a lgdo:BuildingResidential ; geom:geometry ?g .\n" +
				"  ?g ogc:asWKT ?w .\n" + 
				"  OPTIONAL { ?s rdfs:label ?l }\n" +
				"}";

//		String excerptQuery = "PREFIX lgdo: <http://linkedgeodata.org/ontology/>\n" + 
//				"PREFIX ogc: <http://www.opengis.net/ont/geosparql#>\n" + 
//				"PREFIX geom: <http://geovocab.org/geometry#>\n" + 
//				"SELECT * {\n" + 
//				"  ?s a lgdo:BuildingResidential ; geom:geometry ?g .\n" + 
//				"  ?g ogc:asWKT ?w .\n" + 
//				"}";

		
		System.out.println(excerptQuery);

		try (DockerServiceFactory<?> dsf = DockerServiceFactoryDockerClient.create(true, Collections.emptyMap(), Collections.emptySet())) {
			// Create a session id (used in naming of the amqp communication
			// channels to avoid conflicts between different sessions)
			String sessionId = "testsession" + "." + RabbitMqFlows.idGenerator.get();
	
			// Configure the AMQP server
			DockerService amqpService =dsf
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
				
				// https://github.com/openlink/virtuoso-opensource/issues/119
//				DockerService dbService = ComponentUtils.wrapSparqlServiceWithHealthCheck(
//						dsf.create("git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/linkedgeodata-20180719-germany-building",
//								ImmutableMap.<String, String>builder()
//								.put("SPARQL_UPDATE", "true")
//								.put("VIRT_SPARQL_ResultSetMaxRows", "1000000000")
//								.put("VIRT_SPARQL_MaxQueryCostEstimationTime", "0")
//								.put("VIRT_SPARQL_MaxQueryExecutionTime", "600")
//								.put("VIRT_Parameters_MaxVectorSize", "1000000000")
//								.build()),
//						8890);
		
				DockerService dbService = ComponentUtils.wrapSparqlServiceWithHealthCheck(
						dsf.create("tenforce/virtuoso",
								ImmutableMap.<String, String>builder()
								.put("SPARQL_UPDATE", "true")
								.put("VIRT_Parameters_NumberOfBuffers", "170000")
								.put("VIRT_Parameters_MaxDirtyBuffers", "130000")
								.put("VIRT_Parameters_MaxVectorSize", "1000000000")
								.put("VIRT_SPARQL_ResultSetMaxRows", "1000000000")
								.put("VIRT_SPARQL_MaxQueryCostEstimationTime", "0")
								.put("VIRT_SPARQL_MaxQueryExecutionTime", "600")
								.build()),
						8890);
				
				try {
					// Start up the SPARQL endpoint
					dbService.startAsync().awaitRunning(60, TimeUnit.SECONDS);
					String host = dbService.getContainerId();
					String sparqlApiBase = "http://" + host + ":8890/";
					String sparqlEndpoint = sparqlApiBase + "sparql";
					
					logger.info("Sparql endpoint online at " + sparqlEndpoint);
			
					// Configure a connection to the SPARQL endpoint
					//RDFConnection coreConn = RDFConnectionFactory.connect(sparqlEndpoint);
					
					
					RDFConnection coreConn = VirtuosoSystemService.connectVirtuoso(host, 8890, 1111);

					RDFConnection conn =
						new RDFConnectionModular(new SparqlQueryConnectionJsa(
								FluentQueryExecutionFactory
									.from(new QueryExecutionFactorySparqlQueryConnection(coreConn))
									.config()
										.withPostProcessor(x -> {
											QueryExecution qe = ((QueryExecutionDecorator)x).getDecoratee();
											((QueryEngineHTTP)qe).setSelectContentType(WebContent.contentTypeResultsXML);
										})
										.withClientSideConstruct()
										.withDatasetDescription(DatasetDescriptionUtils.createDefaultGraph("http://linkedgeodata.org"))
									.end()
									.create()
									), coreConn, coreConn);

					// Set up a flow that transforms SPARQL inseCollection<E>s of a collection
					// of quads into correspondingQuadate requests
					PublishProcessor<Collection<Quad>> quadsInserter = PublishProcessor.create();
			
					// ... thereby remove old records once the data grows too large
					int expectedModelSize = 3;

					SimpleSparqlInsertRequestFactory insertHandler = new SimpleSparqlInsertRequestFactoryWindowedInMemory(expectedModelSize);
			
					Disposable disposable = quadsInserter
						.map(insertHandler::createUpdateRequest)
						.subscribe(ur -> {
							//System.out.println("Request: " + ur);
							try {
								conn.update(ur);
							} catch(Exception e) {
								 logger.warn("Failed request", e);
							}
						}, t -> logger.warn("Failed update: " + t));
			

					boolean loadData = true;
					if(loadData) {
						Model src = RDFDataMgr.loadModel("/tmp/hobbit-lgd-residential-buildings-20180719-core-with-labels.nt");
						Dataset d = DatasetFactory.create();
						d.addNamedModel("http://linkedgeodata.org", src);
						
						List<Quad> qs = Lists.newArrayList(d.asDatasetGraph().find());
						List<List<Quad>> partitions = Lists.partition(qs, 1000);
						for(List<Quad> part : partitions) {
							//System.out.println("INSERTING:\n" + part);
							//quadsInserter.onNext(part);
							conn.update(UpdateRequestUtils.createUpdateRequest(part, null));
						}
					}						
					//conn.load("http://linkedgeodata.org", "/tmp/hobbit-lgd-residential-buildings-20180719-core.nt");
					
					
					Stopwatch sw = Stopwatch.createStarted();
					Model ex = conn.query(excerptQuery).execConstruct();
					long size = ex.size();
					//int size = ResultSetFormatter.consume(conn.query(excerptQuery).execSelect());
					sw.stop();
					System.out.println("Excerpt with " + size + " triples created in " + sw.elapsed(TimeUnit.SECONDS) + "s");
					
					
					
//					if(true) { throw new RuntimeException("Aborting"); }

					// Configure the data generator container
					DockerService dgService = dsf.create("git.project-hobbit.eu:4567/smirnp/grow-smarter-benchmark/datagen", ImmutableMap.<String, String>builder()
							.put(Constants.RABBIT_MQ_HOST_NAME_KEY, amqpHost)
							.put(Constants.HOBBIT_SESSION_ID_KEY, sessionId)
							.put(Constants.DATA_QUEUE_NAME_KEY, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME)
							.put(Constants.GENERATOR_ID_KEY, "1")
							.put(Constants.GENERATOR_COUNT_KEY, "1")
							.put("HOUSES_COUNT", "150000")
							.put("DEVICES_PER_HOUSEHOLD_MIN", "1")
							.put("DEVICES_PER_HOUSEHOLD_MAX", "10")
							.put("SENSORS_PER_DEVICE", "4")
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
						
						
						OutputStream eventOutStream = new FileOutputStream("/tmp/lgd-hobbit-out.trig");

						int[] nextEventId = {1};
						
						flow.subscribe(msg -> {
							// Parse messages as RDF models and pass them to the inserter

							String str = new String(msg.array());
							str = MainTestFacetedBrowsingBenchmarkWithPavelsDataGenerator.substituteSpaceWithTInTimestamps(str);
							
							String wrappedMsg = "<http://www.example.org/event" + nextEventId[0]++ + "> {\n" + str + "\n}\n\n";
							
							System.out.println(wrappedMsg);
							Iterable<Quad> i = () -> RDFDataMgr.createIteratorQuads(new ByteArrayInputStream(wrappedMsg.getBytes()) , Lang.TRIG, "http://www.example.org/");

							Flowable<Dataset> eventStream = Flowable.fromIterable(i)
									.compose(Transformers.<Quad>toListWhile(
								            (list, t) -> list.isEmpty() 
								                         || list.get(0).getGraph().equals(t.getGraph())))
									.map(DatasetGraphQuadsImpl::create)
									.map(DatasetFactory::wrap);
							
							eventStream.forEach(d -> {
//								System.out.println("Got event");
								RDFDataMgr.write(eventOutStream, d, RDFFormat.TRIG);
							});

							
							if(false) {
							Model m = ModelFactory.createDefaultModel();
							RDFDataMgr.read(m, new ByteArrayInputStream(msg.array()), null, Lang.NTRIPLES);
							System.out.println("Got model with " + m.size() + " triples");
				
							// Convert the model to quads
							Dataset ds = DatasetFactory.createGeneral();
							ds.addNamedModel("http://www.example.org/", m);
							
							List<Quad> quads = Lists.newArrayList(ds.asDatasetGraph().find());
							quadsInserter.onNext(quads);
							}

						}, e -> {
							throw new RuntimeException(e);
						}, () -> {
							eventOutStream.flush();
							eventOutStream.close();
						});

						
						
						// Start the data generator
						logger.info("DG starting ...");
						try {
							dgService.startAsync().awaitRunning(10, TimeUnit.SECONDS);
							
							// We need to wait for the DG service to be ready
							// TODO Wait for the event on the command channel
							
							Thread.sleep(60000);
							logger.info("DG started");
										
							// Obtain the command sender from the spring context ...
							Subscriber<ByteBuffer> commandSender = (Subscriber<ByteBuffer>)tgService.getAppBuilder().context().getBean("commandSender");	
					
							// ... and send the the command to start data generation
					        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATOR_START_SIGNAL}));
												        
							logger.info("DG termination awaited");
							dgService.awaitTerminated(5, TimeUnit.MINUTES);

							

							if(false) {
							TaskGenerator taskGenerator = TaskGenerator.autoConfigure(conn);
							Supplier<SparqlTaskResource> querySupplier = taskGenerator.createScenarioQuerySupplier();

							for(int i = 0; i < 10; ++i) {
								SparqlTaskResource task = querySupplier.get();
								if(task != null) {
									Query query = SparqlTaskResource.parse(task).getAsQueryStmt().getQuery();
									try(QueryExecution qe = conn.query(query)) {
										System.out.println(ResultSetFormatter.asText(qe.execSelect()));
									}
								}
							}
							}
							
							System.out.println("Done - press a key to stop services");
							System.in.read();
							
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
