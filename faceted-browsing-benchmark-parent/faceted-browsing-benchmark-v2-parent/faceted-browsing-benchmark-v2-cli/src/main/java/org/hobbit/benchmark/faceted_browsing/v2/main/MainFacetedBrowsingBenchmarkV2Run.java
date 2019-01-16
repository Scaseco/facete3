package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.core.utils.RDFDataMgrRx;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.update.UpdateRequest;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingEncoders;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.main.HobbitBenchmarkUtils;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
import org.hobbit.core.component.BenchmarkVocab;
import org.hobbit.core.component.ServiceNoOp;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceSystem;
import org.hobbit.core.service.docker.impl.core.DockerServiceWrapper;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;
import com.spotify.docker.client.exceptions.DockerCertificateException;

import io.reactivex.Flowable;

public class MainFacetedBrowsingBenchmarkV2Run {
	
	private static final Logger logger = LoggerFactory.getLogger(MainFacetedBrowsingBenchmarkV2Run.class);

		
	public static void main(String[] args) throws DockerCertificateException, Exception {
		
		if(false) {
			int[] start = {0};
			Supplier<? extends Collection<String>> s = () -> Arrays.asList("" + start[0]++, "a", "b");
	
			Supplier<String> test = SupplierUtils.toSupplier(SupplierUtils.flatMapIterable(s::get));
			for(int i = 0; i < 20; ++i) {
				System.out.println(test.get());
			}

			return;
		}

		
//		Dataset raw = DatasetFactory.create();
		
		//.forEach(d -> Streams.stream(d.asDatasetGraph().find()).forEach(raw.asDatasetGraph()::add));

		
//		
//		Model model = RDFDataMgr.loadModel(uri);
//		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(model));
//
//		taskGenerator = TaskGenerator.autoConfigure(conn);
//		changeTracker = taskGenerator.getChangeTracker();
//		fq = taskGenerator.getCurrentQuery();
//		changeTracker.commitChangesWithoutTracking();
//		
		
		boolean useDocker = true;
		
		try (DockerServiceSystem<?> dss = DockerServiceSystemDockerClient.create(true, Collections.emptyMap(), Collections.emptySet())) {

			DockerService ds;
			if(useDocker) {
				ds = dss.create("docker-service-example", "tenforce/virtuoso", ImmutableMap.<String, String>builder()
						.put("SPARQL_UPDATE", "true")
						.put("DEFAULT_GRAPH", "http://www.example.org/")
	                    .put("VIRT_Parameters_NumberOfBuffers", "170000")
	                    .put("VIRT_Parameters_MaxDirtyBuffers", "130000")
	                    .put("VIRT_Parameters_MaxVectorSize", "1000000000")
	                    .put("VIRT_SPARQL_ResultSetMaxRows", "1000000000")
	                    .put("VIRT_SPARQL_MaxQueryCostEstimationTime", "0")
	                    .put("VIRT_SPARQL_MaxQueryExecutionTime", "600")
	                    .build());
			} else {
				ds = new DockerServiceWrapper<Service>(new ServiceNoOp(), "localhost/jena", () -> "store1");
			}
			try {
				ds.startAsync().awaitRunning();

				// Give the sparql endpoint 5 seconds to come up
				// TODO Add another example for wrapping with health checks
//				
				if(useDocker) {
					Thread.sleep(10000);
				}
				
				// Load a subset of the data
				
				
				String sparqlApiBase = "http://" + ds.getContainerId() + ":8890/";
				String sparqlEndpoint = sparqlApiBase + "sparql";

				dss.findServiceByName("docker-service-example");
				
				
//				try(RDFConnection rawConn = RDFConnectionFactory.connect(sparqlEndpoint)) {
				RDFConnection tmpConn;
				if(useDocker) {
					tmpConn = RDFConnectionFactory.connect(sparqlEndpoint);
				} else {
					Dataset dataset = DatasetFactory.create();
					tmpConn = RDFConnectionFactory.connect(dataset);
				}
				
				try(RDFConnection rawConn = tmpConn) {
					
					RDFConnection baseConn = rawConn;
					//RDFConnection baseConn = RDFConnectionFactory.connect(DatasetFactory.create());

					// Wrap the connection to use a different content type for queries...
					// Jena rejects some of Virtuoso's json output
					@SuppressWarnings("resource")
					RDFConnection wrappedConn =
							new RDFConnectionModular(new SparqlQueryConnectionJsa(
									FluentQueryExecutionFactory
										.from(new QueryExecutionFactorySparqlQueryConnection(baseConn))
										.config()
										.withDatasetDescription(DatasetDescriptionUtils.createDefaultGraph("http://www.example.org/"))
										.withPostProcessor(qe -> {
											if(qe instanceof QueryEngineHTTP) {
												((QueryEngineHTTP)qe).setSelectContentType(WebContent.contentTypeResultsXML);
											}
										})
										.end()
										.create()
										), baseConn, baseConn);

					
					
					RDFConnection conn = wrappedConn;
					
					if(false)
					{
						// This part works; TODO Make a unit test in jsa for it
						for(int i = 0; i < 1000; ++i) {
							System.out.println("Query deadlock test iteration #" + i);
							QueryExecution test = conn.query("SELECT * { ?s ?p ?o }");
							ReactiveSparqlUtils.execSelect(() -> test)
								.limit(10)
								.count()
								.blockingGet();
							
							if(!test.isClosed()) {
								throw new RuntimeException("QueryExecution was not closed with flow");
							}
						}
					}					
					
					
					Flowable<Dataset> flow = RDFDataMgrRx.createFlowableDatasets(
							() -> HobbitBenchmarkUtils.openBz2InputStream("hobbit-sensor-stream-150k-events-data.trig.bz2"),
//							() -> new FileInputStream("/home/raven/Projects/Data/Hobbit/hobbit-sensor-stream-150k.trig"),
							Lang.TRIG,
							"http://www.example.org/");
					
					
					
					long count = flow.count().blockingGet();
					long initSample = count / 2;

					
					long triples = flow.limit(initSample).map(xxx -> xxx.asDatasetGraph().size())
					.toList().blockingGet().stream().mapToLong(yyy -> yyy).sum();
					
					logger.info("Records stats from given dataset (used/available - triples: " + initSample + "/" + count + " - " + triples);
					//flow.onBackpressureBuffer().blockingNext();
					//flow.forEach(x -> System.out.println("Next: " + x));
					flow.limit(initSample).forEach(batch -> {
//					flow.limit(initSample).forEach(batch -> {
						
						// Its probably more efficient (not scientifially evaluated)
						// to create an indexed copy 
						Dataset tmp = DatasetFactory.create();						
						DatasetGraphUtils.addAll(tmp.asDatasetGraph(), batch.asDatasetGraph());
						
						Model m = tmp.getUnionModel();
						UpdateRequest ur = UpdateRequestUtils.createUpdateRequest(m, null);
						ur = UpdateRequestUtils.copyWithIri(ur, "http://www.example.org/", true);
						//System.out.println("Update request: " + ur);
						conn.update(ur);
					});

					

//					System.out.println(Remaining item);flow.count().blockingGet();
					
					// One time auto config based on available data
					TaskGenerator taskGenerator = TaskGenerator.autoConfigure(conn);
					
					// Now wrap the scenario supplier with the injection of sparql update statements
					
					Callable<SparqlTaskResource> querySupplier = taskGenerator.createScenarioQuerySupplier();
					Callable<SparqlTaskResource> updateSupplier = () -> null;
					
					
					Callable<SparqlTaskResource> taskSupplier = SupplierUtils.interleave(querySupplier, updateSupplier);

					
					
					SparqlTaskResource tmp = null;
					
					
					
					try(OutputStream eventOutStream = new FileOutputStream("/tmp/hobbit-tasks.ttl")) {
//					try(OutputStream eventOutStream = new MetaBZip2CompressorInputStream(MainFacetedBrowsingBenchmarkV2Run.class.getResourceAsStream("hobbit-sensor-stream-150k-events.trig.bz2"))) {

						
						//List<String> 
						for(int i = 0; (tmp = taskSupplier.call()) != null; ++i) {			
							int scenarioId = ResourceUtils.tryGetLiteralPropertyValue(tmp, FacetedBrowsingVocab.scenarioId, Integer.class)
								.orElseThrow(() -> new RuntimeException("no scenario id"));

							System.out.println("GENERATED TASK: " + tmp.getURI());
							RDFDataMgr.write(System.out, tmp.getModel(), RDFFormat.TURTLE_PRETTY);
							SparqlStmt stmt = SparqlTaskResource.parse(tmp);
							System.out.println("Query: " + stmt);
	
							tmp.addLiteral(FacetedBrowsingVocab.sequenceId, i);
							
							annotateTaskWithReferenceResult(tmp, conn);
							
//							try(SPARQLResultEx srx = SparqlStmtUtils.execAny(conn, stmt)) {
//								// Ensure to close the result set
//								if(srx.isResultSet()) {
//									// Add reference result set
//									
//									System.out.println("RESULTSET SIZE: " + ResultSetFormatter.consume(srx.getResultSet()));
//								}
//							}
	
							// The old eval module applies special treatment to scenario 0
							// We don't want that

							if(false) {
								Dataset taskData = DatasetFactory.create();
								taskData.addNamedModel(tmp.getURI(), tmp.getModel());
								RDFDataMgr.write(eventOutStream, taskData, RDFFormat.TRIG);
							} else {
								Model model = tmp.getModel();
								RDFDataMgr.write(eventOutStream, model, RDFFormat.TURTLE_PRETTY);								
							}
							eventOutStream.flush();
							
							if(scenarioId > 10) {
								break;
							}
							
							//System.out.println(i + ": " + SparqlTaskResource.parse(tmp));
							
	//						try(SPARQLResultEx srx = SparqlStmtUtils.execAny(conn, stmt)) {
	//							// Ensure to close the result set
	//						}
						}
						
						
					}
					System.out.println("DONE");
					
//					conn.update("PREFIX eg: <http://www.example.org/> INSERT DATA { GRAPH <http://www.example.org/> { eg:s eg:p eg:o } }");
//
//					Model model = conn.queryConstruct("CONSTRUCT FROM <http://www.example.org/> WHERE { ?s ?p ?o }");
//					RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
				}
				
			} finally {
				ds.stopAsync().awaitTerminated(30, TimeUnit.SECONDS);
			}
			
			
		}
		
	}
	
	
	public static Resource annotateTaskWithReferenceResult(Resource task, SparqlQueryConnection refConn) {

        //logger.info("Generated task: " + task);
        
        String queryStr = task.getProperty(BenchmarkVocab.taskPayload).getString();
        
        // The task generation is not complete without the reference result
        // TODO Reference result should be computed against TDB
        try(QueryExecution qe = refConn.query(queryStr)) {
//        	if(task.getURI().equals("http://example.org/Scenario_10-1")) {
//        		System.out.println("DEBUG POINT REACHED");
//        	}
        	
        	ResultSet resultSet = qe.execSelect();
        	//int wtf = ResultSetFormatter.consume(resultSet);
        	ResultSetMem rsMem = new ResultSetMem(resultSet);
        	int numRows = ResultSetFormatter.consume(rsMem);
        	rsMem.rewind();
            logger.info("Number of expected result set rows for task " + task + ": " + numRows + " query: " + queryStr);

        	String resultSetStr = FacetedBrowsingEncoders.resultSetToJsonStr(rsMem);
        	task
        		.addLiteral(BenchmarkVocab.expectedResult, resultSetStr)
        		.addLiteral(BenchmarkVocab.expectedResultSetSize, numRows);

        }
            	//result = FacetedBrowsingEncoders.formatForEvalStorage(task, resultSet, timestamp);
        

        return task;
	}
}
