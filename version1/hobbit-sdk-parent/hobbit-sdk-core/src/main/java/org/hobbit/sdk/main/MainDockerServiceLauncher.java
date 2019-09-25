package org.hobbit.sdk.main;


import java.util.Map;
import java.util.Optional;

import org.apache.jena.sys.JenaSystem;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderFactory;
import org.hobbit.core.service.docker.impl.spring_boot.util.SpringEnvironmentUtils;
import org.hobbit.sdk.docker.registry.DockerServiceRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.StandardEnvironment;


/**
 * Command line runner for launching a (virtual) docker service
 * 
 * @author raven Dec 12, 2017
 *
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class MainDockerServiceLauncher {

    private static final Logger logger = LoggerFactory.getLogger(MainDockerServiceLauncher.class);
    
	public static void main(String[] args) {
		JenaSystem.init();
		
        if(args.length != 1) {
            throw new RuntimeException("Exactly 1 argument expected which is the name of a (virtual) docker image to launch");
        }
        
        String imageName = args[0];

        Map<String, String> env = SpringEnvironmentUtils.toStringMap(new StandardEnvironment());

        try {
            logger.info("Service launcher launching virtual image '" + imageName);
	        logger.debug("Environment: " + env);
            mainCore(imageName, env);
	        logger.info("Service launcher terminated normally with image '" + imageName + "'");
	        System.exit(0);
	    } catch(Exception e) {
            logger.info("Service launcher encountered an exception with image '" + imageName + "'", e);
            System.exit(1);
	    }	    
	}
	
    public static void mainCore(String imageName, Map<String, String> env) {
	
		// TODO Make it possible to provide the config class from which to obtain the docker service factory
		

		
		// Get the registry and launch an image
		//Map<String, Supplier<SpringApplicationBuilder>> map = ConfigVirtualDockerServiceFactory.getVirtualDockerComponentRegistry();

    	// TODO The class providing the config docker service beans must be configurable...
    	//DockerServiceFactory<?> dockerServiceFactory = null; //ComponentUtils.createVirtualComponentDockerServiceFactory();
		
    	Map<String, DockerServiceBuilderFactory<?>> serviceFactoryMap = DockerServiceRegistryImpl.get().getServiceFactoryMap();
    	DockerServiceBuilderFactory<?> dockerServiceBuilder = Optional.ofNullable(serviceFactoryMap.get(imageName))
    			.orElseThrow(() -> new NullPointerException("No registry entry for docker image " + imageName + "; Available: " + serviceFactoryMap));

    	DockerService dockerService = dockerServiceBuilder.get().setLocalEnvironment(env).get();
    	
//		map = map.entrySet().stream()
//				.filter(e -> Objects.equals(e.getKey(), imageName))
//				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		//DockerServiceFactory<?> dockerServiceFactory = new DockerServiceFactorySpringApplicationBuilder(map);
		//dockerServiceFactory = ComponentUtils.applyServiceWrappers(dockerServiceFactory);

		//DockerService dockerService = dockerServiceFactory.create(imageName, env);
		
		logger.info("Service launcher waiting for termination...");
		dockerService.startAsync().awaitTerminated();
	}

//	@Inject
//	protected Environment env;
//	
//	public static class Config {
//		ApplicationRunner appRunner(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
//			return args -> {
//				List<String> nonOptionArgs = args.getNonOptionArgs();
//				if(nonOptionArgs.size() != 1) {
//					throw new RuntimeException("Exactly 1 argument expected which is the name of a (virtual) docker image to launch");
//				}
//				
//				String imageName = nonOptionArgs.get(0);
//
//				Map<String, String> localEnv = null;
//				DockerService dockerService = dockerServiceBuilderFactory.get()
//					.setImageName(imageName)
//					.setLocalEnvironment(localEnv)
//					.get();
//				
//				dockerService.startAsync().awaitTerminated();
//			};
//		}
//	}
//	
//	
//	public static void main(String[] args) {
//		// Get the registry and launch an image
//		
//		try(ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
//			.sources(ConfigDockerServiceFactory.class)
//				.child(Config.class)
//				.run()) {			
//		}
//	}
}
