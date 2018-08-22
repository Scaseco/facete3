package org.hobbit.benchmark.faceted_browsing.config;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.LocalHobbitComponentServiceFactory;
import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoSystemService;
import org.apache.jena.rdfconnection.RDFConnection;
import org.hobbit.core.component.BenchmarkControllerComponentImpl;
import org.hobbit.core.component.DataGeneratorComponentImpl;
import org.hobbit.core.component.DefaultEvaluationStorage;
import org.hobbit.core.component.EvaluationModuleComponent;
import org.hobbit.core.component.PseudoHobbitPlatformController;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmark;
import org.hobbit.core.data.Result;
import org.hobbit.core.service.api.ServiceBuilder;
import org.hobbit.core.storage.Storage;
import org.hobbit.core.storage.StorageInMemory;
import org.hobbit.rdf.component.SystemAdapterRDFConnection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.Service;

import io.reactivex.Flowable;



//class LocalChannels {
//
//
//}

//@Configuration(LocalChannels.class)
@Deprecated // Should not be used anymore
public class ConfigFacetedBenchmarkV1LocalServices {


//    @Bean
//    public DockerServiceBuilder<DockerService> dockerServiceManagerClientComponent() {
//        return new DockerServiceManagerClientComponent(null, null, null);
//    }

    @Bean
    public ServiceBuilder<Service> benchmarkControllerServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(BenchmarkControllerComponentImpl.class);
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


//    @Bean
//    public TripleStreamSupplier dataGenerationMethod() {
//        return () -> {
//			try {
//				return PodiggWrapper.test();
//			} catch (IOException | InterruptedException e) {
//				throw new RuntimeException(e);
//			}
//		};
//    }

    @Bean
    public ServiceBuilder<Service> dataGeneratorServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(DataGeneratorComponentImpl.class);
    }

    @Bean
    public ServiceBuilder<Service> taskGeneratorServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(TaskGeneratorFacetedBenchmark.class);
    }

    @Bean
    public ServiceBuilder<Service> systemAdapterServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(SystemAdapterRDFConnection.class);
    }

    @Bean
    public ServiceBuilder<Service> evaluationStorageServiceFactory() {
        return new LocalHobbitComponentServiceFactory<>(DefaultEvaluationStorage.class);
    }


    @Bean
    public ServiceBuilder<Service> evaluationModuleServiceFactory() {
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

