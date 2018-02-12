package org.hobbit.benchmark.faceted_browsing.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.component.TaskGeneratorModuleFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDockerServiceFactory;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.hobbit.core.service.docker.DockerServiceBuilderJsonDelegate;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class MainHobbitFacetedBrowsingBenchmarkStandalone {

	
	private static final Logger logger = LoggerFactory.getLogger(MainHobbitFacetedBrowsingBenchmarkStandalone.class);

	
	public static void main(String[] args) throws Exception {
		
		DockerServiceFactory<?> dsf = ConfigDockerServiceFactory.createDockerServiceFactory(true,
				ImmutableMap.<String, String>builder().build()
		);
		
		DockerServiceBuilderFactory<?> dsbf = () -> DockerServiceBuilderJsonDelegate.create(dsf::create);
		
		SparqlBasedService saService = ConfigsFacetedBrowsingBenchmark.createVirtuosoSparqlService(dsbf);
	
		saService.startAsync().awaitRunning();

		//logger.info("SPARQL Endpoint online at: " + saService.);
		
		RDFConnection conn = saService.createDefaultConnection();
//		SparqlBasedService saService = dsbf.get()
//				.setImageName("virtuoso/tenforce")
//				.setLocalEnvironment(ImmutableMap.<String, String>builder()
//				.build());
		
		File baseOutputDir = Files.createTempDirectory("mocha-resources").toFile();
		logger.info("Preparing output folder " + baseOutputDir.getAbsolutePath());

	
		logger.info("Generating dataset...");
		Stream<Triple> triples = ConfigsFacetedBrowsingBenchmark.ConfigDataGeneratorFacetedBrowsing.createPodiggDatasetViaDocker(
			dsbf,
			"podigg",
			ImmutableMap.<String, String>builder()
					.put("GTFS_GEN_SEED", "123")
					.put("GTFS_GEN_CONNECTIONS__CONNECTIONS", "50")
					.build());

		Model model = ModelFactory.createDefaultModel();

		triples.forEach(t -> {
			Statement stmt = ModelUtils.tripleToStatement(model, t);
			model.add(stmt);
		});
		
		File tmpFile = new File(baseOutputDir, "lc.ttl");
		RDFDataMgr.write(new FileOutputStream(tmpFile), model, RDFFormat.TURTLE_PRETTY);
		
		logger.info("Running task generation...");
        String graphName = "http://www.virtuoso-graph.com"; //"http://www.example.com/graph"; //"http://www.virtuoso-graph.com";
		conn.load(graphName, tmpFile.getAbsolutePath());

		List<Resource> tasks = TaskGeneratorModuleFacetedBrowsing.runTaskGenerationCore(conn, conn).collect(Collectors.toList());
		

		
		File selectQueryDir = new File(baseOutputDir, "selectQueries");
		selectQueryDir.mkdir();
		
		File expectedResultDir = new File(baseOutputDir, "expectedResults");
		expectedResultDir.mkdir();

		
		int i = 0;
		for(Resource task : tasks) {
			
			logger.info("Processing task " + i + ": " + task.getURI());

			
			++i;
			//String baseName = task.getLocalName();
			String baseName = "" + i;
			
			File selectQueryFile = new File(selectQueryDir, "selectQuery" + baseName + ".sparql");
			File expectedResultFile = new File(expectedResultDir, "expectedResults" + baseName + ".sparql");
			
			String queryStr = task.getRequiredProperty(RDFS.label).getString();
			com.google.common.io.Files.asCharSink(selectQueryFile, StandardCharsets.UTF_8).write(queryStr);
			
			String resultSetStr = task.getRequiredProperty(RDFS.comment).getString();
			com.google.common.io.Files.asCharSink(expectedResultFile, StandardCharsets.UTF_8).write(resultSetStr);
			
			//File expectedResult = new File(expectedresultdir, "")
			
		//tasks.forEach(t -> {
			//RDFDataMgr.write(System.out, task.getModel(), RDFFormat.TURTLE_PRETTY);
		}
		
		
		
		logger.info("Cleaning  up...");
		saService.stopAsync().awaitTerminated();
		// Load data into the task generator triple store
		
		logger.info("Output written to " + baseOutputDir.getAbsolutePath());
		logger.info("Done.");
		
		// Run task generation

		
		//DataGeneratorFacetedBrowsing
		
		//TaskGeneratorModule taskGenerator = new TaskGeneratorModuleFacetedBrowsing();
		
		
		
	}
}
