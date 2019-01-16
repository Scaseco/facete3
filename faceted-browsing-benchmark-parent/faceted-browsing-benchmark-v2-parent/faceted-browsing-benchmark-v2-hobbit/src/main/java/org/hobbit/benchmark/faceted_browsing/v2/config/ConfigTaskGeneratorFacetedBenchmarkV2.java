package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
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
import org.hobbit.benchmark.faceted_browsing.v2.main.MainFacetedBrowsingBenchmarkV2Run;
import org.hobbit.core.component.BenchmarkVocab;
import org.hobbit.core.component.TaskGeneratorModule;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderFactory;
import org.springframework.context.annotation.Bean;

import io.reactivex.Flowable;

// Configuration for the worker task generator fo the faceted browsing benchmark
	public class ConfigTaskGeneratorFacetedBenchmarkV2 {
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
	    
	  
	    public static Flowable<Resource> readStaticFile() throws IOException {

	    	InputStream in = MainFacetedBrowsingBenchmarkV2Run.openBz2InputStream("hobbit-sensor-stream-150k-events-tasks.ttl.bz2");	    	
			Model taskModel = ModelFactory.createDefaultModel();
			RDFDataMgr.read(
				taskModel,
				in,
				"http://www.example.org/",
				Lang.TURTLE);
    		
			Set<String> skips = new HashSet<>(Arrays.asList(
				"http://example.org/scenario6-2",
				"http://example.org/scenario5-2",
				"http://example.org/scenario6-5",
				"http://example.org/scenario9-5",
				"http://example.org/scenario2-8",
				"http://example.org/scenario7-2",
				"http://example.org/scenario2-5"));

			for(String skip : skips) {
				taskModel.createResource(skip).removeProperties();
			}
			
//			taskModel.removeAll(null, BenchmarkVocab.expectedResult, null);
//			taskModel.removeAll(null, BenchmarkVocab.expectedResultSetSize, null);
			
			// Get task resources ordered by sequence ID
			Query query = QueryFactory.create("SELECT DISTINCT ?s { ?s <" + FacetedBrowsingVocab.sequenceId.getURI() + "> ?o } ORDER BY ASC(?o)");
			
			Flowable<Resource> result = ReactiveSparqlUtils
				.execSelectQs(() -> QueryExecutionFactory.create(query, taskModel))
				.map(qs -> qs.get("s").asResource());
    			
			return result;
		};

	    
	    @Bean
	    public TaskGeneratorModule taskGeneratorModule() {
	    	
	    	BiFunction<SparqlQueryConnection, SparqlQueryConnection, Flowable<Resource>> fn = (conn, refConn) -> {
	    		try {
					return readStaticFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
	    	};
	    	
	    	return new TaskGeneratorModuleFacetedBrowsing(fn);
	    }
	}
