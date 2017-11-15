package org.hobbit.benchmark.faceted_browsing.config;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.LocalHobbitComponentServiceFactory;
import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoSystemService;
import org.apache.jena.rdfconnection.RDFConnection;
import org.hobbit.core.component.BenchmarkControllerFacetedBrowsing;
import org.hobbit.core.component.DataGeneratorFacetedBrowsing;
import org.hobbit.core.component.DefaultEvaluationStorage;
import org.hobbit.core.component.EvaluationModuleComponent;
import org.hobbit.core.component.PseudoHobbitPlatformController;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmark;
import org.hobbit.core.data.Result;
import org.hobbit.core.service.api.ServiceFactory;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.DockerServiceManagerClientComponent;
import org.hobbit.core.storage.Storage;
import org.hobbit.core.storage.StorageInMemory;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.hobbit.rdf.component.SystemAdapterRDFConnection;
import org.hobbit.service.podigg.PodiggWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.Service;

import io.reactivex.Flowable;



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
    public PseudoHobbitPlatformController defaultCommandHandler(@Qualifier("commandPub") Flowable<ByteBuffer> commandChannel) {
        PseudoHobbitPlatformController result = new PseudoHobbitPlatformController() ;//BenchmarkControllerFacetedBrowsing.class);
        commandChannel.subscribe(result::accept);
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
    public SparqlBasedSystemService taskGeneratorSparqlService() {
        VirtuosoSystemService result = new VirtuosoSystemService(
                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit-task-generation_1112_8891/virtuoso.ini"));

        return result;
    }

    @Bean
    public SparqlBasedSystemService systemUnderTestService() {
        VirtuosoSystemService result = new VirtuosoSystemService(
                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit-system-under-test_1113_8892/virtuoso.ini"));

        return result;
    }

    /**
     * The RDF connection supplier to use by the system adapter
     *
     * @return
     */
    @Bean
    public Supplier<RDFConnection> systemUnderTestRdfConnectionSupplier(@Qualifier("systemUnderTestService") SparqlBasedSystemService sparqlService) {
        return () -> sparqlService.createDefaultConnection();
    }


//    @Bean
//    public SparqlBasedSystemService preparationSparqlService() {
//    }



//    @Bean
//    public SparqlBasedSystemService referenceSparqlService() {
//    }
}

