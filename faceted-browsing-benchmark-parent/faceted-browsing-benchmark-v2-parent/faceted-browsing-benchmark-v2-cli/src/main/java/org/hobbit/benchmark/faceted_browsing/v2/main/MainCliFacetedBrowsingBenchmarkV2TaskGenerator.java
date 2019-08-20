package org.hobbit.benchmark.faceted_browsing.v2.main;

import com.beust.jcommander.JCommander;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ObjectArrays;
import com.spotify.docker.client.DockerClient;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.core.RDFConnectionEx;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.core.RDFConnectionMetaData;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoBulkLoad;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingEncoders;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.config.ComponentUtils;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa.ScenarioConfig;
import org.hobbit.core.component.BenchmarkVocab;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceDockerClient;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import virtuoso.jdbc4.VirtuosoDataSource;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;




/**
 * Entry point for the CLI of the task generator of the
 * hobbit faceted browsing benchmark v2
 * 
 * @author Claus Stadler, Jan 17, 2019
 *
 */
public class MainCliFacetedBrowsingBenchmarkV2TaskGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(MainCliFacetedBrowsingBenchmarkV2TaskGenerator.class);
		

	
	
	public static void main(String[] args) throws Exception {
	
		DockerServiceSystemDockerClient dss = null;
		DockerService ds = null;

		try {
	
			//PropertyBasedAcFluent.create();
			
			// HACK/WORKAROUND for Jcommander issue
			// https://github.com/cbeust/jcommander/issues/464
			// Add a dummy element to initialize a list property
			args = ObjectArrays.concat(new String[] {"-d", "foo"}, args, String.class);
			
			JenaPluginUtils.registerResourceClasses(
					CommandMain.class,
					RDFConnectionMetaData.class);
			
			JenaPluginUtils.scan(ScenarioConfig.class);

	//		Expr e = ExprUtils.parse("xsd:integer(floor(1.2)", PrefixMapping.Extended);
	//		System.out.println(e);
	//		NodeValue x = e.eval(BindingFactory.root(), FunctionEnvBase.createTest());
	//		System.out.println(x);
	//System.out.println("yay");
	//		Expr ffs = new E_Function(XSD.xint.getURI(), new ExprList(NodeValue.makeDecimal(1.2))); 
	//		System.out.println(ffs.eval(BindingFactory.root(), FunctionEnvBase.createTest()));
	//		if(true) { return; }
			
			CommandMain cmMain = ModelFactory.createDefaultModel().createResource().as(CommandMain.class);
	
			JCommander jc = JCommander.newBuilder()
					.addObject(cmMain)
					.build();
			          
			
			jc.parse(args);
			
			if(cmMain.getHelp()) {
				jc.usage();
				return;
			}
			
			String configUri = cmMain.getConfig();			
			if(configUri == null) {
				configUri = "config-all.ttl";
			}

			ScenarioConfig config = TaskGenerator.extractScenarioConfig(configUri);			
			
			
			RDFConnectionEx rawConnEx;
			
			List<String> nonOptionArgs = cmMain.getNonOptionArgs();
			String sparqEndpoint = cmMain.getSparqlEndpoint();
	
			if(!nonOptionArgs.isEmpty()) {
				
				DatasetDescription datasetDescription = new DatasetDescription();
				datasetDescription.addDefaultGraphURI("http://www.example.org/");
				
				// Make paths absolute
				List<Path> paths = nonOptionArgs.stream()
						.map(Paths::get)
						.map(Path::toAbsolutePath)
						.collect(Collectors.toList());
				
				
				if(sparqEndpoint != null) {
					throw new RuntimeException("Cannot mix file and sparql enpdoint sources");
				}
				
//				Model model = ModelFactory.createDefaultModel();
//				for(Path arg : paths) {
//					Model contrib = RDFDataMgr.loadModel(arg.toString());
//					model.add(contrib);
//				}
				
				RDFConnectionMetaData metadata = ModelFactory.createDefaultModel()
						.createResource().as(RDFConnectionMetaData.class);
	
				String id = paths.stream().map(Object::toString).collect(Collectors.joining("---"));
				id = StringUtils.urlEncode(id);
				id = id.replaceAll("%2F", "_");
				metadata.setServiceURL(id);
				metadata.getDefaultGraphs().addAll(datasetDescription.getDefaultGraphURIs());
				metadata.getNamedGraphs().addAll(datasetDescription.getNamedGraphURIs());
				
				
/// begin of DOCKER STUFF

				dss = DockerServiceSystemDockerClient.create(true, Collections.emptyMap(), Collections.emptySet());
				DockerClient dockerClient = dss.getDockerClient();
				
				//conn = DatasetFactory.wrap(model);
				
				logger.info("Attempting to starting a virtuoso from docker");
				DockerServiceDockerClient dsCore = dss.create("tenforce/virtuoso", ImmutableMap.<String, String>builder()
						.put("SPARQL_UPDATE", "true")
						.put("DEFAULT_GRAPH", "http://www.example.org/")
						.put("VIRT_Parameters_NumberOfBuffers", "170000")
						.put("VIRT_Parameters_MaxDirtyBuffers", "130000")
						.put("VIRT_Parameters_MaxVectorSize", "1000000000")
						.put("VIRT_SPARQL_ResultSetMaxRows", "1000000000")
						.put("VIRT_SPARQL_MaxQueryCostEstimationTime", "0")
						.put("VIRT_SPARQL_MaxQueryExecutionTime", "600")
						.build());
								
				ds = ComponentUtils.wrapSparqlServiceWithHealthCheck(dsCore, 8890);
	
				ds.startAsync().awaitRunning();
	
				// Give the sparql endpoint 5 seconds to come up
				// TODO Add another example for wrapping with health checks
				//Thread.sleep(10000);
				
				String dockerContainerId = dsCore.getCreationId();
				String dockerContainerIp = ds.getContainerId();
				
				Path unzipFolder = Paths.get("/tmp").resolve(dockerContainerId);
				Files.createDirectories(unzipFolder);
				
				for(Path path : paths) {
					Path filename = path.getFileName();
					Path target = unzipFolder.resolve(filename);
					Files.copy(path, target);
				}

				
				String allowedDir = "/usr/local/virtuoso-opensource/var/lib/virtuoso/db/";
				dockerClient.copyToContainer(unzipFolder, dockerContainerId, allowedDir);

				logger.info("Connecting to virtuoso");
				VirtuosoDataSource dataSource = new VirtuosoDataSource();
				dataSource.setUser("dba");
				dataSource.setPassword("dba");
				dataSource.setPortNumber(1111);
				dataSource.setServerName(dockerContainerIp);
				try(java.sql.Connection c = dataSource.getConnection()) {
					logger.info("Preparing bulk loader");
					VirtuosoBulkLoad.logEnable(c, 2, 0);

					for(Path path : paths) {
						String actualFilename = path.getFileName().toString();
						logger.info("Registered file for bulkload: " + actualFilename);
						VirtuosoBulkLoad.ldDir(c, allowedDir, actualFilename, "http://www.example.org/");
					}
					
					logger.info("Running bulk loader");
					VirtuosoBulkLoad.rdfLoaderRun(c);

					logger.info("Creating checkpoint");
					VirtuosoBulkLoad.checkpoint(c);
				}
				
				Files.walk(unzipFolder)
				    .sorted(Comparator.reverseOrder())
				    .map(Path::toFile)
				    //.peek(System.out::println)
				    .forEach(File::delete);
				    //.count();
				
/// end of DOCKER STUFF
				
				String sparqlApiBase = "http://" + ds.getContainerId() + ":8890/";
				String sparqlEndpoint = sparqlApiBase + "sparql";

				// Wrap the connection to use a different content type for queries...
				// Jena rejects some of Virtuoso's json output
				@SuppressWarnings("resource")
				RDFConnection rawConn = RDFConnectionFactoryEx.connect(sparqlEndpoint, datasetDescription);

				rawConnEx = RDFConnectionFactoryEx.wrap(
						rawConn,
						metadata);

			} else {
				
				DatasetDescription datasetDescription = new DatasetDescription();
				datasetDescription.addAllDefaultGraphURIs(cmMain.getDefaultGraphUris());
	
				rawConnEx = RDFConnectionFactoryEx.connect(sparqEndpoint, datasetDescription);
			}				
			
			String dataSummaryUri = cmMain.getPathFindingDataSummaryUri();
			Model dataSummaryModel = null;
			if(dataSummaryUri != null) {
				logger.info("Loading path finding data summary from " + dataSummaryUri);
				dataSummaryModel = RDFDataMgr.loadModel(dataSummaryUri);
				logger.info("Done loading path finding data summary from" + dataSummaryUri);
			}
			
			
			RDFConnectionEx conn = rawConnEx;
		
			
			Random random = new Random(0);

			// One time auto config based on available data
			TaskGenerator taskGenerator = TaskGenerator.autoConfigure(config, random, conn, dataSummaryModel, true);
			
			// Now wrap the scenario supplier with the injection of sparql update statements
			
			Callable<SparqlTaskResource> querySupplier = taskGenerator.createScenarioQuerySupplier();
			// TODO How can we simplify the interleaves of updates?
			Callable<SparqlTaskResource> updateSupplier = () -> null;
			
			
			Callable<SparqlTaskResource> taskSupplier = SupplierUtils.interleave(querySupplier, updateSupplier);

			SparqlTaskResource tmp = null;
			
			
			Integer numScenarios = config.getNumScenarios();
			Integer numWarmups = config.getNumWarmups();
		
			logger.info("Num warmups  : " + numWarmups);
			logger.info("Num scenarios: " + numScenarios);
			
			int numTotalScenarios = numScenarios + numWarmups;
			
			//try(OutputStream eventOutStream = new FileOutputStream("/tmp/hobbit-tasks.ttl")) {
			OutputStream eventOutStream = System.out;
//					try(OutputStream eventOutStream = new MetaBZip2CompressorInputStream(MainFacetedBrowsingBenchmarkV2Run.class.getResourceAsStream("hobbit-sensor-stream-150k-events.trig.bz2"))) {

				int lastSeenScenario = -1;
				
				int scenarioCounter = 0;
				
				//List<String> 
				for(int i = 0; (tmp = taskSupplier.call()) != null; ++i) {			
					int scenarioId = ResourceUtils.tryGetLiteralPropertyValue(tmp, FacetedBrowsingVocab.scenarioId, Integer.class)
						.orElseThrow(() -> new RuntimeException("no scenario id"));

					if(scenarioId != lastSeenScenario) {
						lastSeenScenario = scenarioId;
						++scenarioCounter;
					}
					
					if(scenarioCounter > numTotalScenarios) {
						break;
					}


					if(scenarioCounter <= numWarmups) {
						tmp.addLiteral(FacetedBrowsingVocab.warmup, true);						
						tmp = org.apache.jena.util.ResourceUtils.renameResource(tmp, tmp.getURI() + "-warmup").as(SparqlTaskResource.class);
					}

					
					//System.out.println("GENERATED TASK: " + tmp.getURI());
					logger.info("GENERATED TASK: " + tmp.getURI());
					tmp.addLiteral(FacetedBrowsingVocab.sequenceId, i);
										
					//RDFDataMgr.write(System.out, tmp.getModel(), RDFFormat.TURTLE_PRETTY);
					//SparqlStmt stmt = SparqlTaskResource.parse(tmp);
					//System.out.println("Query: " + stmt);


					try {
						annotateTaskWithReferenceResult(tmp, conn);
					} catch (QueryExceptionHTTP exceptionHTTP) {
						logger.warn("Query execution failed: {}", exceptionHTTP.toString());
					}
					
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

					if(true) {
						Dataset taskData = DatasetFactory.create();
						taskData.addNamedModel(tmp.getURI(), tmp.getModel());
						RDFDataMgr.write(eventOutStream, taskData, RDFFormat.TRIG);
					} else {
						Model model = tmp.getModel();
						RDFDataMgr.write(eventOutStream, model, RDFFormat.TURTLE_PRETTY);								
					}
					eventOutStream.flush();
										
					//System.out.println(i + ": " + SparqlTaskResource.parse(tmp));
					
//						try(SPARQLResultEx srx = SparqlStmtUtils.execAny(conn, stmt)) {
//							// Ensure to close the result set
//						}
				}
				
				
			//}
			
			//System.err.println("DONE");
			
//					conn.update("PREFIX eg: <http://www.example.org/> INSERT DATA { GRAPH <http://www.example.org/> { eg:s eg:p eg:o } }");
//
//					Model model = conn.queryConstruct("CONSTRUCT FROM <http://www.example.org/> WHERE { ?s ?p ?o }");
//					RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
		} finally {
			if(ds != null) {
				ds.stopAsync().awaitTerminated(30, TimeUnit.SECONDS);
			}

			if(dss != null) {
				dss.close();
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
        	logger.debug(queryStr);
        	ResultSet resultSet = qe.execSelect();
        	//int wtf = ResultSetFormatter.consume(resultSet);
        	ResultSetMem rsMem = new ResultSetMem(resultSet);
        	int numRows = ResultSetFormatter.consume(rsMem);
        	rsMem.rewind();
            logger.info("Number of expected result set rows for task " + task + ": " + numRows + " query: " + queryStr);

        	String resultSetStr = FacetedBrowsingEncoders.resultSetToJsonStr(rsMem);
        	task
        		.addLiteral(BenchmarkVocab.expectedResult, resultSetStr)
        		.addLiteral(BenchmarkVocab.expectedResultSize, numRows);

        }
            	//result = FacetedBrowsingEncoders.formatForEvalStorage(task, resultSet, timestamp);
        

        return task;
	}
}




//if(false)
//{
//	// This part works; TODO Make a unit test in jsa for it
//	for(int i = 0; i < 1000; ++i) {
//		System.out.println("Query deadlock test iteration #" + i);
//		QueryExecution test = conn.query("SELECT * { ?s ?p ?o }");
//		ReactiveSparqlUtils.execSelect(() -> test)
//			.limit(10)
//			.count()
//			.blockingGet();
//		
//		if(!test.isClosed()) {
//			throw new RuntimeException("QueryExecution was not closed with flow");
//		}
//	}
//}					
