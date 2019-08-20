package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.common.launcher.ConfigsFacetedBrowsingBenchmark;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingEncoders;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.component.TaskGeneratorModuleFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.main.HobbitBenchmarkUtils;
import org.hobbit.core.Constants;
import org.hobbit.core.component.BenchmarkVocab;
import org.hobbit.core.component.TaskGeneratorModule;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import io.reactivex.Flowable;

// Configuration for the worker task generator fo the faceted browsing benchmark
	public class ConfigTaskGeneratorFacetedBenchmarkV2 {
		
		private static final Logger logger = LoggerFactory.getLogger(ConfigTaskGeneratorFacetedBenchmarkV2.class);
		
		
	    @Bean
	    public SparqlBasedService taskGeneratorSparqlService(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
	    	SparqlBasedService result = ConfigsFacetedBrowsingBenchmark.createVirtuosoSparqlService("tenforce/virtuoso", dockerServiceBuilderFactory);
	    	return result;
	    	
//	        VirtuosoSystemService result = new VirtuosoSystemService(
//	                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
//	                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit-task-generation_1112_8891/virtuoso.ini"));
//
//	        return result;
	    }
	    
	    
	    public static TaskGeneratorModule generateDummyData() {

	    	BiFunction<SparqlQueryConnection, SparqlQueryConnection, Flowable<Resource>> fn = (conn, refConn) ->
    		{
    			
    			
				return Flowable.fromIterable(Arrays.asList(
						ModelFactory.createDefaultModel().createResource("http://www.example.org/testTask1")
							.addLiteral(BenchmarkVocab.taskPayload, "SELECT * { ?s ?p ?o }")
							.addLiteral(BenchmarkVocab.expectedResult, FacetedBrowsingEncoders.resultSetToJsonStr(
									ResultSetFactory.create(new QueryIterPlainWrapper(
											Arrays.asList(BindingFactory.binding(Vars.s, RDF.Nodes.type)).iterator()),
											Arrays.asList("s"))))
							.addLiteral(FacetedBrowsingVocab.scenarioId, 1)
							.addLiteral(FacetedBrowsingVocab.queryId, 1)
							.addLiteral(FacetedBrowsingVocab.chokepointId, 1)
				));
			};
    	
    	//List<Resource> tasks = FacetedTaskGeneratorOld.runTaskGenerationCore(conn, refConn).collect(Collectors.toList());
    	//Flowable<Resource> t2 = Flowable.fromIterable(tasks);
    	
    	return new TaskGeneratorModuleFacetedBrowsing(fn);	
	    }
	    

	    
	    @Bean
	    public TaskGeneratorModule taskGeneratorModule(@Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr)
	    	throws Exception
	    {
	        logger.info("TG: Supplied param model is: " + paramModelStr);
	        
	        // Load the benchmark.ttl config as it contains the parameter mapping
	        Model paramModel = ModelFactory.createDefaultModel();
	        RDFDataMgr.read(paramModel, new ByteArrayInputStream(paramModelStr.getBytes()), Lang.JSONLD);

	        Model model = ModelFactory.createDefaultModel();
	        model.add(paramModel);

	        String preconfig = model.listStatements(null, FacetedBrowsingVocab.preconfTasks, (RDFNode)null).nextOptional().map(Statement::getString).orElse("").trim(); 
	        if(Strings.isNullOrEmpty(preconfig)) {
	        	throw new RuntimeException("No task file configured in config model");
	        }

	        // Try to open the resource now
	        try(InputStream rawIn = HobbitBenchmarkUtils.openResource(preconfig)) { }

	    	
	    	BiFunction<SparqlQueryConnection, SparqlQueryConnection, Flowable<Resource>> fn = (conn, refConn) -> {
    	    	Flowable<Resource> result = RDFDataMgrRx.createFlowableResources(() -> HobbitBenchmarkUtils.openResource(preconfig), Lang.TRIG, "http://www.example.org/");
				return result;
	    	};
	    	
	    	return new TaskGeneratorModuleFacetedBrowsing(fn);
	    }
	    
//	    public static Flowable<Resource> readStaticFile() throws IOException {
//
//	    	//String src = "hobbit-sensor-stream-75k-events-tasks.ttl.bz2";
//	    	String src = "hobbit-sensor-stream-2516-events-103156-triples-tasks.ttl.bz2";
//	    	
//	    	
//	    	InputStream in = HobbitBenchmarkUtils.openBz2InputStream(src);	    	
//			Model taskModel = ModelFactory.createDefaultModel();
//			RDFDataMgr.read(
//				taskModel,
//				in,
//				"http://www.example.org/",
//				Lang.TURTLE);
//
//			Set<String> skips = Collections.emptySet();
////			Set<String> skips = new HashSet<>(Arrays.asList(
////				"http://example.org/scenario6-2",
////				"http://example.org/scenario5-2",
////				"http://example.org/scenario6-5",
////				"http://example.org/scenario9-5",
////				"http://example.org/scenario2-8",
////				"http://example.org/scenario7-2",
////				"http://example.org/scenario2-5"));
//
//			for(String skip : skips) {
//				taskModel.createResource(skip).removeProperties();
//			}
//			
////			taskModel.removeAll(null, BenchmarkVocab.expectedResult, null);
////			taskModel.removeAll(null, BenchmarkVocab.expectedResultSetSize, null);
//			
//			// Get task resources ordered by sequence ID
//			Query query = QueryFactory.create("SELECT DISTINCT ?s { ?s <" + FacetedBrowsingVocab.sequenceId.getURI() + "> ?o } ORDER BY ASC(?o)");
//			
//			Flowable<Resource> result = ReactiveSparqlUtils
//				.execSelectQs(() -> QueryExecutionFactory.create(query, taskModel))
//				.map(qs -> qs.get("s").asResource());
//    			
//			return result;
//		};
//
//	    
//	    @Bean
//	    public TaskGeneratorModule taskGeneratorModule() {
//	    	
//	    	BiFunction<SparqlQueryConnection, SparqlQueryConnection, Flowable<Resource>> fn = (conn, refConn) -> {
//	    		try {
//					return readStaticFile();
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//	    	};
//	    	
//	    	return new TaskGeneratorModuleFacetedBrowsing(fn);
//	    }
	}
