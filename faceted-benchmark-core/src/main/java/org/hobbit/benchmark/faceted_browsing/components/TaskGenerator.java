package org.hobbit.benchmark.faceted_browsing.components;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.core.component.FacetedTaskGeneratorOld;
import org.hobbit.core.utils.ServiceManagerUtils;

public class TaskGenerator {

    public void runTaskGeneration() throws IOException {

        // Now invoke the actual task generation
        FacetedTaskGeneratorOld gen = new FacetedTaskGeneratorOld();

        try(RDFConnection conn = sparqlService.createDefaultConnection();
            RDFConnection refConn = sparqlService.createDefaultConnection()) {

        	gen.setQueryConn(conn);
            gen.initializeParameters();
            Stream<Resource> tasks = gen.generateTasks();

            tasks.forEach(task -> {
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
                
                generatedTasks.add(task);
            });
        }
        logger.info("TaskGenerator created " + generatedTasks.size() + " tasks");

        logger.debug("Stopping preparation sparql service");
        ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);
        logger.debug("Stopped preparation sparql service");
    }
}
