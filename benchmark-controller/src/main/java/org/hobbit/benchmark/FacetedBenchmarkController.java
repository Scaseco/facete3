package org.hobbit.benchmark;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import org.apache.jena.atlas.web.auth.PreemptiveBasicAuthenticator;
import org.apache.jena.atlas.web.auth.ScopedAuthenticator;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.UpdateProcessRemoteBase;
import org.apache.jena.sparql.modify.UpdateProcessRemoteForm;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * Created by gkatsimpras on 26/1/2017.
 */
public class FacetedBenchmarkController extends AbstractBenchmarkController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacetedBenchmarkController.class);

    public static final byte DATA_INSERTION_FINISHED = Byte.valueOf("100");
    public String containerName;

    private String SEED_PARAMETER = "SEED_PARAMETER";
    private int seedValue = 1234;

    @Override
    public void init() throws Exception {
        super.init();
        // Loading parameters...
        NodeIterator iterator ;
        iterator = benchmarkParamModel
                .listObjectsOfProperty(benchmarkParamModel.getProperty("http://w3id.org/bench#FacetedSeed"));
        if (iterator.hasNext()) {
            try {
                seedValue = iterator.next().asLiteral().getInt();
            } catch (Exception e) {
                LOGGER.error("Exception while parsing parameter.", e);
            }
        }
        if (seedValue < 0) {
            LOGGER.error(
                    "Couldn't get the seed for the mimicking algorithm seed from the parameter model. Using the default value.");
            seedValue = 1234;
        }
        // Create virtuoso instance which will hold gold standard dataset

        LOGGER.info("Starting creating Virtuoso");
        String goldVirtuoso = "git.project-hobbit.eu:4567/henning.petzka/facetedgoldvirtuoso/image";
        String[] envVariables = new String[]{"DBA_PASSWORD=dba","SPARQL_UPDATE=true", "DEFAULT_GRAPH=http://www.virtuoso-graph.com"};
        containerName = this.createContainer(goldVirtuoso, envVariables);
        LOGGER.info("Created virtuoso container ("+containerName+") for computation of gold standard.");

        TimeUnit.SECONDS.sleep(15);
        ResultSet currentSizeQuery = executeSparqlQuery("select (count(?x) as ?c) where {?x ?o ?p}");
        String resultString = currentSizeQuery.nextSolution().toString();
        LOGGER.info(resultString);
        // Create the other components
        // Create data generators
        LOGGER.info("Starting creating data generator");
        String dataGeneratorImageName = "git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator/image";
        int numberOfDataGenerators = 1;
        envVariables = new String[]{"NODE_MEM=1000",};
        createDataGenerators(dataGeneratorImageName, numberOfDataGenerators, envVariables);
        // give some time for virtuoso instance to initialize and load dataset

        // Added dataset when building virtuoso docker image so below lines are commented out
        //LOGGER.info("Start inserting data to virtuoso");
        //insertTrainingDataToVirtuoso();
        //LOGGER.info("Done inserting Data to virtuoso!");
        /////////////////////////////////////////////

        // Create task generators
        String taskGeneratorImageName = "git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator/image";
        int numberOfTaskGenerators = 1;
        envVariables = new String[]{"TASK_GENERATOR_TRUE=1000","VIRTUOSO_GOLD_SERVICE_URL=http://"+containerName+":8890/sparql",
                                    SEED_PARAMETER+"="+Integer.toString(seedValue)};

        LOGGER.info("CreatingTaskGenerator ...");
        createTaskGenerators(taskGeneratorImageName, numberOfTaskGenerators, envVariables);


        LOGGER.info("Creating Evaluation Storage ...");
        String EVALUATION_STORAGE_IMG = "git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0";
        envVariables = new String[]{"ACKNOWLEDGEMENT_FLAG=true"};
        createEvaluationStorage(EVALUATION_STORAGE_IMG, envVariables);

        waitForComponentsToInitialize();


        LOGGER.info("BENCHMARK CONTROLLER INITIALIZED!");

    }

    private ResultSet executeSparqlQuery(String query){
        QueryExecution queryEx = QueryExecutionFactory.sparqlService("http://"+containerName+":8890/sparql",
                query, "http://www.virtuoso-graph.com");
        ResultSet result = queryEx.execSelect();
        return result;
    }

    @Override
    protected void executeBenchmark() throws Exception {
        // give the start signals
        sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);
        sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
        // wait for the data generators to finish their work
        LOGGER.info("WAITING FOR DATA GENERATOR ...");
        waitForDataGenToFinish();
        // wait for the task generators to finish their work
        // wait for the system to terminate
        waitForTaskGenToFinish();
        LOGGER.info("WAITING FOR SYSTEM ...");
        waitForSystemToFinish();
        // Create the evaluation module
        String evalModuleImageName = "git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule/image";
        String[] envVariables = new String[]{"NO_VAR=true"};
        createEvaluationModule(evalModuleImageName, envVariables);
        // wait for the evaluation to finish
        waitForEvalComponentsToFinish();
        // the evaluation module should have sent an RDF model containing the
        // results. We should add the configuration of the benchmark to this
        // model.
        // this.resultModel.add(...);
        // Send the resultModul to the platform controller and terminate
        sendResultModel(resultModel);
    }



}

