package org.hobbit.benchmark.faceted_browsing.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hobbit.core.component.DataGeneratorFacetedBrowsing;
import org.hobbit.core.component.EvaluationModuleComponent;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmark;
import org.hobbit.core.components.test.InMemoryEvaluationStore;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.Service;


public class ConfigDockerServiceFactoryHobbitFacetedBenchmarkLocal {

	
    @Bean
    public DockerServiceFactory<?> dockerServiceManagerComponent() throws TimeoutException {
        Map<String, Class<?>> imageNameToClass = new HashMap<>();
        imageNameToClass.put("git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator/image", DataGeneratorFacetedBrowsing.class);
        imageNameToClass.put("git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator/image", TaskGeneratorFacetedBenchmark.class);
        imageNameToClass.put("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0", InMemoryEvaluationStore .class);
        imageNameToClass.put("git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule/image", EvaluationModuleComponent.class);

        
        DockerServiceFactory<?> result = DockerServiceFactoryUtilsSpringBoot.createDockerServiceFactoryForBootstrap(imageNameToClass,
        		() -> new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF)
        );
        
//        Docker
//
//        BiFunction<String, Map<String, String>, DockerService> serviceFactory = (imageName, env) -> {        	
//        	if(!imageNameToClass.containsKey(imageName)) {
//        		throw new RuntimeException("No image " + imageName + " in the registry");
//        	}
//
//        	Class<?> ctx = imageNameToClass.get(imageName);
//        	Objects.requireNonNull(ctx);
//        	
//        	
//        	new DockerServiceSimpleDelegation(
//        			imageName,
//        			startServiceDelegate,
//        			stopServiceDelegate);
//        	
//        };
        
        // FIXME Get this mapping working: We may need a preconfigured env + launcher in addition to the class
        //DockerServiceFactorySimpleDelegation


        //DockerServiceManagerServerComponent result = new DockerServiceManagerServerComponent(null, null, null, null, null);
        return result;
    }

    @Bean
    //@Autowired
    public String dummy(@Qualifier("dockerServiceManagerComponent") Service service) throws TimeoutException {
        //Thread.dumpStack();

        service.startAsync();
        service.awaitRunning(60, TimeUnit.SECONDS);
        return "dummy";
    }
}
