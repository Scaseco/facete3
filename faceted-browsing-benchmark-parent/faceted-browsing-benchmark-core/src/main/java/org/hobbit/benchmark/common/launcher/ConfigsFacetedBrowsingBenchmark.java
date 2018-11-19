package org.hobbit.benchmark.common.launcher;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.hobbit.benchmark.faceted_browsing.config.HobbitSdkConstants;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.service.api.SparqlDockerApiService;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ServiceManager.Listener;


public class ConfigsFacetedBrowsingBenchmark {

	/***
	 * The BenchmarkLauncher is the entry point for launching benchmarks locally.
	 * (For those familiar with the Hobbit platform: This launcher does, what the platform does when launching a platform)
	 * 
	 * It takes a configuration of a registry of docker image names plus
	 * the image names of the benchmark controller and system adapter which to start from the registry
	 * 
	 * @author Claus Stadler, Nov 18, 2018
	 *
	 */
	public static class BenchmarkLauncher
		implements EnvironmentAware
	{
		
		private static final Logger logger = LoggerFactory.getLogger(ConfigsFacetedBrowsingBenchmark.BenchmarkLauncher.class);

		protected Environment env;
		
		
		
		
		
		// Hack from https://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
		public static void setEnv(Map<String, String> newenv) throws Exception {
		    try {
		      Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		      Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
		      theEnvironmentField.setAccessible(true);
		      Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
		      env.putAll(newenv);
		      Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
		      theCaseInsensitiveEnvironmentField.setAccessible(true);
		      Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
		      cienv.putAll(newenv);
		    } catch (NoSuchFieldException e) {
		      Class[] classes = Collections.class.getDeclaredClasses();
		      Map<String, String> env = System.getenv();
		      for(Class cl : classes) {
		        if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
		          Field field = cl.getDeclaredField("m");
		          field.setAccessible(true);
		          Object obj = field.get(env);
		          Map<String, String> map = (Map<String, String>) obj;
		          map.clear();
		          map.putAll(newenv);
		        }
		      }
		    }
		  }
		public void mockSa() throws Exception {
		    Map<String, String> map = new HashMap<>(System.getenv());
		    map.put(Constants.HOBBIT_SESSION_ID_KEY, env.getRequiredProperty(Constants.HOBBIT_SESSION_ID_KEY));
		    map.put(Constants.RABBIT_MQ_HOST_NAME_KEY, env.getProperty(Constants.RABBIT_MQ_HOST_NAME_KEY, "localhost"));
		    setEnv(map);
		    
		    AbstractSystemAdapter asa = new AbstractSystemAdapter() {
                
                @Override
                public void receiveGeneratedTask(String taskId, byte[] data) {
                	
                	String queryString = RabbitMQUtils.readString(data);
                    System.out.println("SA Received task " + taskId + ": " + queryString);
                    try {
						sendResultToEvalStorage(taskId, RabbitMQUtils.writeString(""));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
                }
                
                @Override
                public void receiveGeneratedData(byte[] data) {
                	
                    System.out.println("SA Received some data");
                    
                }
            };
            

            asa.init();
		}
		
		

		/**
		 * FIXME Make SA configurable (hard coded to jena)
		 * 
		 * @param dockerServiceBuilderFactory
		 * @param commandSender
		 * @return
		 */
		@Bean
		public ApplicationRunner benchmarkLauncher(
				@Value("${" + HobbitSdkConstants.BC_IMAGE_NAME_KEY + "}") String bcImageName,
				@Value("${" + HobbitSdkConstants.SA_IMAGE_NAME_KEY + "}") String saImageName,
				//BenchmarkConfig benchmarkConfig,
				DockerServiceBuilderFactory<?> dockerServiceBuilderFactory,
				@Qualifier("commandSender") Subscriber<ByteBuffer> commandSender		
		) {
			return args -> {
				try {
					logger.info("BenchmarkLauncher starting");
					logger.info("  BC Image: " + bcImageName);
					logger.info("  SA Image: " + saImageName);
				
					// The service builder factory is pre-configured to set Constants.HOBBIT_SESSION_ID_KEY
					
					Map<String, String> serviceEnv = new HashMap<>();
					serviceEnv.put(Constants.HOBBIT_SESSION_ID_KEY, env.getRequiredProperty(Constants.HOBBIT_SESSION_ID_KEY));

//					String bcImageName = benchmarkConfig.getBenchmarkControllerImageName();

//					String bcImageName = "git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/faceted-browsing-benchmark-v1-benchmark-controller";
//					String saImageName = "git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/system-adapter-mocha-jena-in-memory";

					//String saImageName = benchmarkConfig.get
					
					// "git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image"
					// git.project-hobbit.eu:4567/gkatsibras/facetedbenchmarkcontroller/image
					
					// Launch the system adapter
					DockerService saService = dockerServiceBuilderFactory.get()
						.setImageName(saImageName)
						//.setLocalEnvironment(serviceEnv)
						.get();
					
					
					// Launch the benchmark
					Service bcService = dockerServiceBuilderFactory.get()
						.setImageName(bcImageName)
						//.setLocalEnvironment(serviceEnv)
						.get();
	
	//				Service esService = dockerServiceBuilderFactory.get()
	//						.setImageName("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0")
	//						.setLocalEnvironment(ImmutableMap.<String, String>builder().build())
	//						.get();
	
	//				esService.startAsync().awaitRunning();
	//				esService.stopAsync().awaitTerminated(5, TimeUnit.SECONDS);
	//				
	//				if(true) {
	//					System.out.println("yay");
	//					return;
	//				}
					
					//mockAbstractSa();

					// Ensure that both the SA and BC are healthy before sending out the start signal
					// This ensures that the BC is ready to accept the start event
					ServiceManager serviceManager = new ServiceManager(Arrays.asList(saService, bcService));
					serviceManager.addListener(new Listener() {
						@Override
						public void healthy() {
							String saContainerId = saService.getContainerId();
							commandSender.onNext(ByteBuffer.wrap(Bytes.concat(
								new byte[] {Commands.START_BENCHMARK_SIGNAL},
								saContainerId.getBytes(StandardCharsets.UTF_8)
							)));							
						}
					});

					serviceManager.startAsync();
					try {					
						// Wait for the bc to finish
						bcService.awaitTerminated();
					} finally {
						saService.awaitTerminated(10, TimeUnit.SECONDS);
					}
				} finally {
					logger.info("BenchmarkLauncher terminating");
				}
			};
		}



		@Override
		public void setEnvironment(Environment environment) {
			this.env = environment;
		}
	}
	
	
	static final Logger logger = LoggerFactory.getLogger(ConfigsFacetedBrowsingBenchmark.class);

