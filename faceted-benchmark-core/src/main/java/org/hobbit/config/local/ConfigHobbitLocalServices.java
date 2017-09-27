package org.hobbit.config.local;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoSystemService;
import org.apache.jena.rdfconnection.RDFConnection;
import org.hobbit.benchmarks.faceted_browsing.components.BenchmarkControllerFacetedBrowsing;
import org.hobbit.benchmarks.faceted_browsing.components.DataGeneratorFacetedBrowsing;
import org.hobbit.benchmarks.faceted_browsing.components.DefaultEvaluationStorage;
import org.hobbit.benchmarks.faceted_browsing.components.EvaluationModuleComponent;
import org.hobbit.benchmarks.faceted_browsing.components.LocalHobbitComponentServiceFactory;
import org.hobbit.benchmarks.faceted_browsing.components.PodiggWrapper;
import org.hobbit.benchmarks.faceted_browsing.components.PseudoHobbitPlatformController;
import org.hobbit.benchmarks.faceted_browsing.components.Storage;
import org.hobbit.benchmarks.faceted_browsing.components.StorageInMemory;
import org.hobbit.benchmarks.faceted_browsing.components.SystemAdapterRDFConnection;
import org.hobbit.benchmarks.faceted_browsing.components.TaskGeneratorFacetedBenchmark;
import org.hobbit.core.components.test.InMemoryEvaluationStore;
import org.hobbit.core.data.Result;
import org.hobbit.core.services.DockerService;
import org.hobbit.core.services.DockerServiceFactory;
import org.hobbit.core.services.DockerServiceFactorySimpleDelegation;
import org.hobbit.core.services.DockerServiceManagerClientComponent;
import org.hobbit.core.services.DockerServiceManagerServerComponent;
import org.hobbit.core.services.ServiceFactory;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.hobbit.transfer.Publisher;
import org.hobbit.transfer.PublishingWritableByteChannel;
import org.hobbit.transfer.PublishingWritableByteChannelQueued;
import org.hobbit.transfer.PublishingWritableByteChannelSimple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;



//class LocalChannels {
//
//
//}

//@Configuration(LocalChannels.class)
public class ConfigHobbitLocalServices {


    @Bean
    public DockerServiceFactory<DockerService> dockerServiceManagerClientComponent() {
        return new DockerServiceManagerClientComponent();
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
    public PseudoHobbitPlatformController defaultCommandHandler(@Qualifier("commandPub") Publisher<ByteBuffer> commandChannel) {
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
        return new LocalHobbitComponentServiceFactory<>(DefaultEvaluationStorage.class);
    }


    @Bean
    public ServiceFactory<Service> evaluationModuleServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(EvaluationModuleComponent.class);
    }



    // Used by the DefaultEvaluationStorage
    @Bean
    public Storage<String, Result> storage() {
        return new StorageInMemory<>();
    }

    @Bean
    public SparqlBasedSystemService preparationSparqlService() {
        VirtuosoSystemService result = new VirtuosoSystemService(
                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit_1112_8891/virtuoso.ini"));

        return result;
    }

//    @Bean
//    public SparqlBasedSystemService preparationSparqlService() {
//    }



//    @Bean
//    public SparqlBasedSystemService referenceSparqlService() {
//    }
}

