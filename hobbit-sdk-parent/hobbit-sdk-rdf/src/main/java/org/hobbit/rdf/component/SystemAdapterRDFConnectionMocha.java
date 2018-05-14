package org.hobbit.rdf.component;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.core.Commands;
import org.hobbit.core.component.ComponentBaseExecutionThread;
import org.hobbit.core.component.DataGeneratorFacetedBrowsing;
import org.hobbit.core.component.DataProtocol;
import org.hobbit.core.component.MochaConstants;
import org.hobbit.core.utils.ByteChannelUtils;
import org.hobbit.core.utils.PublisherUtils;
import org.hobbit.core.utils.ServiceManagerUtils;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.gson.Gson;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import jersey.repackaged.com.google.common.collect.Iterators;

/**
 * TODO Rename to something like TaskExecutorSparql
 *
 * SPARQL based SystemAdapter implementation for Jena's RDFConnection capable systems
 *
 * @author raven Sep 19, 2017
 *
 */
@Component
@Qualifier("MainService")
public class SystemAdapterRDFConnectionMocha
    extends ComponentBaseExecutionThread
{
    private static final Logger logger = LoggerFactory.getLogger(SystemAdapterRDFConnectionMocha.class);

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


    //protected StreamManager streamManager;

    protected ServiceManager serviceManager = null;

    //protected RDFConnection rdfConnection;

    
    @Resource(name="taskResourceDeserializer")
    protected Function<ByteBuffer, org.apache.jena.rdf.model.Resource> taskResourceDeserializer;
    
//    protected Service systemUnderTestService;

    protected CompletableFuture<?> taskGenerationFinishedFuture;
    
    
    protected Disposable unsubscribe;

    // Protocol fields
	//private CompletableFuture<?> allDataReceivedMutex = new CompletableFuture<>();    
    	
	protected DataProtocol rdfBulkLoadProtocol;
    
	@Resource(name="actualResultEncoder")
	protected BiFunction<String, ResultSet, Stream<ByteBuffer>> actualResultEncoder;
    
	
    @Override
    public void startUp() {
    	logger.info("SystemAdapter::startUp() started");    
    	
    	super.startUp();

        taskGenerationFinishedFuture = PublisherUtils.triggerOnMessage(commandReceiver,
                ByteChannelUtils.firstByteEquals(Commands.TASK_GENERATION_FINISHED));

        
        //streamManager = new InputStreamManagerImpl(commandSender::onNext);
        // The system adapter will send a ready signal, hence register on it on the command queue before starting the service
        // NOTE A completable future will resolve only once; Java 9 flows would allow multiple resolution (reactive streams)
//        systemUnderTestReadyFuture = PublisherUtils.awaitMessage(commandPublisher,
//                firstByteEquals(Commands.SYSTEM_READY_SIGNAL));

        //systemUnderTestService = systemUnderTestServiceFactory.get();
        //streamManager = new InputStreamManagerImpl(c);
        //fromDataGenerator.subscribe(streamManager::handleIncomingData);

//        streamManager.subscribe(inputStream -> {
//            logger.info("Bulk load data received");
//
//            File file = null;
//            try(InputStream in = inputStream) {
//                // Write incoming data to a file
//                file = File.createTempFile("hobbit-system-adapter-data-to-load", ".ttl");
//                FileCopyUtils.copy(in, new FileOutputStream(file));
//                
//                
//                // Load data
//                logger.debug("Clearing and loading graph: " + graphName);
//                rdfConnection.delete(graphName);
//                rdfConnection.load(graphName, file.getAbsolutePath());
//                file.delete();
//                logger.info("Data loading complete");
//                
//            } catch (Exception e) {
//            	String filename = file == null ? "(file creation failed)" : file.getAbsolutePath();
//                throw new RuntimeException("While preparing dataset tmp file " + filename, e);
//            }
//        });

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

        rdfBulkLoadProtocol = new RdfBulkLoadProtocolMocha(rdfConnection,
        		() -> commandSender.onNext(ByteBuffer.allocate(1).put(0, MochaConstants.BULK_LOADING_DATA_FINISHED)),
        		() -> {});
        
        unsubscribe = new CompositeDisposable(
        		fromDataGenerator.subscribe(rdfBulkLoadProtocol::onData),
        		fromTaskGenerator.subscribe(this::onTask),
        		commandReceiver.subscribe(rdfBulkLoadProtocol::onCommand));

        
//		allDataReceivedMutex.whenComplete((v, t) -> {
//			dataLoadingFinished = true;
//		});

        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.SYSTEM_READY_SIGNAL}));

    	logger.info("SystemAdpater::startUp() completed");    
    }

    
    @Override
    public void triggerShutdown() {
    	taskGenerationFinishedFuture.cancel(true);
    	try {
	    	logger.info("SystemAdapter::shutDown() [begin]");    
	    	unsubscribe.dispose();
	    	
//	    	if(streamManager != null) {
//	    		streamManager.close();
//	    	}

	    	if(serviceManager != null) {
	    		ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);
	    	}
    	} finally {
    		super.triggerShutdown();
    	}
    	logger.info("SystemAdapter::shutDown() [end]");    
    }

    @Override
    public void run() throws Exception {
        logger.info("SA: waiting for task generation to finish");
        //taskGenerationFinishedFuture.get(10, TimeUnit.MINUTES);
//        taskGenerationFinishedFuture.get(60, TimeUnit.SECONDS);
        taskGenerationFinishedFuture.get(60 * 30, TimeUnit.SECONDS);

        logger.info("SA: Task generation finished");
    }
    
    public void sendResultSet(String taskIdStr, ResultSet rs) {
        logger.debug("Forwarding task result to evaluation storage");
        Stream<ByteBuffer> actualResultMsgs = actualResultEncoder.apply(taskIdStr, rs);
        Iterator<ByteBuffer> it = actualResultMsgs.iterator();
        while(it.hasNext()) {
        	ByteBuffer buffer = it.next();
            sa2es.onNext(buffer);
        }	
    }
    
    public static ResultSet createErrorResultSet() {
    	Var xxx = Var.alloc("xxx");
    	Binding binding = BindingFactory.binding(xxx, NodeFactory.createLiteral("XXX"));
    	ResultSet result = ResultSetUtils.create2(Collections.singleton(xxx), Iterators.singletonIterator(binding));
    	return result;
    }

    
    public void onTask(ByteBuffer buf) {
    	ByteBuffer byteBuffer = buf.duplicate();
        logger.debug("SystemAdapter received a message form the TaskGenerator");

        //rdfConnection = //RDFConnectionFactory.connect(DatasetFactory.create());

        org.apache.jena.rdf.model.Resource r = taskResourceDeserializer.apply(byteBuffer);
        
        
        String taskIdStr = r.getURI();
        String sparqlStmtStr = r.getProperty(RDFS.label).getString();

        //logger.debug("R"
        //RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_PRETTY);
        logger.debug("TaskId - Sparql stmt: " + taskIdStr + " - " + sparqlStmtStr);

        Function<String, SparqlStmt> parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);

        SparqlStmt stmt = parser.apply(sparqlStmtStr);