	// TODO I think this method is no longer needed, as the service wrapping is done by
	// applySericeWrappers
	public static SparqlBasedService createVirtuosoSparqlService(String imageName, DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
    	DockerService service = dockerServiceBuilderFactory.get()
//	    			.setImageName("tenforce/virtuoso:virtuoso7.2.4")
    			//"tenforce/virtuoso"
    			.setImageName(imageName)
    			.setLocalEnvironment(ImmutableMap.<String, String>builder()
    					.put("SPARQL_UPDATE", "true")
    					.put("VIRT_SPARQL_ResultSetMaxRows", "1000000")
    					.build())
    			.get();

    	SparqlBasedService result = new SparqlDockerApiService(service) {
    		public Supplier<RDFConnection> getApi() {
    			Supplier<RDFConnection> result;
    			if(isRunning()) {
        			String host = delegate.getContainerId();
        			String baseUrl = "http://" + host + ":" + "8890";
        			
        			result = () -> RDFConnectionFactory.connect(baseUrl + "/sparql", baseUrl + "/sparql", baseUrl + "/sparql-graph-crud/");
        			
        			//result = () -> RDFConnectionFactory.connect(baseUrl);
        			
        			logger.info("Sparql endpoint of image " + imageName + " online at: " + baseUrl);
        		} else {
        			throw new IllegalStateException("Can only access API of running services");
        		}
    			return result;
    		}
    	};
    	
    	return result;	    
	}

	// Configuration for the worker of the system adapter
//	public static class ConfigTaskGeneratorFacetedBenchmark {
//	    @Bean
//	    public SparqlBasedSystemService taskGeneratorSparqlService() {
//	        VirtuosoSystemService result = new VirtuosoSystemService(
//	                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
//	                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit-system-under-test_1113_8892/virtuoso.ini"));
//
//		        return result;
//	    }
//	}

}
