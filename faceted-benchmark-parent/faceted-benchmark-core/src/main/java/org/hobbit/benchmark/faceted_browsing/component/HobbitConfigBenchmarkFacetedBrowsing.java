package org.hobbit.benchmark.faceted_browsing.component;

import java.util.function.Supplier;

import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceBuilder;
import org.hobbit.core.utils.CountingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.util.concurrent.Service;



/**
 * This configuration is intended to be loaded programmatically
 * by the Bootstrapping*Controller implementations, because
 * it needs the services provided it
 *
 *
 * @author raven Sep 21, 2017
 *
 */
@Configuration
public class HobbitConfigBenchmarkFacetedBrowsing {
    // It is valid to have dependencies in a configuration class:
    // Section "Working with externalized values" in
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html

    @Autowired
    protected DockerServiceBuilder<DockerService> dockerServiceFactory;



    @Bean
    public Supplier<Service> dataGeneratorServiceFactory() {
        return CountingSupplier.from(count ->
            dockerServiceFactory
                .setImageName("git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator/image")
                .setLocalEnvironment(ImmutableMap.<String, String>builder()
                        .put("NODE_MEM", "1000")
                        .build())
                .get());
    }

    @Bean
    public Supplier<Service> taskGeneratorServiceFactory() {
        return CountingSupplier.from(count ->
            dockerServiceFactory
                .setImageName("git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator/image")
                .setLocalEnvironment(ImmutableMap.<String, String>builder()
                        .build())
                .get());
    }

    @Bean
    public Supplier<Service> evaluationStorageServiceFactory() {
        return CountingSupplier.from(count ->
            dockerServiceFactory
                .setImageName("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0")
                .setLocalEnvironment(ImmutableMap.<String, String>builder()
                        .put("ACKNOWLEDGEMENT_FLAG", "true")
                        .build())
                .get());
    }

}