//        try(QueryExecution xxx = rdfConnection.query("SELECT ?g (COUNT(*) AS ?c) { GRAPH ?g { ?s ?p ?o } } GROUP BY ?g ORDER BY DESC(COUNT(*))")) {
//       try(QueryExecution xxx = rdfConnection.query("SELECT ?g ?s ?p ?o { GRAPH ?g { ?s ?p ?o } } ORDER BY ?g ?s ?p ?o LIMIT 10")) {
//        	System.out.println(ResultSetFormatter.asText(xxx.execSelect()));
//        }
        
        if(stmt.isQuery()) {
        	//System.out.println("isInTransaction: " + rdfConnection.isInTransaction());
        	//xrdfConnection.begin(ReadWrite.READ);
        	String queryStr = stmt.getOriginalString();
            try(QueryExecution qe = rdfConnection.query(queryStr)) {
            	ResultSet rs = qe.execSelect();
            	//System.out.println(ResultSetFormatter.asText(rs));
            	sendResultSet(taskIdStr, rs);
//                ResultSetMem rsMem = new ResultSetMem(rs);
//                int numRows = ResultSetFormatter.consume(rsMem);
//                rsMem.rewind();
//                logger.debug("Number of result set rows for task " + taskIdStr + ": " + numRows + " query: " + stmt.getOriginalString());
//
//                ResultSetFormatter.outputAsJSON(out, rsMem);
            	//xrdfConnection.commit();
            } catch(Exception e) {
                logger.warn("Sparql select query failed", e);
            	sendResultSet(taskIdStr, createErrorResultSet());
            } finally {
        		//xrdfConnection.end();
            }
        } else if(stmt.isUpdateRequest()) {
        	//xrdfConnection.begin(ReadWrite.READ);
        	try {
                //xrdfConnection.update(stmt.getOriginalString());
            	//xrdfConnection.commit();
            	sendResultSet(taskIdStr, new ResultSetMem());
            } catch(Exception e) {
                logger.warn("Sparql update query failed", e);
            	sendResultSet(taskIdStr, createErrorResultSet());
            } finally {
        		//xrdfConnection.end();
            }
        }
        
//        
//        long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
//
//        //sendResultToEvalStorage(taskId, outputStream.toByteArray());
//
//        //String taskIdStr = "task-id-foobar";
//        byte[] data = out.toByteArray();
//
//
//        byte[] taskIdBytes = taskIdStr.getBytes(StandardCharsets.UTF_8);
//        // + 4 for taskIdBytes.length
//        // + 4 for data.length
//        int capacity = 4 + 4 + taskIdBytes.length + data.length;
//        ByteBuffer buffer = ByteBuffer.allocate(capacity);
//        buffer.putInt(taskIdBytes.length);
//        buffer.put(taskIdBytes);
//        buffer.putInt(data.length);
//        buffer.put(data);
//
////        byte[] taskIdBytes = taskIdStr.getBytes(StandardCharsets.UTF_8);
////        int capacity = 8 + taskIdBytes.length + data.length;
////        ByteBuffer buffer = ByteBuffer.allocate(capacity);
////        buffer.putInt(taskIdBytes.length);
////        buffer.put(taskIdBytes);
////        buffer.putInt(data.length);
////        buffer.put(data);
//
//        buffer.rewind();

//        try {
        
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
    
 
    
}
