package org.hobbit.benchmark.faceted_browsing.components;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.core.component.TaskGeneratorModule;
import org.hobbit.core.utils.ServiceManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

public class TaskGeneratorModuleFacetedBrowsing
	implements TaskGeneratorModule
{
	
	private static final Logger logger = LoggerFactory.getLogger(TaskGeneratorModuleFacetedBrowsing.class);

	
    @javax.annotation.Resource(name="taskGeneratorSparqlService")
    protected SparqlBasedService sparqlService;

    
    protected transient ServiceManager serviceManager;
    
    
	@Override
	public void startUp() throws Exception {
        Set<Service> services = Sets.newIdentityHashSet();
        services.addAll(Arrays.asList(sparqlService));

        ServiceManager serviceManager = new ServiceManager(services);
        
        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(serviceManager,
                60, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);
	}


	@Override
	public void shutDown() throws Exception {
        logger.debug("Stopping preparation sparql service");
    	serviceManager.stopAsync();
        ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);
        logger.debug("Stopped preparation sparql service");    	

	
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

        Stream<Resource> result = tasks.map(task -> {
        	Resource r = annotateTaskWithReferenceResult(task, conn, refConn);
        	return r;
        });
        
        return result;
    }
    
    
	public static Resource annotateTaskWithReferenceResult(Resource task, RDFConnection conn, RDFConnection refConn) {

        System.out.println("Generated task: " + task);
        
        String queryStr = task.getProperty(RDFS.label).getString();
        
        // The task generation is not complete without the reference result
        // TODO Reference result should be computed against TDB
        try(QueryExecution qe = refConn.query(queryStr)) {
        	ResultSet resultSet = qe.execSelect();
        	ResultSetMem rsMem = new ResultSetMem(resultSet);
        	int numRows = ResultSetFormatter.consume(rsMem);
        	rsMem.rewind();
            logger.debug("Number of result set rows for task " + task + ": " + numRows + " query: " + queryStr);

        	
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
            logger.debug("Data stream from data generator received");

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


}
