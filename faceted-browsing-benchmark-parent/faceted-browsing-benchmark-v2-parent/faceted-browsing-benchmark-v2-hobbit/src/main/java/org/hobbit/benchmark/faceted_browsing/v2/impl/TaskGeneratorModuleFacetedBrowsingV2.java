package org.hobbit.benchmark.faceted_browsing.v2.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.hobbit.benchmark.faceted_browsing.component.TaskGeneratorModuleFacetedBrowsing;
import org.hobbit.core.component.DataGeneratorComponentBase;
import org.hobbit.core.component.DataProtocol;
import org.hobbit.core.component.TaskGeneratorModule;
import org.hobbit.core.utils.ServiceManagerUtils;
import org.hobbit.rdf.component.RdfBulkLoadProtocolMocha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

import io.reactivex.Flowable;

/**
 * TODO This class seems quite flawed... the task generation and sparql endpoint stuff are separate, although they belong together
 * 
 * 
 * Note: The life cycle of this worker is managed by the TaskGeneratorComponent
 * 
 * @author raven Nov 21, 2017
 *
 */
public class TaskGeneratorModuleFacetedBrowsingV2
	implements TaskGeneratorModule
{
	
	private static final Logger logger = LoggerFactory.getLogger(TaskGeneratorModuleFacetedBrowsing.class);

	
    @javax.annotation.Resource(name="taskGeneratorSparqlService")
    protected SparqlBasedService sparqlService;

    protected DataProtocol dataHandler;
    
    
    protected transient ServiceManager serviceManager;
    
    protected CompletableFuture<Void> dataLoadingComplete = new CompletableFuture<>();

    //@javax.annotation.Resource
    protected BiFunction<? super SparqlQueryConnection, ? super SparqlQueryConnection, ? extends Flowable<? extends Resource>> taskSupplierFactory;
    
    
    public TaskGeneratorModuleFacetedBrowsingV2(BiFunction<? super SparqlQueryConnection, ? super SparqlQueryConnection, ? extends Flowable<? extends Resource>> taskSupplierFactory) {
    	this.taskSupplierFactory = taskSupplierFactory;
    }
    
    public CompletableFuture<Void> getDataLoadingComplete() {
		return dataLoadingComplete;
	}


	//@PostConstruct
	@Override
	public void startUp() throws Exception {
        Set<Service> services = Sets.newIdentityHashSet();
        services.addAll(Arrays.asList(sparqlService));

        serviceManager = new ServiceManager(services);
        
        //sparqlService.startAsync().awaitRunning();
        
        logger.info("TaskGeneratorWorker::startUp(): Waiting for SPARQL service to become ready");
        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(serviceManager,
                60, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);
        logger.info("TaskGeneratorWorker::startUp(): SPARQL service is now ready");
        
        RDFConnection conn = sparqlService.createDefaultConnection();
        dataHandler = new RdfBulkLoadProtocolMocha(conn, "http://example.org", () -> {}, () -> {
        	dataLoadingComplete.complete(null);
        });
        
        logger.info("TaskGeneratorWorker::startUp(): Startup is complete");
	}


    //@PreDestroy
	@Override
	public void shutDown() throws Exception {
        logger.info("TaskGeneratorWorker::shutDown() - Stopping preparation sparql service");
        ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);
        logger.info("TaskGeneratorWorker::shutDown() - Stopped preparation sparql service");    	

	
	}


	@Override
	public void loadDataFromStream(InputStream tmpIn) {
		try(RDFConnection conn = sparqlService.createDefaultConnection()) {
			loadDataFromStream(tmpIn, conn);
		} catch (IOException f) {
			f.printStackTrace();
			throw new RuntimeException(f);
		}
	}
	

    
    
	//public Stream<Resource>
	@Override
	public Stream<? extends Resource> generateTasks() {
		Stream<? extends Resource> result = runTaskGeneration().stream();
		return result;
	}
	
	
	
	
    public List<? extends Resource> runTaskGeneration() {

        List<? extends Resource> tasks;

        try(RDFConnection conn = sparqlService.createDefaultConnection();
    		  RDFConnection refConn = sparqlService.createDefaultConnection()) {
    	
//            try {
				//tasks = FacetedTaskGeneratorOld.runTaskGenerationCore(conn, refConn).collect(Collectors.toList());
        		tasks = taskSupplierFactory.apply(conn, refConn).toList().blockingGet();
        	//tasks = taskSupplier.toList().blockingGet();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
    	
            logger.info("TaskGenerator created " + tasks.size() + " tasks");

        }
        return tasks;
    }    

	
    public static void loadDataFromStream(InputStream tmpIn, RDFConnection conn) throws IOException {
        try(InputStream in = tmpIn) {
            logger.info("Data stream from data generator received");

                try {
                    // Perform bulk load
                    File tmpFile = File.createTempFile("hobbit-faceted-browsing-benchmark-task-generator-bulk-load-", ".nt");
                    tmpFile.deleteOnExit();
                    FileCopyUtils.copy(in, new FileOutputStream(tmpFile));

                    // TODO Bulk loading not yet implemented...

                    String graphName = DataGeneratorComponentBase.GRAPH_IRI;
                    logger.info("Clearing and loading graph: " + graphName);
                    try {
                    	conn.delete(graphName);
                    } catch(Exception e) {
                    	logger.warn("Delete graph raised exception", e);
                    }
                    conn.load(graphName, tmpFile.getAbsolutePath());
                    tmpFile.delete();
                } catch(Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                
                logger.info("Bulk loading complete");
//                try {
//                    Thread.sleep(5000);
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
                
                
                // Wait for a response of the store that the loading is actually complete                    
                //loadDataFinishedFuture.complete(null);
            }

//            try {
//                commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATION_FINISHED}));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
    }


	@Override
	public void onCommand(ByteBuffer buffer) throws Exception {
		dataHandler.onCommand(buffer);
	}


	@Override
	public void onData(ByteBuffer buffer) throws Exception {
		dataHandler.onData(buffer);
	}


}