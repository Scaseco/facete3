package org.hobbit.core.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.core.Commands;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.service.api.RunnableServiceCapable;
import org.hobbit.core.utils.ByteChannelUtils;
import org.hobbit.core.utils.PublisherUtils;
import org.hobbit.core.utils.ServiceManagerUtils;
import org.hobbit.transfer.InputStreamManagerImpl;
import org.hobbit.transfer.StreamManager;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.gson.Gson;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

// Some thoughts on client stubs for the referenced services

//interface EvaluationStorage {
//	sendTask(Resource resource);
//}


// The system adapter is there to interact with the system (e.g. a single triple store, or a search engine cluster)
// and thus feature functionality COMMON TO A CATEGORY OF SYSTEMS (e.g. triple store, rdf store, graph store, search engine, routing system)
//
//interface SystemAdapterClientRDFConnection {
// loadGraph(String graphURI, URI uri);
//
//}

// The task executor is there to execute tasks on
//interface TaskExecutorClient extends TaskExecutor { // The client should just look like a local object
//  execTask(Resource taskDesc);
//}

@Component
public class TaskGeneratorFacetedBenchmark
    extends ComponentBase
    implements RunnableServiceCapable
{
    private static final Logger logger = LoggerFactory.getLogger(TaskGeneratorFacetedBenchmark.class);

    @javax.annotation.Resource(name="taskGeneratorSparqlService")
    protected SparqlBasedService sparqlService;


//    @javax.annotation.Resource(name="referenceSparqlService")
//    protected SparqlBasedService referenceSparqlService;



    @javax.annotation.Resource(name="commandChannel")
    protected Subscriber<ByteBuffer> commandChannel;

//    @Resource(name="dataChannel")
//    protected WritableByteChannel dataChannel;

    @javax.annotation.Resource(name="dg2tgPub")
    protected Flowable<ByteBuffer> fromDataGenerator;

    @javax.annotation.Resource(name="tg2sa")
    protected Subscriber<ByteBuffer> toSystemAdater;


    @javax.annotation.Resource(name="tg2es")
    protected Subscriber<ByteBuffer> toEvaluationStorage;

    @javax.annotation.Resource(name="taskAckPub")
    protected Flowable<ByteBuffer> taskAckPub;

    @javax.annotation.Resource(name="taskStreamSupplier")
    protected Supplier<Stream<Resource>> taskStreamSupplier;
    
    
    
    //FacetedBrowsingEncoders.formatForEvalStorage(task, timestamp);
    
    protected BiFunction<Resource, Long, ByteBuffer> taskEncoderForEvalStorage;

//  JsonObject json = FacetedBrowsingEncoders.resourceToJson(subResource);
//  ByteBuffer buf2 = ByteBuffer.wrap(gson.toJson(json).getBytes(StandardCharsets.UTF_8));    
    protected Function<Resource, ByteBuffer> taskEncoderForSystemAdapter;

    
    @Autowired
    protected Gson gson;


    //@Resource(name="referenceSparqlService")
    //protected SparqlBasedSystemService referenceSparqlService;

//    @Resource
//    protected
    protected ServiceManager serviceManager;

    protected StreamManager streamManager;


    // The generated tasks; we should use file persistence for scaling in the general case
    //protected Collection<Resource> generatedTasks = new ArrayList<>();

    
    
    protected CompletableFuture<Void> startTaskGenerationFuture;
    
    
    protected CompletableFuture<Void> loadDataFinishedFuture = new CompletableFuture<>();
    protected CompletableFuture<ByteBuffer> startSignalReceivedFuture;
    
    
    protected transient Disposable fromDataGeneratorUnsubscribe = null;
    
    @Override
    public void startUp() throws Exception {
        
        CompletableFuture<ByteBuffer> startSignalReceivedFuture = PublisherUtils.triggerOnMessage(commandPublisher, ByteChannelUtils.firstByteEquals(Commands.TASK_GENERATOR_START_SIGNAL));

        startTaskGenerationFuture = CompletableFuture.allOf(startSignalReceivedFuture, loadDataFinishedFuture);
        
        // Avoid duplicate services
        Set<Service> services = Sets.newIdentityHashSet();
        services.addAll(Arrays.asList(
                sparqlService
                //referenceSparqlService
        ));

        serviceManager = new ServiceManager(services);

        streamManager = new InputStreamManagerImpl(commandChannel::onNext);

        //Consumer<ByteBuffer> fromDataGeneratorObserver
        fromDataGeneratorUnsubscribe = fromDataGenerator.subscribe(streamManager::handleIncomingData);

        /*
         * The protocol here is:
         * We expect data to arrive exactly once in the form of a stream.
         *
         * This steam contains the dataset to be loaded into the preparation sparql endpoint
         *
         * Once the stream is consumed, the task generation starts.
         * The tasks are then evaluated against an evaluation sparqlService
         * and the result are set to the eval store.
         *
         * Finally, the tasks are sent again to system adapter
         *
         * As we have served out duty then, we can stop the services
         *
         */
        streamManager.subscribe(tmpIn -> {
            try(InputStream in = tmpIn) {
                logger.debug("Data stream from data generator received");

                try(RDFConnection conn = sparqlService.createDefaultConnection()) {
                    try {
                        // Perform bulk load
                        File tmpFile = File.createTempFile("hobbit-faceted-browsing-benchmark-task-generator-bulk-load-", ".nt");
                        tmpFile.deleteOnExit();
                        FileCopyUtils.copy(in, new FileOutputStream(tmpFile));

                        // TODO Bulk loading not yet implemented...

                        String graphName = "http://www.virtuoso-graph.com";
                        logger.debug("Clearing and loading graph: " + graphName);
                        conn.delete(graphName);
                        conn.load(graphName, tmpFile.getAbsolutePath());
                        tmpFile.delete();
                    } catch(Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    
                    logger.debug("Bulk loading complete");
//                    try {
//                        Thread.sleep(5000);
//                    } catch(Exception e) {
//                        e.printStackTrace();
//                    }
                    
                    
                    // Wait for a response of the store that the loading is actually complete                    
                    loadDataFinishedFuture.complete(null);
                }

//                try {
//                    commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATION_FINISHED}));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
            } catch (IOException f) {
                f.printStackTrace();
                throw new RuntimeException(f);
            }
        });


        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(serviceManager,
                60, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);


//
//        commandPublisher.subscribe((buffer) -> {
//            // Pass all data with a defensive copy to stream handler
//            streamManager.handleIncomingData(buffer.duplicate());
//
//            if(buffer.hasRemaining()) {
//                byte cmd = buffer.get(0);
//                switch(cmd) {
//                case Commands.TASK_GENERATOR_START_SIGNAL:
//                    try {
//                        runTaskGeneration();
////                        sendOutTasks();
//                        commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATION_FINISHED}));
//                    } catch(Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                    //computeReferenceResultAndSendToEvalStorage();
//                    break;
//                case BenchmarkControllerFacetedBrowsing.START_BENCHMARK_SIGNAL:
//                    sendOutTasks();
//                    break;
//                }
//            }
//        });

        // At this point, the task generator is ready for processing
        // The message should be sent out by the service wrapper:
        commandChannel.onNext(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATOR_READY_SIGNAL}));
    }


    @Override
    public void run() throws Exception {
        // Wait for the start signal; but also make sure the data was loaded!
        
        
        logger.debug("Task generator waiting for start signal");
        startTaskGenerationFuture.get(60, TimeUnit.SECONDS);

        //logger.debug("Task generator received start signal; running task generation");
        //runTaskGeneration();
        //logger.debug("Task generator done with task generation; sending tasks out");
        sendOutTasks();
        logger.debug("Task generator fulfilled its purpose and shutting down");
    }




