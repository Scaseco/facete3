package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.util.Arrays;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.common.launcher.ConfigsFacetedBrowsingBenchmark;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingEncoders;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.component.TaskGeneratorModuleFacetedBrowsing;
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
	    
	    
	    @Bean
	    public TaskGeneratorModule taskGeneratorModule() {
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
	}
