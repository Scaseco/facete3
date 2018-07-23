package org.hobbit.benchmark.faceted_browsing.main;

import java.util.Collections;
import java.util.HashMap;

import org.aksw.jena_sparql_api.http.HttpExceptionUtils;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.hobbit.benchmark.faceted_browsing.config.ComponentUtils;
import org.hobbit.benchmark.faceted_browsing.config.DockerServiceFactoryDockerClient;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceBuilder;
import org.hobbit.core.service.docker.DockerServiceBuilderJsonDelegate;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class MainTestBulkLoad {
	
	private static final Logger logger = LoggerFactory.getLogger(MainTestBulkLoad.class);

	
	public static void main(String[] args) throws Exception {
		try(DockerServiceFactory<?> dsf = DockerServiceFactoryDockerClient.create(true, new HashMap<>(), Collections.singleton("hobbit"))) {
			DockerServiceFactory<?> core = ComponentUtils.applyServiceWrappers(dsf);
			DockerServiceBuilder<?> dsb = DockerServiceBuilderJsonDelegate.create(core::create);

	    	DockerService service = dsb
	    		.setImageName("tenforce/virtuoso")
				//.setImageName("git.project-hobbit.eu:4567/mspasic/virtuoso")
				.setLocalEnvironment(ImmutableMap.<String, String>builder()
						.put("SPARQL_UPDATE", "true")
						.put("VIRT_SPARQL_ResultSetMaxRows", "50000")
						.build())
				.get();
	
	    	logger.info("Starting up...");
	    	service.startAsync().awaitRunning();
	    	try {
	    		String graphName = "http://example.org/default-1.ttl";
	    		String host = "http://" + service.getContainerId() + ":8890/";
	    		try(RDFConnection conn = RDFConnectionFactory.connect(host + "sparql", host + "sparql", host + "sparql-graph-crud/")) {
//	    			conn.load(graphName, "/home/raven/Projects/Eclipse/faceted-benchmark-parent/faceted-benchmark-parent/faceted-benchmark-core/src/main/resources/podigg-lc-small.ttl");
	    			conn.load(graphName, "/home/raven/Projects/Data/Hobbit/monaco-lgd4a.nt");
	    		}
	    	} catch (Exception e) {
	    		throw HttpExceptionUtils.makeHumanFriendly(e);
	    	} finally {
	    		logger.info("Shutting down...");
	    		service.stopAsync().awaitTerminated();
	    	}
		}
	    logger.info("Done");
	}
}
