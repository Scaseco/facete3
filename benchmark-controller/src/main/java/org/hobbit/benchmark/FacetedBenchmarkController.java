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

   /* protected void insertTrainingDataToVirtuoso() throws Exception{
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

/*

 */
   /* protected void insertDataWithScript() throws Exception{
        // We insert data to virtuoso instance using the isl
        // commands tool by executing a scipt
        //docker cp load_data_virtuoso.sh my-virtuoso:/usr/local/virtuoso-opensource/var/lib/virtuoso/db/load_data.sh
        String filesPath = System.getProperty("user.dir")+"/src/main/resources/load_data_virtuoso.sh";
        System.out.println(System.getProperty("user.dir"));
        containerName = "my-virtuoso";
        String command1 = "docker cp "+filesPath+" "+containerName+":/usr/local/virtuoso-opensource/var/lib/virtuoso/db/load_data.sh";
        String command2 = "docker cp TrainingData.ttl "+containerName+":/usr/local/virtuoso-opensource/var/lib/virtuoso/db/trainingdata.ttl";
        String command3 = "docker cp td-ontology.ttl "+containerName+":/usr/local/virtuoso-opensource/var/lib/virtuoso/db/td.ttl";
        String command4 = "docker exec -i "+containerName+" bash -c 'chmod +x load_data.sh'";
        List<String> commadsList = Arrays.asList(command1, command2, command3, command4);


        String[] command = {"docker", "cp", filesPath, containerName+":/usr/local/virtuoso-opensource/var/lib/virtuoso/db/load_data.sh"};
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process proc = pb.start();
        InputStream is = proc.getInputStream();
        OutputStream os = proc.getOutputStream();

        BufferedReader reader
                = new BufferedReader(new InputStreamReader(is));

        BufferedWriter writer
                = new BufferedWriter(new OutputStreamWriter(os));
        writer.write("pwd");
        writer.flush();
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.print(line + "\n");
        }

        proc.waitFor();

        for (String commandw : commadsList){
            //Runtime rt = Runtime.getRuntime();
            //System.out.println(command);


        }



    }

*/



   /* @Override
    public void receiveCommand(byte command, byte[] data) {

        switch (command) {
            case Commands.START_BENCHMARK_SIGNAL: {
                startBenchmarkMutex.release();
                systemContainerId = RabbitMQUtils.readString(data);
                break;
            }
            case Commands.DATA_GENERATOR_READY_SIGNAL: {
                LOGGER.debug("Received DATA_GENERATOR_READY_SIGNAL");
                dataGenReadyMutex.release();
                break;
            }
            case Commands.TASK_GENERATOR_READY_SIGNAL: {
                LOGGER.debug("Received TASK_GENERATOR_READY_SIGNAL");
                taskGenReadyMutex.release();
                break;
            }
            case Commands.EVAL_STORAGE_READY_SIGNAL: {
                LOGGER.debug("Received EVAL_STORAGE_READY_SIGNAL");
                evalStoreReadyMutex.release();
                break;
            }
            case Commands.DOCKER_CONTAINER_TERMINATED: {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                String containerName = RabbitMQUtils.readString(buffer);
                int exitCode = buffer.get();
                containerTerminated(containerName, exitCode);
                break;
            }
            case Commands.EVAL_MODULE_FINISHED_SIGNAL: {
                setResultModel(RabbitMQUtils.readModel(data));
                LOGGER.info("model size = " + resultModel.size());
            }

            //case (byte)150: {
            //    try {
            //        LOGGER.info("Starting Task Generator...");
            //        sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
            //        LOGGER.info("WAITING FOR TASK GENERATOR ...");
            //    } catch (IOException e) {
            //        e.printStackTrace();
             //   }
            // }
        }
        super.receiveCommand(command, data);
    }
    */

    /*public static void main(String[] args) throws Exception {
        FacetedBenchmarkController fb = new FacetedBenchmarkController();
        fb.insertDataWithScript();
    }
*/


}

