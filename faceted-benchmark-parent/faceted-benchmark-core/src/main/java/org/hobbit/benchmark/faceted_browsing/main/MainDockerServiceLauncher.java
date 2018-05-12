package org.hobbit.benchmark.faceted_browsing.main;


import java.util.Map;

import org.hobbit.benchmark.faceted_browsing.config.ConfigVirtualDockerServiceFactory;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.SpringEnvironmentUtils;
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
        if(args.length != 1) {
            throw new RuntimeException("Exactly 1 argument expected which is the name of a (virtual) docker image to launch");
        }
        
        String imageName = args[0];

        Map<String, String> env = SpringEnvironmentUtils.toStringMap(new StandardEnvironment());

        try {
            logger.info("Service launcher launching virtual image '" + imageName + "' with env " + env);
	        mainCore(imageName, env);
	        logger.info("Service launcher terminated normally with image '" + imageName + "'");
	        System.exit(0);
	    } catch(Exception e) {
            logger.info("Service launcher encountered an exception with image '" + imageName + "'");
            System.exit(1);
	    }	    
	}
	
    public static void mainCore(String imageName, Map<String, String> env) {
	
		// TODO Make it possible to provide the config class from which to obtain the docker service factory
		

		
		// Get the registry and launch an image
		//Map<String, Supplier<SpringApplicationBuilder>> map = ConfigVirtualDockerServiceFactory.getVirtualDockerComponentRegistry();
		DockerServiceFactory<?> dockerServiceFactory = ConfigVirtualDockerServiceFactory.createVirtualComponentDockerServiceFactory();
		
//		map = map.entrySet().stream()
//				.filter(e -> Objects.equals(e.getKey(), imageName))
//				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		//DockerServiceFactory<?> dockerServiceFactory = new DockerServiceFactorySpringApplicationBuilder(map);
		dockerServiceFactory = ConfigVirtualDockerServiceFactory.applyServiceWrappers(dockerServiceFactory);

		DockerService dockerService = dockerServiceFactory.create(imageName, env);
		
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
