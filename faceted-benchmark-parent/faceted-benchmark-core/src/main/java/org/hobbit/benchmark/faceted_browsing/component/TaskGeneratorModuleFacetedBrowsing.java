package org.hobbit.benchmark.faceted_browsing.component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.core.component.DataProtocol;
import org.hobbit.core.component.TaskGeneratorModule;
import org.hobbit.core.utils.ServiceManagerUtils;
import org.hobbit.rdf.component.RdfBulkLoadProtocolMocha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

/**
 * 
 * 
 * Note: The life cycle of this worker is managed by the TaskGeneratorComponent
 * 
 * @author raven Nov 21, 2017
 *
 */
public class TaskGeneratorModuleFacetedBrowsing
	implements TaskGeneratorModule
{
	
	private static final Logger logger = LoggerFactory.getLogger(TaskGeneratorModuleFacetedBrowsing.class);

	
    @javax.annotation.Resource(name="taskGeneratorSparqlService")
    protected SparqlBasedService sparqlService;

    protected DataProtocol dataHandler;
    
    
    protected transient ServiceManager serviceManager;
    
    protected CompletableFuture<Void> dataLoadingComplete = new CompletableFuture<>();
    
    
    
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
        dataHandler = new RdfBulkLoadProtocolMocha(conn, () -> {}, () -> {
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
	public Stream<Resource> generateTasks() {
		Stream<Resource> result = runTaskGeneration().stream();
		return result;
	}
	
	
	
	
    public List<Resource> runTaskGeneration() {

        List<Resource> tasks;

        try(RDFConnection conn = sparqlService.createDefaultConnection();
    		  RDFConnection refConn = sparqlService.createDefaultConnection()) {
    	
            try {
				tasks = runTaskGenerationCore(conn, refConn).collect(Collectors.toList());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    	
            logger.info("TaskGenerator created " + tasks.size() + " tasks");

        }
        return tasks;
    }

    
    
    public static Stream<Resource> runTaskGenerationCore(RDFConnection conn, RDFConnection refConn) throws IOException {

        // Now invoke the actual task generation
        FacetedTaskGeneratorOld gen = new FacetedTaskGeneratorOld();

//        try(RDFConnection conn = sparqlService.createDefaultConnection();
//            RDFConnection refConn = sparqlService.createDefaultConnection()) {

    	gen.setQueryConn(conn);
        gen.initializeParameters();
        Stream<Resource> tasks = gen.generateTasks();

        SparqlStmtParser parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, false);

        Stream<Resource> result = tasks.map(task -> {
        	
        	Statement stmt = task.getProperty(RDFS.label);
        	String str = stmt.getString();
        	Query query = parser.apply(str).getAsQueryStmt().getQuery();
        	query.addGraphURI("http://example.org/graph");
        	String newQueryStr = Objects.toString(query);
        	stmt.changeObject(newQueryStr);
        	
        	
        	Resource r = annotateTaskWithReferenceResult(task, conn, refConn);
        	return r;
        });
        
        return result;
    }
    
    
	public static Resource annotateTaskWithReferenceResult(Resource task, RDFConnection conn, RDFConnection refConn) {

        logger.info("Generated task: " + task);
        
        String queryStr = task.getProperty(RDFS.label).getString();
        
        // The task generation is not complete without the reference result
        // TODO Reference result should be computed against TDB
        try(QueryExecution qe = refConn.query(queryStr)) {
        	ResultSet resultSet = qe.execSelect();
        	ResultSetMem rsMem = new ResultSetMem(resultSet);
        	int numRows = ResultSetFormatter.consume(rsMem);
        	rsMem.rewind();
            logger.info("Number of result set rows for task " + task + ": " + numRows + " query: " + queryStr);

        	
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	ResultSetFormatter.outputAsJSON(baos, rsMem); //resultSet);
        	//baos.flush();
        	String resultSetStr = baos.toString();
        	task.addLiteral(RDFS.comment, resultSetStr);
        }
            	//result = FacetedBrowsingEncoders.formatForEvalStorage(task, resultSet, timestamp);
        

        return task;
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

                    String graphName = "http://www.virtuoso-graph.com";
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
