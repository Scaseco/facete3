package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoSystemService;
import org.apache.jena.rdfconnection.RDFConnection;
import org.hobbit.core.services.ServiceFactory;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.hobbit.transfer.Publisher;
import org.hobbit.transfer.PublishingWritableByteChannel;
import org.hobbit.transfer.PublishingWritableByteChannelSimple;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.Service;


public class HobbitLocalConfig {

    /*
     * Standard channels
     */

    @Bean(name={"commandChannel", "commandPublisher"})
    public PublishingWritableByteChannel commandChannel() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name={"dataChannel", "dataPublisher"})
    public PublishingWritableByteChannel dataChannel() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name={"dg2tg","dg2tgPub"})
    public PublishingWritableByteChannel dg2tg() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name={"dg2sa","dg2saPub"})
    public PublishingWritableByteChannel dg2sa() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name={"tg2sa","tg2saPub"})
    public PublishingWritableByteChannel tg2sa() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name={"tg2es","tg2esPub"})
    public PublishingWritableByteChannel tg2es() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name={"sa2es","sa2esPub"})
    public PublishingWritableByteChannel sa2es() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name={"es2em","es2emPub"})
    public PublishingWritableByteChannel es2em() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name={"em2es","em2esPub"})
    public PublishingWritableByteChannel em2es() {
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
    public PseudoHobbitPlatformController defaultCommandHandler(@Qualifier("commandPublisher") Publisher<ByteBuffer> commandChannel) {
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
        return new LocalHobbitComponentServiceFactory<>(TaskGeneratorFacetedBenchmark.class);
    }

    @Bean
    public ServiceFactory<Service> systemAdapterServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(SystemAdapterRDFConnection.class);
    }


    /**
     * The RDF connection supplier to use by the system adapter
     *
     * @return
     */
    @Bean
    public Supplier<RDFConnection> systemUnderTestRdfConnectionSupplier() {
        return () -> null;
    }

    @Bean
    public ServiceFactory<Service> evaluationStorageServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(InMemoryEvaluationStorage.class);
    }


    @Bean
    public ServiceFactory<Service> evaluationModuleServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(EvaluationModuleComponent.class);
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

