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
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.UpdateProcessRemote;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by gkatsimpras on 26/1/2017.
 */

public class FacetedSystemAdapter extends AbstractSystemAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacetedSystemAdapter.class);
    private byte[] receivedData = null;
    private String VIRTUOSO_TEST_SERVICE_URL;//= "http://localhost:8890/sparql";
    private String VIRTUOSO_GRAPH_IRI = "http://testdata.org";
    private String virtusoContainerName;
    @Override
    public void init() throws Exception {
        super.init();
        LOGGER.info("Starting creating Virtuoso for Test Data...");
        String[] envVariables = new String[]{"DBA_PASSWORD=dba", "SPARQL_UPDATE=true", "DEFAULT_GRAPH=" + VIRTUOSO_GRAPH_IRI};
        virtusoContainerName = this.createContainer("tenforce/virtuoso", envVariables);
        LOGGER.info("Created virtuoso container (" + virtusoContainerName + ") for computation of test data.");
        VIRTUOSO_TEST_SERVICE_URL = "http://"+virtusoContainerName+":8890/sparql";
    }

    @Override
    public void receiveGeneratedData(byte[] data) {
        //retrieve generated data
        // 1st case: do we have to insert the data to virtuoso??!!
        // 2nd case: create a jena model by reading inputstream data
        //InputStream dataStream = new ByteArrayInputStream(receivedData);
        //Model rdfModel = ModelFactory.createDefaultModel();
        //rdfModel.read(dataStream, null, "TTL");
        if (data!=null) {
            // convert received data to a model and insert it to virtuoso
            Model receivedModel = RabbitMQUtils.readModel(data);
            //LOGGER.info("The model to use as Test data: "+receivedModel);
            try {
                insertTestDataToVirtuoso(receivedModel);
                LOGGER.info("Inserted test data!!");
            } catch (Exception e) {
                LOGGER.error("Error inserting test data received from the data generator!");
            }
        }
    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] queryTask) {
        // read query
        LOGGER.info("Task id: "+taskId);
        LOGGER.info("Task bytes length: "+queryTask.length);
        //ByteBuffer buffer = ByteBuffer.wrap(queryTask);
        String sparqlQuery = RabbitMQUtils.readString(queryTask);
        LOGGER.info("Task query: "+sparqlQuery);
        //execute sparql query
        ResultSet resultData = executeSparqlQuery(sparqlQuery);
        // concatenate result with comma and convert to bytearray
        byte[] resultByteArray = formatResultData(resultData);
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

    protected void insertTestDataToVirtuoso(Model rdfModel) throws Exception{
        String username = "dba";
        String password = "dba";
        // authenticated client
        /*
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);

        HttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        */
        // get all statements from the model and create a quad holder
        StmtIterator it = rdfModel.listStatements();
        QuadDataAcc quads = new QuadDataAcc();
        quads.setGraph(NodeFactory.createURI(VIRTUOSO_GRAPH_IRI));

        // add triples from model into quads
        while (it.hasNext()) {
            Triple triple = it.next().asTriple();
            quads.addTriple(triple);
        }
        // insertion in chunks (virtuoso seems cannot handle batches larger than 1000)
        List<List<Quad>> chunks = org.apache.commons.collections4.ListUtils.partition(quads.getQuads(), 1000);
        //System.out.println(chunks.size());
        for (List<Quad> chunk : chunks) {
            QuadDataAcc quadChunk = new QuadDataAcc(chunk);
            String query = (new UpdateDataInsert(quadChunk)).toString(rdfModel);
            //execute insert query to virtuoso
            UpdateProcessRemote update = (UpdateProcessRemote) UpdateExecutionFactory
                    .createRemote(UpdateFactory.create(query), VIRTUOSO_TEST_SERVICE_URL + "-auth");
            update.setAuthentication(username,password.toCharArray());//;setClient(client);
            update.execute();
        }

    }

}