//    public ByteBuffer createMessageForEvalStorage(Resource task, SparqlQueryConnection conn) {
//        String queryStr = task.getProperty(RDFS.label).getString();
//
//        ByteBuffer result;
//
//        try(QueryExecution qe = conn.query(queryStr)) {
//            ResultSet resultSet = qe.execSelect();
//            long timestamp = System.currentTimeMillis();
//            result = FacetedBrowsingEncoders.formatForEvalStorage(task, resultSet, timestamp);
//        }
//
//        return result;
//    }
    
    protected void sendOutTasks() {

        // Pretend we have a stream of tasks because this is what it should eventually be        
    	try(Stream<Resource> taskStream = taskStreamSupplier.get()) {

            taskStream.forEach(task -> {
            	
                // We are now sending out the task, so track the timestamp
                long timestamp = System.currentTimeMillis();
            	
            	
                ByteBuffer buf = taskEncoderForEvalStorage.apply(task, timestamp);
                		//createMessageForEvalStorage(task, referenceConn);

//                try {
                	logger.debug("Sending to eval store");
                    toEvaluationStorage.onNext(buf);
//                } catch(IOException e) {
//                    throw new RuntimeException(e);
//                }

                
                // The SA only needs to see the URI and the label (the query string)
                Resource subResource = task.inModel(ModelFactory.createDefaultModel());
                subResource.addLiteral(RDFS.label, task.getProperty(RDFS.label).getString());
                
                ByteBuffer buf2 = taskEncoderForSystemAdapter.apply(subResource);
                
                
                //String queryStr = task.getProperty(RDFS.label).getString();
//                try {
                
                String taskId = task.getURI();

                CompletableFuture<?> taskAckFuture = PublisherUtils
						.triggerOnMessage(taskAckPub, (buffer) -> {
							String ackMsg = RabbitMQUtils.readString(buffer.array());
							boolean r = taskId.equals(ackMsg);
							return r;
						});

            	logger.debug("Sending to system under test");
                toSystemAdater.onNext(buf2);
                    
                    
                    
               // Wait for acknowledgement
               try {
            	   taskAckFuture.get(60, TimeUnit.SECONDS);
               } catch (InterruptedException | ExecutionException | TimeoutException e) {
            	   throw new RuntimeException("Timeout waiting for acknowledgement of task " + taskId);
               }
               System.out.println("Acknowledged: " + taskId);
               
//                } catch(IOException e) {
//                    throw new RuntimeException(e);
//                }
            });
        }
    }


//    @Override
//    public void receiveCommand(byte command, byte[] data) {
//        streamManager.handleIncomingData(ByteBuffer.wrap(data));
//    }

//    /**
//     * This method gets invoked by the data generator
//     */
//    @Override
//    public void generateTask(byte[] data) throws Exception {
//        streamManager.handleIncomingData(ByteBuffer.wrap(data));
//    }
//
//    @Override
//    public void sendTaskToSystemAdapter(String taskIdString, byte[] data) throws IOException {
//        // TODO Auto-generated method stub
//
//    }

    @Override
    public void shutDown() throws IOException {
        streamManager.close();
        ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);

        //fromDataGenerator.unsubscribe(streamManager::handleIncomingData);
        Optional.ofNullable(fromDataGeneratorUnsubscribe).ifPresent(Disposable::dispose);
    }
}