package org.hobbit.rdf.component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.Resource;

import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.core.Commands;
import org.hobbit.core.component.ComponentBase;
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
import org.springframework.util.FileCopyUtils;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.gson.Gson;

import io.reactivex.Flowable;

/**
 * TODO Rename to something like TaskExecutorSparql
 *
 * SPARQL based SystemAdapter implementation for Jena's RDFConnection capable systems
 *
 * @author raven Sep 19, 2017
 *
 */
public class SystemAdapterRDFConnection
    extends ComponentBase
    implements RunnableServiceCapable
{
    private static final Logger logger = LoggerFactory.getLogger(SystemAdapterRDFConnection.class);

    @Autowired
    protected Gson gson;
        
    @Resource(name="systemUnderTestRdfConnection")
    protected RDFConnection rdfConnection;

    @Resource(name="dg2saReceiver")
    protected Flowable<ByteBuffer> fromDataGenerator;

    @Resource(name="tg2saReceiver")
    protected Flowable<ByteBuffer> fromTaskGenerator;

    @Resource(name="sa2esSender")
    protected Subscriber<ByteBuffer> sa2es;

    @Resource(name="commandSender")
    protected Subscriber<ByteBuffer> commandSender;


    protected StreamManager streamManager;

    protected ServiceManager serviceManager = null;

    //protected RDFConnection rdfConnection;

    
    @Resource(name="taskResourceDeserializer")
    protected Function<ByteBuffer, org.apache.jena.rdf.model.Resource> taskResourceDeserializer;
    
//    protected Service systemUnderTestService;

    protected CompletableFuture<?> taskGenerationFinishedFuture;
    
    @Override
    public void startUp() throws Exception {
    	logger.info("SystemAdapter::startUp() started");    
    	
    	super.startUp();

        taskGenerationFinishedFuture = PublisherUtils.triggerOnMessage(commandReceiver,
                ByteChannelUtils.firstByteEquals(Commands.TASK_GENERATION_FINISHED));

        
        streamManager = new InputStreamManagerImpl(commandSender::onNext);
        // The system adapter will send a ready signal, hence register on it on the command queue before starting the service
        // NOTE A completable future will resolve only once; Java 9 flows would allow multiple resolution (reactive streams)
//        systemUnderTestReadyFuture = PublisherUtils.awaitMessage(commandPublisher,
//                firstByteEquals(Commands.SYSTEM_READY_SIGNAL));

        //systemUnderTestService = systemUnderTestServiceFactory.get();
        //streamManager = new InputStreamManagerImpl(c);
        fromDataGenerator.subscribe(streamManager::handleIncomingData);

        streamManager.subscribe(inputStream -> {
            logger.info("Bulk load data received");

            File file = null;
            try(InputStream in = inputStream) {
                // Write incoming data to a file
                file = File.createTempFile("hobbit-system-adapter-data-to-load", ".ttl");
                FileCopyUtils.copy(in, new FileOutputStream(file));
                
                
                // Load data
                String graphName = "http://www.virtuoso-graph.com";
                logger.debug("Clearing and loading graph: " + graphName);
                rdfConnection.delete(graphName);
                rdfConnection.load(graphName, file.getAbsolutePath());
                file.delete();
                logger.info("Data loading complete");
                
            } catch (Exception e) {
            	String filename = file == null ? "(file creation failed)" : file.getAbsolutePath();
                throw new RuntimeException("While preparing dataset tmp file " + filename, e);
            }
        });

        //rdfConnection = rdfConnectionSupplier.get();

        List<Service> services = Arrays.asList(
//                systemUnderTestService
        );
        
        if(!services.isEmpty()) {
	        serviceManager = new ServiceManager(services);
	
	        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(
	                serviceManager,
	                60, TimeUnit.SECONDS,
	                60, TimeUnit.SECONDS);
        }

        fromDataGenerator.subscribe(byteBuffer -> {
            // non-stream messages from the data generator are ignored
            // System.out.println("Got a message form the data generator");
        });

        fromTaskGenerator.subscribe(byteBuffer -> {
            logger.debug("SystemAdapter received a message form the TaskGenerator");

            //rdfConnection = //RDFConnectionFactory.connect(DatasetFactory.create());

            org.apache.jena.rdf.model.Resource r = taskResourceDeserializer.apply(byteBuffer);
            
            
            String taskIdStr = r.getURI();
            String sparqlStmtStr = r.getProperty(RDFS.label).getString();

            //logger.debug("R"
            //RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_PRETTY);
            logger.debug("TaskId - Sparql stmt: " + taskIdStr + " - " + sparqlStmtStr);

            Function<String, SparqlStmt> parser = SparqlStmtParserImpl.create(Syntax.syntaxSPARQL_11, true);

            SparqlStmt stmt = parser.apply(sparqlStmtStr);

            Stopwatch stopwatch = Stopwatch.createStarted();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if(stmt.isQuery()) {
                try(QueryExecution qe = rdfConnection.query(stmt.getOriginalString())) {
                    ResultSet rs = qe.execSelect();
                    ResultSetMem rsMem = new ResultSetMem(rs);
                    int numRows = ResultSetFormatter.consume(rsMem);
                    rsMem.rewind();
                    logger.debug("Number of result set rows for task " + taskIdStr + ": " + numRows + " query: " + stmt.getOriginalString());

                    ResultSetFormatter.outputAsJSON(out, rsMem);
                } catch(Exception e) {
                    logger.warn("Sparql select query failed", e);

                    try {
                        out.write("{\"head\":{\"vars\":[\"xxx\"]},\"results\":{\"bindings\":[{\"xxx\":{\"type\":\"literal\",\"value\":\"XXX\"}}]}}".getBytes(StandardCharsets.UTF_8));
                    } catch (IOException f) {}
                }
            } else if(stmt.isUpdateRequest()) {
                try {
                    rdfConnection.update(stmt.getOriginalString());
                } catch(Exception e) {
                    try {
                        out.write("{\"head\":{\"vars\":[\"xxx\"]},\"results\":{\"bindings\":[{\"xxx\":{\"type\":\"literal\",\"value\":\"XXX\"}}]}}".getBytes(StandardCharsets.UTF_8));
                    } catch (IOException f) {}
                    logger.warn("Sparql update query failed", e);
                }
            }

            long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);

            //sendResultToEvalStorage(taskId, outputStream.toByteArray());

            //String taskIdStr = "task-id-foobar";
            byte[] data = out.toByteArray();


            byte[] taskIdBytes = taskIdStr.getBytes(StandardCharsets.UTF_8);
            // + 4 for taskIdBytes.length
            // + 4 for data.length
            int capacity = 4 + 4 + taskIdBytes.length + data.length;
            ByteBuffer buffer = ByteBuffer.allocate(capacity);
            buffer.putInt(taskIdBytes.length);
            buffer.put(taskIdBytes);
            buffer.putInt(data.length);
            buffer.put(data);

//            byte[] taskIdBytes = taskIdStr.getBytes(StandardCharsets.UTF_8);
//            int capacity = 8 + taskIdBytes.length + data.length;
//            ByteBuffer buffer = ByteBuffer.allocate(capacity);
//            buffer.putInt(taskIdBytes.length);
//            buffer.put(taskIdBytes);
//            buffer.putInt(data.length);
//            buffer.put(data);

            buffer.rewind();

//            try {
                logger.debug("Forwarding task result to evaluation storage");
                sa2es.onNext(buffer);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        });

        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.SYSTEM_READY_SIGNAL}));

    	logger.info("SystemAdpater::startUp() completed");    
    }

    @Override
    public void shutDown() throws Exception {
    	try {
	    	logger.info("SystemAdapter::shutDown() [begin]");    
	    	if(streamManager != null) {
	    		streamManager.close();
	    	}

	    	if(serviceManager != null) {
	    		ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);
	    	}
    	} finally {
    		super.shutDown();
    	}
    	logger.info("SystemAdapter::shutDown() [end]");    
    }

    @Override
    public void run() throws Exception {
        logger.info("Waiting for task generation to finish");
        taskGenerationFinishedFuture.get(10, TimeUnit.MINUTES);
//        taskGenerationFinishedFuture.get(60, TimeUnit.SECONDS);

        logger.info("Task generation finished");
    }

}
