package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.ext.virtuoso.HealthcheckRunner;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceFactory;
import org.hobbit.core.service.docker.api.DockerServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentUtils {
	private static final Logger logger = LoggerFactory.getLogger(ComponentUtils.class);

	public static final String COMPONENT_NAME_KEY = "componentName";
    public static final String DEFAULT_REQUESTED_CONTAINER_TYPE_KEY = "defaultRequstedContainerType";
	

	
//	public static DockerServiceFactory<?> createVirtualComponentDockerServiceFactory() {
//
//		Map<String, Supplier<SpringApplicationBuilder>> virtualDockerComponentRegistry = getVirtualDockerComponentRegistry();
//        DockerServiceFactory<?> result = new DockerServiceFactorySpringApplicationBuilder(virtualDockerComponentRegistry);
//
//		return result;
//	}


	public static <S extends DockerService> DockerService wrapSparqlServiceWithHealthCheck(S dockerService, Function<? super S, String> healthCheckUrlFromServiceDelegate) {
	       
		DockerService result = new AbstractDockerServiceDelegate<S>(dockerService) {
	            // FIXME We want to enhance the startup method within the thread allocated by the guava service
	            @Override
	            public void afterStart() {
	                // The delegate has started, so we have a container id
	                //String host = delegate.getContainerId();
	        		String urlStr = healthCheckUrlFromServiceDelegate.apply(delegate);

	                //String destination = "http://" + host + (port == null ? "" : ":" + port) + "/sparql";
	                
	                URL url = HealthcheckUtils.createUrl(urlStr);
	                
	                new HealthcheckRunner(
	                        60, 1, TimeUnit.SECONDS, () -> {
	                        HealthcheckUtils.checkUrl(url);
	                        
	                        // This part seems to leak connections with jena 3.7.0 as long as the endpoint is not ready
//	                      try (RDFConnection conn = RDFConnectionFactory.connect(destination)) {
//	                          //conn.querySelect("SELECT * { <http://example.org/healthcheck> a ?t }", qs -> {});
//	                          ResultSetFormatter.consume(conn.query("SELECT * { <http://example.org/healthcheck> a ?t }").execSelect());
//	                      }
	                }).run();
	            }
	        };
		
	        return result;
	}
	
	public static DockerService wrapSparqlServiceWithHealthCheck(DockerService dockerService, Integer port) {
		return wrapSparqlServiceWithHealthCheck(dockerService, delegate -> "http://" + delegate.getContainerId() + (port == null ? "" : ":" + port) + "/sparql");
	}


	public static DockerServiceFactory<?> applyServiceWrappers(DockerServiceFactory<?> delegate) {

        // Service wrappers which modifies startup/shutdown of other services; mostly healthchecks
        // on startup
        Map<Pattern, Function<DockerService, DockerService>> serviceWrappers = new LinkedHashMap<>();
        serviceWrappers.put(Pattern.compile("virtuoso"), dockerService -> wrapSparqlServiceWithHealthCheck(dockerService, 8890));
        DockerServiceFactory<?> result = new DockerServiceFactory<DockerService>() {

        	@Override
        	public DockerService create(DockerServiceSpec serviceSpec) {
        		
        		String imageName = serviceSpec.getImageName();
        		
	        	DockerService r = delegate.create(serviceSpec);
	
	        	Map<Pattern, Function<DockerService, DockerService>> cands =
	        			serviceWrappers.entrySet().stream()
	        			.filter(x -> x.getKey().matcher(imageName).find())
	        			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	
	
	        	for(Entry<Pattern, Function<DockerService, DockerService>> cand : cands.entrySet()) {	        		
	        		logger.info("Applying service decorator: " + cand.getKey() + " to docker image " + imageName);
	        		r = cand.getValue().apply(r);
	        	}
	
	        	return r;
        	}
        	
        	@Override
        	public void close() throws Exception {
        		delegate.close();
        	}
        };

        return result;
	}
			
}