package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.update.UpdateRequest;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
import org.hobbit.core.component.ServiceNoOp;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceSystem;
import org.hobbit.core.service.docker.impl.core.DockerServiceWrapper;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;
import com.spotify.docker.client.exceptions.DockerCertificateException;

import io.reactivex.Flowable;

public class MainFacetedBrowsingBenchmarkV2Run {
	public static void main(String[] args) throws DockerCertificateException, Exception {


		
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
					
					// Wrap the connection to use a different content type for queries...
					// Jena rejects some of Virtuoso's json output
					@SuppressWarnings("resource")
					RDFConnection conn =
							new RDFConnectionModular(new SparqlQueryConnectionJsa(
									FluentQueryExecutionFactory
										.from(new QueryExecutionFactorySparqlQueryConnection(rawConn))
										.config()
										.withDatasetDescription(DatasetDescriptionUtils.createDefaultGraph("http://example.org/"))
										.withPostProcessor(qe -> {
											if(qe instanceof QueryEngineHTTP) {
												((QueryEngineHTTP)qe).setSelectContentType(WebContent.contentTypeResultsXML);
											}
										})
										.end()
										.create()
										), rawConn, rawConn);

					
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
							() -> new FileInputStream("/home/raven/Projects/Data/Hobbit/hobbit-sensor-stream-150k.trig"),
							Lang.TRIG,
							"http://www.example.org/");
					
					int initSample = 1000;
					//flow.onBackpressureBuffer().blockingNext();
					//flow.forEach(x -> System.out.println("Next: " + x));
					flow.take(initSample).forEach(batch -> {
						
						// Its probably more efficient (not scientifially evaluated)
						// to create an indexed copy 
						Dataset tmp = DatasetFactory.create();						
						DatasetGraphUtils.addAll(tmp.asDatasetGraph(), batch.asDatasetGraph());
						
						Model m = tmp.getUnionModel();
						UpdateRequest ur = UpdateRequestUtils.createUpdateRequest(m, null);
						ur = UpdateRequestUtils.copyWithIri(ur, "http://example.org/", true);
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
					
					//List<String> 
					for(int i = 0; (tmp = taskSupplier.call()) != null; ++i) {			
						int scenarioId = ResourceUtils.tryGetLiteralPropertyValue(tmp, FacetedBrowsingVocab.scenarioId, Integer.class)
							.orElseThrow(() -> new RuntimeException("no scenario id"));
						
						System.out.println("GENERATED TASK: " + tmp.getURI());
						RDFDataMgr.write(System.out, tmp.getModel(), RDFFormat.TURTLE_PRETTY);
						SparqlStmt stmt = SparqlTaskResource.parse(tmp);
						System.out.println("Query: " + stmt);

						if(scenarioId >= 10) {
							break;
						}
						
						//System.out.println(i + ": " + SparqlTaskResource.parse(tmp));
						
//						try(SPARQLResultEx srx = SparqlStmtUtils.execAny(conn, stmt)) {
//							// Ensure to close the result set
//						}
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
}
