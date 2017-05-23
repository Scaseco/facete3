package org.hobbit.system;

import org.apache.commons.io.Charsets;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.UpdateProcessRemote;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;


import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by gkatsimpras on 26/1/2017.
 */

public class FacetedSystemAdapter extends AbstractSystemAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacetedSystemAdapter.class);
    private byte[] receivedData = null;
    private String VIRTUOSO_TEST_SERVICE_URL;//= "http://localhost:8890/sparql";
    private String VIRTUOSO_GRAPH_IRI = "http://testdata.org";
    private String virtusoContainerName;
    private Semaphore terminateMutex = new Semaphore(0);
    private int chunkCounter = 0;
    // private Semaphore allDataSemaphore = new Semaphore(1);

    protected int dataSize = 0;
    @Override
    public void init() throws Exception {
        super.init();

        String goldVirtuoso = "git.project-hobbit.eu:4567/henning.petzka/facetedgoldvirtuoso/image";
        String[] envVariables = new String[]{"DBA_PASSWORD=dba","SPARQL_UPDATE=true","DEFAULT_GRAPH=" + VIRTUOSO_GRAPH_IRI};
        String containerName = this.createContainer(goldVirtuoso, envVariables);
        VIRTUOSO_TEST_SERVICE_URL = "http://"+containerName+":8890/sparql";
        TimeUnit.SECONDS.sleep(59);

    }

    @Override
    public void receiveGeneratedData(byte[] data) {

        if (data != null) {
            // convert received data to a model and insert it to virtuoso

            // NEW VERSION

            // NEW VERSION FOLLOWING API
            ByteBuffer buffer = ByteBuffer.wrap(data);
            String graphURI = RabbitMQUtils.readString(buffer);
            byte[] triples = new byte[buffer.remaining()];
            buffer.get(triples);

            String dataString = RabbitMQUtils.readString(triples);

            StringReader reader = new StringReader(dataString);

            Model receivedModel = ModelFactory.createDefaultModel();

            RDFDataMgr.read(receivedModel, reader, "", Lang.N3);

            // OLD VERSION
            // Model receivedModel = RabbitMQUtils.readModel(data);


            //LOGGER.info("The model to use as Test data: "+receivedModel);

        }


        //"SELECT (COUNT(DISTINCT ?x) as ?count) where {?x ?p ?o .}"
        ResultSet currentSizeQuery = executeSparqlQuery("select (count(?x) as ?c) where {?x ?o ?p}");
        String resultString = currentSizeQuery.nextSolution().toString();
        LOGGER.info(resultString);

        /* if (chunkCounter == 20) {
            try {
                LOGGER.info("Finished inserting test data!! Sending signal to TaskGen");
                TimeUnit.SECONDS.sleep(59);
                sendToCmdQueue((byte) 150);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        chunkCounter++;
    */

    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] queryTask) {
        // read query
        LOGGER.info("Task id: "+taskId);
        LOGGER.info("Task bytes length: "+queryTask.length);
        ByteBuffer buffer = ByteBuffer.wrap(queryTask);
        String sparqlQuery = RabbitMQUtils.readString(buffer);
        LOGGER.info("Task query: "+sparqlQuery);
        //execute sparql query
        ResultSet resultData = executeSparqlQuery(sparqlQuery);
        // concatenate result with comma and convert to bytearray


        // change to sending around JSON.
        //
        // OLD:
        // byte[] resultByteArray = formatResultData(resultData);

        //NEW:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, resultData);
        byte[] resultByteArray = outputStream.toByteArray();

        //Model resultedModel = RDFOutput.encodeAsModel(resultData);
        LOGGER.info("Task id: "+taskId);
        //send results to evaluation storage
        try {
            sendResultToEvalStorage(taskId, resultByteArray);
            LOGGER.info("Task sent to eval storage");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] formatResultData(ResultSet result){
        StringBuilder listString = new StringBuilder();
        while(result.hasNext()) {
            String value = (result.next().get(result.getResultVars().get(0)).toString());
            listString.append(value+",");
        }
        byte[] resultsByteArray = listString.toString().getBytes(Charsets.UTF_8);
        return resultsByteArray;
    }

    private ResultSet executeSparqlQuery(String query){
        QueryExecution queryEx = QueryExecutionFactory.sparqlService(VIRTUOSO_TEST_SERVICE_URL,
                query, VIRTUOSO_GRAPH_IRI);
        ResultSet result = queryEx.execSelect();
        return result;
    }



    @Override
    public void receiveCommand(byte command, byte[] data) {
        // If this is the signal to start the data generation
        if (command == (byte) 151) {
            try {
                sendToCmdQueue((byte) 150);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
    }


}

