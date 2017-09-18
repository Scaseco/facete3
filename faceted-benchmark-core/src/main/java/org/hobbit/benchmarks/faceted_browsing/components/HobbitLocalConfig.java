package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.file.Paths;

import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoSystemService;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.Service;


public class HobbitLocalConfig {
    @Bean
    public ObservableByteChannel commandChannel() {
        return new ObservableByteChannel();
    }

    @Bean
    public ObservableByteChannel dataChannel() {
        return new ObservableByteChannel();
    }

    @Bean
    public ObservableByteChannel dg2tg() {
        return new ObservableByteChannel();
    }

    @Bean
    public ObservableByteChannel dg2sa() {
        return new ObservableByteChannel();
    }

    @Bean
    public ObservableByteChannel tg2sa() {
        return new ObservableByteChannel();
    }


    @Bean
    public ServiceFactory<Service> benchmarkControllerServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(BenchmarkControllerFacetedBrowsing.class);
    }

    /**
     * Register the default command handler
     * @param commandChannel
     * @return
     */
    @Bean
    public PseudoHobbitPlatformController defaultCommandHandler(@Qualifier("commandChannel") ObservableByteChannel commandChannel) {
        PseudoHobbitPlatformController result = new PseudoHobbitPlatformController() ;//BenchmarkControllerFacetedBrowsing.class);
        commandChannel.addObserver(result);
        return result;
    }


    @Bean
    public TripleStreamSupplier dataGenerationMethod() {
        return () -> PodiggWrapper.test();
    }

    @Bean
    public ServiceFactory<Service> dataGeneratorServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(DataGeneratorFacetedBrowsing.class);
    }

    @Bean
    public ServiceFactory<Service> taskGeneratorServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(FacetedTaskGenerator.class);
    }

    @Bean
    public SparqlBasedSystemService preparationSparqlService() {
        VirtuosoSystemService result = new VirtuosoSystemService(
                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit_1112_8891/virtuoso.ini"));

        return result;
    }

//    @Bean
//    public SparqlBasedSystemService referenceSparqlService() {
//    }
}

