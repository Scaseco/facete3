package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.core.RDFConnectionEx;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.core.RDFConnectionMetaData;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingEncoders;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa.ScenarioConfig;
import org.hobbit.core.component.BenchmarkVocab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.google.common.collect.ObjectArrays;




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
	
		//PropertyBasedAcFluent.create();
		
		// HACK/WORKAROUND for Jcommander issue
		// https://github.com/cbeust/jcommander/issues/464
		// Add a dummy element to initialize a list property
		args = ObjectArrays.concat(new String[] {"-d", "foo"}, args, String.class);
		
		JenaPluginUtils.registerJenaResourceClasses(
				CommandMain.class,
				RDFConnectionMetaData.class);
		
		JenaPluginUtils.registerJenaResourceClassesUsingPackageScan(ScenarioConfig.class);
		
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
		
		RDFConnectionEx connTmp;
		
		List<String> nonOptionArgs = cmMain.getNonOptionArgs();
		String sparqEndpoint = cmMain.getSparqlEndpoint();

		if(!nonOptionArgs.isEmpty()) {
			
			// Make paths absolute
			List<Path> paths = nonOptionArgs.stream()
					.map(Paths::get)
					.map(Path::toAbsolutePath)
					.collect(Collectors.toList());
			
			if(sparqEndpoint != null) {
				throw new RuntimeException("Cannot mix file and sparql enpdoint sources");
			}
			
			Model model = ModelFactory.createDefaultModel();
			for(Path arg : paths) {
				Model contrib = RDFDataMgr.loadModel(arg.toString());
				model.add(contrib);
			}
			
			RDFConnectionMetaData metadata = ModelFactory.createDefaultModel()
					.createResource().as(RDFConnectionMetaData.class);

			String id = nonOptionArgs.stream().collect(Collectors.joining("---"));
			id = StringUtils.urlEncode(id);
			id = id.replaceAll("%2F", "_");
			metadata.setServiceURL(id);

			
			connTmp = RDFConnectionFactoryEx.wrap(
					RDFConnectionFactory.connect(DatasetFactory.wrap(model)),
					metadata);			
//			if(cmMain.getNonOptionArgs().size() > 1) {
//				throw new RuntimeException("Only 1 non-option argument expected");
//			}
		} else {
			
//			String sparqEndpoint = cmMain.getSparqlEndpoint();
	
			DatasetDescription datasetDescription = new DatasetDescription();
			datasetDescription.addAllDefaultGraphURIs(cmMain.getDefaultGraphUris());
			
					
					//RDFConnectionFactory.connect(sparqEndpoint);

			connTmp = RDFConnectionFactoryEx.connect(sparqEndpoint, datasetDescription);
		}				
		
		String dataSummaryUri = cmMain.getPathFindingDataSummaryUri();
		Model dataSummaryModel = null;
		if(dataSummaryUri != null) {
			logger.info("Loading path finding data summary from " + dataSummaryUri);
			dataSummaryModel = RDFDataMgr.loadModel(dataSummaryUri);
			logger.info("Done loading path finding data summary from" + dataSummaryUri);
		}
		
		
		try(RDFConnectionEx conn = connTmp) {
		
			
			Random random = new Random(0);

			// One time auto config based on available data
			TaskGenerator taskGenerator = TaskGenerator.autoConfigure(random, conn, dataSummaryModel, true);
			
			// Now wrap the scenario supplier with the injection of sparql update statements
			
			Callable<SparqlTaskResource> querySupplier = taskGenerator.createScenarioQuerySupplier();
			// TODO How can we simplify the interleaves of updates?
			Callable<SparqlTaskResource> updateSupplier = () -> null;
			
			
			Callable<SparqlTaskResource> taskSupplier = SupplierUtils.interleave(querySupplier, updateSupplier);

			SparqlTaskResource tmp = null;
			
			
			
			//try(OutputStream eventOutStream = new FileOutputStream("/tmp/hobbit-tasks.ttl")) {
			OutputStream eventOutStream = System.out;
//					try(OutputStream eventOutStream = new MetaBZip2CompressorInputStream(MainFacetedBrowsingBenchmarkV2Run.class.getResourceAsStream("hobbit-sensor-stream-150k-events.trig.bz2"))) {

				
				//List<String> 
				for(int i = 0; (tmp = taskSupplier.call()) != null; ++i) {			
					int scenarioId = ResourceUtils.tryGetLiteralPropertyValue(tmp, FacetedBrowsingVocab.scenarioId, Integer.class)
						.orElseThrow(() -> new RuntimeException("no scenario id"));

					//System.out.println("GENERATED TASK: " + tmp.getURI());
					logger.info("GENERATED TASK: " + tmp.getURI());
					RDFDataMgr.write(System.out, tmp.getModel(), RDFFormat.TURTLE_PRETTY);
					//SparqlStmt stmt = SparqlTaskResource.parse(tmp);
					//System.out.println("Query: " + stmt);

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
				
				
			//}
			
			//System.err.println("DONE");
			
//					conn.update("PREFIX eg: <http://www.example.org/> INSERT DATA { GRAPH <http://www.example.org/> { eg:s eg:p eg:o } }");
//
//					Model model = conn.queryConstruct("CONSTRUCT FROM <http://www.example.org/> WHERE { ?s ?p ?o }");
//					RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
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
