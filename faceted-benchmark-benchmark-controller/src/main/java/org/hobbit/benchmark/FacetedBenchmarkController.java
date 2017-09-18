package org.hobbit.benchmark;


import java.util.concurrent.TimeUnit;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Created by gkatsimpras on 26/1/2017.
 */
public class FacetedBenchmarkController extends AbstractBenchmarkController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacetedBenchmarkController.class);

    public static final byte DATA_INSERTION_FINISHED = Byte.valueOf("100");
    public String containerName;

    private String SEED_PARAMETER = "SEED_PARAMETER";
    private int seedValue = 1234;


//    @Resource
//    protected SparqlServiceSupplier sparqlServiceSupplier;


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

        // Spawn a SPARQL endpoint for for reference data
        //SparqlService sparqlService = sparqlServiceSupplier.get();


        // Invoke the data generator and store

        // Somehow get hold of a bulk loader feature
        // (maybe jena rdf connection?)

        //RDFDatasetConnection conn = sparqlService.getFoo();



        // Request a data supplier for the reference data (required for generating tasks)

        // Load the data into the SPARQL service

        // Request the data supplier for the benchmark data






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
        this.stopContainer(containerName);


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

