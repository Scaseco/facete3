package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.ByteBuffer;
import java.nio.file.Paths;

import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoSystemService;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.hobbit.transfer.Publisher;
import org.hobbit.transfer.PublishingWritableByteChannel;
import org.hobbit.transfer.PublishingWritableByteChannelSimple;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.Service;


public class HobbitLocalConfig {
    @Bean
    public PublishingWritableByteChannel commandChannel() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean
    public PublishingWritableByteChannel dataChannel() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean
    public PublishingWritableByteChannel dg2tg() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean
    public PublishingWritableByteChannel dg2sa() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean
    public PublishingWritableByteChannel tg2sa() {
        return new PublishingWritableByteChannelSimple();
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
    public PseudoHobbitPlatformController defaultCommandHandler(@Qualifier("commandChannel") Publisher<ByteBuffer> commandChannel) {
        PseudoHobbitPlatformController result = new PseudoHobbitPlatformController() ;//BenchmarkControllerFacetedBrowsing.class);
        commandChannel.subscribe(result);
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
    public ServiceFactory<Service> systemUnderTestServiceFactory() {
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

