package org.hobbit.benchmark.faceted_browsing.config;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.ext.virtuoso.HealthcheckRunner;
import org.hobbit.benchmark.faceted_browsing.config.amqp.ConfigCommandChannel;
import org.hobbit.benchmark.faceted_browsing.config.amqp.ConfigDataQueueFactory;
import org.hobbit.benchmark.faceted_browsing.config.amqp.ConfigQueueNameMapper;
import org.hobbit.benchmark.faceted_browsing.config.amqp.ConfigRabbitMqConnection;
import org.hobbit.core.config.ConfigGson;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.service.api.DockerServiceDelegate;
import org.hobbit.core.service.api.ServiceDelegate;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.google.common.collect.ImmutableMap;

public class ComponentUtils {
	private static final Logger logger = LoggerFactory.getLogger(ComponentUtils.class);

	public static final String COMPONENT_NAME_KEY = "componentName";
    public static final String DEFAULT_REQUESTED_CONTAINER_TYPE_KEY = "defaultRequstedContainerType";
	
	
	public static SpringApplicationBuilder createComponentBaseConfig(String componentName, String defaultRequestedContainerType) {
	    SpringApplicationBuilder result = new SpringApplicationBuilder()
	            .properties(new ImmutableMap.Builder<String, Object>()
	                    .put(COMPONENT_NAME_KEY, componentName)
	                    .put(DEFAULT_REQUESTED_CONTAINER_TYPE_KEY, defaultRequestedContainerType)
	                    .build())
	            .sources(ConfigGson.class, ConfigCommunicationWrapper.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommandChannel.class, ConfigQueueNameMapper.class, ConfigDataQueueFactory.class)
	                .child(ConfigDockerServiceManagerClient.class);
	    return result;
	}



	
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

	public static DockerService wrapSparqlServiceWithHealthCheckOld(DockerService dockerService, Integer port) {
    	DockerService result = new DockerServiceDelegate<DockerService>(dockerService) {
    		// FIXME We want to enhance the startup method within the thread allocated by the guava service
    		@Override
    		public ServiceDelegate<DockerService> startAsync() {
    			super.startAsync().awaitRunning();
    			// The delegate has started, so we have a container id
    			String host = delegate.getContainerId();
	        	String destination = "http://" + host + (port == null ? "" : ":" + port) + "/sparql";
    			
	        	URL url = HealthcheckUtils.createUrl(destination);
	        	
	        	new HealthcheckRunner(
	        			60, 1, TimeUnit.SECONDS, () -> {
	        			HealthcheckUtils.checkUrl(url);
	        			
	        			// This part seems to leak connections with jena 3.7.0 as long as the endpoint is not ready
//        		        try (RDFConnection conn = RDFConnectionFactory.connect(destination)) {
//        		            //conn.querySelect("SELECT * { <http://example.org/healthcheck> a ?t }", qs -> {});
//        		            ResultSetFormatter.consume(conn.query("SELECT * { <http://example.org/healthcheck> a ?t }").execSelect());
//        		        }
	        	}).run();
	        	return this;
    		}
    	};
    	
    	return result;
	}


	public static DockerServiceFactory<?> applyServiceWrappers(DockerServiceFactory<?> delegate) {

        // Service wrappers which modifies startup/shutdown of other services; mostly healthchecks
        // on startup
        Map<Pattern, Function<DockerService, DockerService>> serviceWrappers = new LinkedHashMap<>();
        serviceWrappers.put(Pattern.compile("virtuoso"), dockerService -> wrapSparqlServiceWithHealthCheck(dockerService, 8890));
        DockerServiceFactory<?> result = new DockerServiceFactory<DockerService>() {

        	@Override
        	public DockerService create(String imageName, java.util.Map<String,String> env) {
	        	DockerService r = delegate.create(imageName, env);
	
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