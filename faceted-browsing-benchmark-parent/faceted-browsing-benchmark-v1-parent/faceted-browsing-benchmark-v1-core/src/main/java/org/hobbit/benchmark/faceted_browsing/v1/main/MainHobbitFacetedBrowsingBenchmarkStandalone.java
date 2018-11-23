package org.hobbit.benchmark.faceted_browsing.v1.main;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.diff.ListDiff;
import org.aksw.jena_sparql_api.compare.QueryExecutionCompare;
import org.aksw.jena_sparql_api.compare.QueryExecutionFactoryCompare;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.connection.RDFDatasetConnectionMultiplex;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.core.connection.SparqlUpdateConnectionMultiplex;
import org.aksw.jena_sparql_api.core.connection.TransactionalMultiplex;
import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.common.launcher.ConfigsFacetedBrowsingBenchmark;
import org.hobbit.benchmark.faceted_browsing.config.ConfigDockerServiceFactory;
import org.hobbit.benchmark.faceted_browsing.v1.config.ConfigDataGeneratorFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.v1.config.ConfigVirtualDockerServiceFactoryV1;
import org.hobbit.benchmark.faceted_browsing.v1.config.FacetedBrowsingBenchmarkV1Constants;
import org.hobbit.benchmark.faceted_browsing.v1.impl.FacetedTaskGeneratorOld;
import org.hobbit.core.component.DataGeneratorComponentBase;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.hobbit.core.service.docker.DockerServiceBuilderJsonDelegate;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.DockerServiceFactorySpringApplicationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class MainHobbitFacetedBrowsingBenchmarkStandalone {

	
	private static final Logger logger = LoggerFactory.getLogger(MainHobbitFacetedBrowsingBenchmarkStandalone.class);

	
	//@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		
        String graphName = DataGeneratorComponentBase.GRAPH_IRI;

        
		DockerServiceFactory<?> dsf = ConfigDockerServiceFactory.createDockerServiceFactory(
				true,
				ImmutableMap.<String, String>builder().build(),
				new DockerServiceFactorySpringApplicationBuilder(ConfigVirtualDockerServiceFactoryV1.getDockerServiceFactoryOverrides(FacetedBrowsingBenchmarkV1Constants.config))
		);
		
		DockerServiceBuilderFactory<?> dsbf = () -> DockerServiceBuilderJsonDelegate.create(dsf::create);
		
		SparqlBasedService saService = ConfigsFacetedBrowsingBenchmark.createVirtuosoSparqlService("tenforce/virtuoso", dsbf);		
		
		saService.startAsync().awaitRunning();

		RDFConnection coreConn = saService.createDefaultConnection();
		
		Dataset dataset = DatasetFactory.create();

		RDFConnection refConn = RDFConnectionFactory.connect(dataset);
		
		QueryExecutionFactory qefA = new QueryExecutionFactorySparqlQueryConnection(coreConn);
		
		
		QueryExecutionFactory qefB = new QueryExecutionFactorySparqlQueryConnection(refConn);
//		qefB = FluentQueryExecutionFactory
//				.from(qefB)
//				.config()
//					.withDatasetDescription(new DatasetDescription(Arrays.asList(graphName), Arrays.asList()))
//					.withParser(SparqlQueryParserImpl.create())
//				.end()
//				.create();

		
		QueryExecutionFactory qefCmp = new QueryExecutionFactoryCompare(qefA, qefB);
		
		List<RDFConnection> delegates = Arrays.asList(coreConn, refConn);
		
		RDFConnection conn = new RDFConnectionModular(
			new SparqlQueryConnectionJsa(qefCmp, new TransactionalMultiplex<>(delegates)),
			new SparqlUpdateConnectionMultiplex(delegates),
			new RDFDatasetConnectionMultiplex(delegates));
						
		
		//logger.info("SPARQL Endpoint online at: " + saService.);
		
//		SparqlBasedService saService = dsbf.get()
//				.setImageName("virtuoso/tenforce")
//				.setLocalEnvironment(ImmutableMap.<String, String>builder()
//				.build());
		
		File baseOutputDir = Files.createTempDirectory("mocha-resources").toFile();
		logger.info("Preparing output folder " + baseOutputDir.getAbsolutePath());

	
		logger.info("Generating dataset...");
		Stream<Triple> triples = ConfigDataGeneratorFacetedBrowsing.createPodiggDatasetViaDocker(
			dsbf,
			"podigg",
			ImmutableMap.<String, String>builder()
//					.put("GTFS_GEN_SEED", "123")
//					.put("GTFS_GEN_CONNECTIONS__CONNECTIONS", "50")
					.put("GTFS_GEN_SEED", "111")
					.put("GTFS_GEN_REGION__SIZE_X", "2000")
					.put("GTFS_GEN_REGION__SIZE_Y", "2000")
					.put("GTFS_GEN_REGION__CELLS_PER_LATLON", "200")
					.put("GTFS_GEN_STOPS__STOPS", "3500")
					.put("GTFS_GEN_CONNECTIONS__DELAY_CHANCE", "0.02")
					.put("GTFS_GEN_CONNECTIONS__CONNECTIONS", "4000")
					.put("GTFS_GEN_ROUTES__ROUTES", "3500")
					.put("GTFS_GEN_ROUTES__MAX_ROUTE_LENGTH", "50")
					.put("GTFS_GEN_ROUTES__MIN_ROUTE_LENGTH", "10")
					.put("GTFS_GEN_CONNECTIONS__ROUTE_CHOICE_POWER", "1.3")
					.put("GTFS_GEN_CONNECTIONS__TIME_FINAL", "31536000000")
					.build());

		Model model = ModelFactory.createDefaultModel();

		triples.forEach(t -> {
			Statement stmt = ModelUtils.tripleToStatement(model, t);
			model.add(stmt);
		});
		triples.close();
		
		File tmpFile = new File(baseOutputDir, "lc.ttl");
		RDFDataMgr.write(new FileOutputStream(tmpFile), model, RDFFormat.TURTLE_PRETTY);
		
		logger.info("Running task generation...");
		// Note: There might be a Jena bug - i could not get sparql queries with a FROM clause working...
		coreConn.load(graphName, tmpFile.getAbsolutePath());
		refConn.load(tmpFile.getAbsolutePath());

		
		
		//System.out.println(ResultSetFormatter.asText(qefB.createQueryExecution("SELECT ?g (COUNT(*) AS ?c) { GRAPH ?g { ?s ?p ?o } } GROUP BY ?g ORDER BY DESC(COUNT(*))").execSelect()));
		System.out.println(ResultSetFormatter.asText(qefB.createQueryExecution("SELECT (Count(*) AS ?c) { ?s ?p ?o }").execSelect()));
		System.out.println(dataset.getUnionModel().size());
		
		
		List<Resource> tasks = FacetedTaskGeneratorOld.runTaskGenerationCore(conn, conn).collect(Collectors.toList());
		

		
		File selectQueryDir = new File(baseOutputDir, "selectQueries");
		selectQueryDir.mkdir();
		
		File expectedResultDir = new File(baseOutputDir, "expectedResults");
		expectedResultDir.mkdir();

		
		int i = 0;
		for(Resource task : tasks) {


//			if(!(task.getURI().contains("Scenario_1-") || task.getURI().contains("Scenario_2-"))) {
//				continue;
//			}
			++i;
			
			System.out.println("Processing task " + i + ": " + task.getURI());
			
			String queryStr = task.getRequiredProperty(RDFS.label).getString();
			String resultSetStr = task.getRequiredProperty(RDFS.comment).getString();

			
			try(QueryExecution qe = conn.query(queryStr)) {				
				int rsSize = ResultSetFormatter.consume(qe.execSelect());
				System.out.println("Candidate " + i + " has " + rsSize + " rows");
			}
			
			//String baseName = task.getLocalName();

			
			// Validate the query once more...
			//ResultSet coreRs = ResultSetFactory.fromJSON(new ByteArrayInputStream(resultSetStr.getBytes()));
			
			boolean isEqual;
			try(
				QueryExecution coreQe = coreConn.query(queryStr);
				QueryExecution refQe = refConn.query(queryStr)) {
				ResultSet coreRs = coreQe.execSelect();
				//ResultSet coreRs = ResultSetMgr.read(, Lang.RDFJSON);
				
				ResultSet refRs = refQe.execSelect();

				ListDiff<Binding> diff = QueryExecutionCompare.compareUnordered(coreRs, refRs);
				
				isEqual = diff.getAdded().isEmpty() && diff.getRemoved().isEmpty();
			}
			
			if(isEqual) {
				System.out.println("Working query " + i);
				
				//String baseName = "" + i;
				String baseName = "_" + task.getLocalName();
				
				File selectQueryFile = new File(selectQueryDir, "selectQuery" + baseName + ".sparql");
				File expectedResultFile = new File(expectedResultDir, "expectedResults" + baseName + ".sparql");
				
				com.google.common.io.Files.asCharSink(selectQueryFile, StandardCharsets.UTF_8).write(queryStr);
				com.google.common.io.Files.asCharSink(expectedResultFile, StandardCharsets.UTF_8).write(resultSetStr);
				
			} else {
				System.out.println("Non working query " + i);
				--i;
				continue;
			}
			
			//File expectedResult = new File(expectedresultdir, "")
			
		//tasks.forEach(t -> {
			//RDFDataMgr.write(System.out, task.getModel(), RDFFormat.TURTLE_PRETTY);
		}
		
		
				
		
		logger.info("Output written to " + baseOutputDir.getAbsolutePath());

//		FactoryBeanSparqlServer.newInstance()
//			.setSparqlServiceFactory(new QueryExecutionFactorySparqlQueryConnection(refConn))
//			.setPort(7531)
//			.create();
		
		logger.info("Press [ENTER] key to terminate servers");
		//System.in.read();
		
		
		logger.info("Cleaning  up...");
		saService.stopAsync().awaitTerminated();
		// Load data into the task generator triple store

		logger.info("Done.");

		// Run task generation

		
		//DataGeneratorFacetedBrowsing
		
		//TaskGeneratorModule taskGenerator = new TaskGeneratorModuleFacetedBrowsing();
		
		
		
	}
}
