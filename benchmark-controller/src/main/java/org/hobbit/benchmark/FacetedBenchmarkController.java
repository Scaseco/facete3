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
import org.apache.jena.rdf.model.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * Created by gkatsimpras on 26/1/2017.
 */
public class FacetedBenchmarkController extends AbstractBenchmarkController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacetedBenchmarkController.class);

    public String containerName;

    @Override
    public void init() throws Exception {
        super.init();
        // Your initialization code comes here...
        // You might want to load parameters from the benchmarks parameter model
        //NodeIterator iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
        //        .getProperty("http://example.org/myParameter"));
        // Create virtuoso instance which will hold gold standard dataset

        LOGGER.info("Starting creating Virtuoso");
        String[] envVariables = new String[]{"DBA_PASSWORD=dba","SPARQL_UPDATE=true", "DEFAULT_GRAPH=http://www.virtuoso-graph.com"};
        containerName = this.createContainer("tenforce/virtuoso", envVariables);
        LOGGER.info("Created virtuoso container ("+containerName+") for computation of gold standard.");


        // Create the other components
        // Create data generators
        LOGGER.info("Starting creating data generator");
        String dataGeneratorImageName = "git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator";
        int numberOfDataGenerators = 1;
        envVariables = new String[]{"NODE_MEM=1000",};
        createDataGenerators(dataGeneratorImageName, numberOfDataGenerators, envVariables);

        // Create evaluation storage
        //createEvaluationStorage();
        TimeUnit.SECONDS.sleep(10);
        // Wait for all components to finish their initialization
        LOGGER.info("Start inserting data to virtuoso");
        insertTrainingDataToVirtuoso();
        LOGGER.info("Done inserting Data to virtuoso!");
        // Create task generators
        String taskGeneratorImageName = "git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator";
        int numberOfTaskGenerators = 1;
        envVariables = new String[]{"TASK_GENERATOR_TRUE=1000","VIRTUOSO_GOLD_SERVICE_URL=http://"+containerName+":8890/sparql"};

        LOGGER.info("CreatingTaskGenerator ...");
        createTaskGenerators(taskGeneratorImageName, numberOfTaskGenerators, envVariables);


        LOGGER.info("Creating Evaluation Storage ...");
        String EVALUATION_STORAGE_IMG = "git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0";
        envVariables = new String[]{"ACKNOWLEDGEMENT_FLAG_KEY=true"};
        createEvaluationStorage(EVALUATION_STORAGE_IMG, envVariables);

        waitForComponentsToInitialize();


        LOGGER.info("BENCHMARK CONTROLLER INITIALIZED!");

    }

    @Override
    protected void executeBenchmark() throws Exception {
        // give the start signals
        sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
        sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);
        // wait for the data generators to finish their work
        LOGGER.info("WAITING FOR DATA GENERATOR ...");
        waitForDataGenToFinish();
        // wait for the task generators to finish their work
        LOGGER.info("WAITING FOR TASK GENERATOR ...");
        waitForTaskGenToFinish();
        // wait for the system to terminate
        LOGGER.info("WAITING FOR SYSTEM ...");
        waitForSystemToFinish();
        // Create the evaluation module
        String evalModuleImageName = "git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule";
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

    protected void insertTrainingDataToVirtuoso() throws Exception{
        LOGGER.info("Starting inserting data");
        String GRAPH_URI = "http://www.virtuoso-graph.com";
        String ENDPOINT = "http://"+containerName +":8890/sparql";
        String username = "dba";
        String password = "dba";
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream trainingData = classloader.getResourceAsStream("TrainingData.ttl");
        //cannot insert td ontology due to an error concerning blank nodes in the ttl file
        //as virtuoso cannot handle insert data with blank nodes
        //To test add tdOnto in dataToAdd list
        InputStream tdOnto = classloader.getResourceAsStream("td-ontology.ttl");
        List<InputStream> dataToAdd = Arrays.asList(tdOnto, trainingData);

        // authenticated client
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);

        HttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        HttpOp.setDefaultHttpClient(client);
        for (int i =0; i<dataToAdd.size();i++){// InputStream input : dataToAdd) {
            Model rdfModel = ModelFactory.createDefaultModel();
            rdfModel.read(dataToAdd.get(i), null, "TTL");
            StmtIterator it = rdfModel.listStatements();

            QuadDataAcc quads = new QuadDataAcc();
            quads.setGraph(NodeFactory.createURI(GRAPH_URI));

            // add triples from file into quads
            while (it.hasNext()) {
                Triple triple = it.next().asTriple();
                quads.addTriple(triple);
            }
            // insertion in chunks (virtuoso seems cannot handle batches larger than 1000)
            List<List<Quad>> chunks = org.apache.commons.collections4.ListUtils.partition(quads.getQuads(), 1000);
            //System.out.println(chunks.size());
            for (List<Quad> chunk : chunks) {
                QuadDataAcc quadChunk = new QuadDataAcc(chunk);
                //System.out.println(quadChunk.getQuads().get(0).asTriple().toString());
                String query = (new UpdateDataInsert(quadChunk)).toString(rdfModel);
                if (i==0) { //change this to 0 to insert td ontology with virtjdbc
                    String url = "jdbc:virtuoso://"+containerName+":1111";
                    //replace INSERT DATA with INSERT for virtuoso compatibility
                    query = query.replace("INSERT DATA", "INSERT");
                    VirtGraph set = new VirtGraph (url, username, password); //connection refused / no solution
                    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(query, set);
                    vur.exec();
                }else {
                    //execute insert query to virtuoso
                    UpdateProcessor update = UpdateExecutionFactory.createRemoteForm(UpdateFactory.create(query), ENDPOINT+"-auth");
                    ((UpdateProcessRemoteForm)update).setAuthentication(username,password.toCharArray());
                    update.execute();
                }
            }
        }

    }

    public static void main(String[] args) throws Exception {
        FacetedBenchmarkController fb = new FacetedBenchmarkController();
        fb.insertTrainingDataToVirtuoso();
    }


}

