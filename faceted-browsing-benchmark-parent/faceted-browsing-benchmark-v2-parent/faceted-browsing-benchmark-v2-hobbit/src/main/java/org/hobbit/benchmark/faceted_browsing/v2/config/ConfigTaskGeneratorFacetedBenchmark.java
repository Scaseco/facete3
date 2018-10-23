package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.hobbit.benchmark.common.launcher.ConfigsFacetedBrowsingBenchmark;
import org.hobbit.benchmark.faceted_browsing.component.TaskGeneratorModuleFacetedBrowsing;
import org.hobbit.core.component.TaskGeneratorModule;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.springframework.context.annotation.Bean;

import io.reactivex.Flowable;

// Configuration for the worker task generator fo the faceted browsing benchmark
	public class ConfigTaskGeneratorFacetedBenchmark {
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
//					try {
						// FIXME Return some flow of tasks
						return null;
						//return Flowable.fromIterable(FacetedTaskGeneratorOld.runTaskGenerationCore(conn, refConn).collect(Collectors.toList()));
//					} catch (IOException e) {
//						throw new RuntimeException(e);
//					}
				};
	    	
	    	//List<Resource> tasks = FacetedTaskGeneratorOld.runTaskGenerationCore(conn, refConn).collect(Collectors.toList());
	    	//Flowable<Resource> t2 = Flowable.fromIterable(tasks);
	    	
	    	return new TaskGeneratorModuleFacetedBrowsing(fn);
	    }
	}