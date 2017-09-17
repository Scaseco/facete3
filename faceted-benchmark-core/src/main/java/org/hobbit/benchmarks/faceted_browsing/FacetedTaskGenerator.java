package org.hobbit.benchmarks.faceted_browsing;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.DatatypeConverter;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This is the Task Generator class.
 * @author gkatsimpras
 */
public class FacetedTaskGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacetedTaskGenerator.class);

    // arrays for reason classes and sub reason instances
    private static Map<String, List<String>> reasonClasses = new HashMap<>();
    private static List<String> allReasons = new ArrayList<>();

    // a hashmap to hold variables that have to be pre-computed
    private Map<String, Object> globalVariables = new HashMap<>();
    private Map<String, List<Map<String, String>>> variables = null;
    private Map<String, Object> preQueries = null;
    private Map<String, List<Map<String, Map<String, String>>>>  scenarios = null;




    // private Semaphore startTaskGenMutex = new Semaphore(0);
    // private Semaphore terminateMutex = new Semaphore(0);
    private final int maxParallelProcessedMsgs=1;
    // next Task id
    private long nextTaskId = 0;

    private int randomSeed = 1234;
    //
    protected String containerName = null;
    // Virtuoso settings for gold-system service
    protected String VIRTUOSO_GOLD_SERVICE_URL; //"http://localhost:8890/sparql";
    //protected String VIRTUOSO_GOLD_GRAPH_IRI = "http://trainingdata.org";
    //protected String VIRTUOSO_TD_GRAPH_IRI = "http://tdonto.org";
    String GRAPH_URI = "http://www.virtuoso-graph.com";
    protected List<String> VIRTUOSO_GRAPHS = Arrays.asList(GRAPH_URI);
    protected int VIRTUOSO_TIMEOUT_SECS = 100;


    protected SparqlQueryConnection queryConn;


    public void init() throws Exception {
        LOGGER.info("INITIALIZING REASONS...");
        initializeReasonClasses();
        LOGGER.info("INITIALIZING PARAMETERS...");
        loadParameterFiles();
        computeParameters();
        LOGGER.info("DONE!");
    }
//
//    LOGGER.info("Executing query for gold standard...");
//    // finally execute query against gold-system
//    ResultSet expectedResult = executeSparqlQuery(replacedQuery);
//    //ResultSetFormatter.outputAsCSV(System.out, expectedResult);
//    // convert result to model
//    //Model expectedModel = RDFOutput.encodeAsModel(expectedResult);
//    LOGGER.info("Received Answer from golden system..");
//    // convert result to byte arraylist
//    byte[] resultsByteArray = adjustFormat(taskIdString,
//            scenarioName, Integer.toString(queryid), expectedResult);
//
//
//    byte[] task = RabbitMQUtils.writeByteArrays(new byte[][] { RabbitMQUtils.writeString(replacedQuery)});
//
//    // send task to systemAdapter
//    long timestamp= System.currentTimeMillis();
//    sendTaskToSystemAdapter(taskIdString,
//            task);
//    sendTaskToEvalStorage(taskIdString,
//            timestamp,
//            resultsByteArray);
//    // send task and expected result to eval storage
//    LOGGER.info("Waiting for acknowledgment..");
//    //waitForAck();
//    //adding time delay to simulate sequential order
//    // TimeUnit.MILLISECONDS.sleep(1500);
//    LOGGER.info("Acknoledgment done!");
//

    protected Stream<String> generateTasks() {
        List<String> resultList = new ArrayList<>();

        ValueComparator vls = new ValueComparator();
        Map<String, List<Map<String, Map<String, String>>>> sortedScenarios = new TreeMap<>(vls);
        sortedScenarios.putAll(scenarios);
        for(Entry<String, List<Map<String, Map<String, String>>>> entry : sortedScenarios.entrySet()) {
            List<Map<String, Map<String, String>>> scenarioQueries = entry.getValue();
            for(Map<String, Map<String, String>> queries : scenarioQueries) {
                //LOGGER.info("query name: " + queries.entrySet());

                int queryid = 0;
                for (Entry<String, Map<String, String>> query : queries.entrySet()) {
                    String scenarioName = entry.getKey();
                    if (query.getKey().contains("Count")){
                        scenarioName = "Scenario_0";
                    }
                    queryid += 1;
                    //String taskIdString = getNextTaskId();
                    //LOGGER.info("ASSIGNED TASKID--: " + taskIdString);
                    //LOGGER.info("NEXT TASKID--: " + nextTaskId);
                    //setTaskIdToWaitFor(taskIdString);
                    // retrieve query string and replace the parameters
                    String replacedQuery = "";
                    String querySparql = (String) (query.getValue()).get("query");
                    String params = (String) (query.getValue()).get("parameters");
                    if (params!=null) {
                        replacedQuery = replaceParameters(querySparql, params);
                    }
                    else {
                        replacedQuery = querySparql;
                    }

                    resultList.add(replacedQuery);


                }
            }
        }

        return resultList.stream();
    }

    protected String getTheNextTaskId() {
        String taskIdString = Long.toString(nextTaskId);
        nextTaskId += 1;
        return taskIdString;
    }

    private void initializeReasonClasses() {
        reasonClasses.put("td:AbnormalTraffic", Arrays.asList(
            "td:QueuingTraffic",
            "td:SlowTraffic"
        ));
        reasonClasses.put("td:Accident", Arrays.asList(
            "td:Derailment",
            "td:CollisionWithAnimal",
            "td:CollisionWithPerson",
            "td:HeadOnCollision"
        ));
        reasonClasses.put("td:Activity", Arrays.asList(
            "td:CivilEmergency",
            "td:PoliceInvestigation",
            "td:IllVehicleOccupants",
            "td:BombAlert",
            "td:Demonstration",
            "td:AltercationOfVehicleOccupants",
            "td:Strike"
        ));
        reasonClasses.put("td:EnvironmentalConditions", Arrays.asList(
            "td:StrongWinds",
            "td:ExtremeCold",
            "td:BadWeather",
            "td:ExtremeHeat",
            "td:HeavySnowfall"
        ));
        reasonClasses.put("td:Obstruction", Arrays.asList(
            "td:FallenTrees",
            "td:Flooding",
            "td:StormDamage",
            "td:FallenPowerCables",
            "td:DamagedTunnel",
            "td:AbnormalLoad",
            "td:BrokenDownTrain",
            "td:DamagedVehicle"
        ));
        reasonClasses.put("td:InfrastructureWorks", Arrays.asList(
            "td:ConstructionWork",
            "td:MaintenanceWork",
            "td:RepairWork",
            "td:TreeAndVegetationCuttingWork",
            "td:TrafficSignalsFailure"
        ));

        for (Entry<String, List<String>> reasonclass : reasonClasses.entrySet()) {
            List<String> reasons = reasonclass.getValue();
            for (String reason : reasons) {
                allReasons.add(reason);
            }
        }

    }

    /**
     * Initialize generator with files and parameters
     * @author gkatsimpras
     */
    private void initializeParameters(){
        initializeReasonClasses();
        this.loadParameterFiles();
        this.computeParameters();
    }

    public <T> T loadTaskResource(String resourceName, TypeToken<T> typeToken) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        Type mapToListOfMapsType = typeToken.getType();//new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType();

        Gson gson = new Gson();
        T result = gson.fromJson(new InputStreamReader(classloader.getResourceAsStream(resourceName), StandardCharsets.UTF_8), mapToListOfMapsType);

        return result;
    }

    /**
     * Load the parameters files from resources folder
     * @author gkatsimpras
     */
    private void loadParameterFiles(){
        variables = loadTaskResource("variables.json", new TypeToken<Map<String, List<Map<String, String>>>>() {});
        preQueries = loadTaskResource("Preparational_Queries_decimalLatLong_and_improved.json", new TypeToken<Map<String, Object>>() {});
        scenarios = loadTaskResource("SPARQL_Queries_decimalLatLong.json", new TypeToken<Map<String, List<Map<String, Map<String, String>>>>>() {});
    }

    /**
     * Precompute the parameters needed for the execution of the benchmark
     * @author gkatsimpras
     */
    private void computeParameters() {
        Random random = new Random(randomSeed);

        // compute variables For_All
        List<Map<String, String>> forAll = (List<Map<String, String>>)preQueries.get("For_All");
        for (Map<String, String> entry : forAll){
            Object finalValue = computeVariable(entry);
            globalVariables.put((String) entry.get("variable"), finalValue);
        }
        // construct queries for each scenario
        for (Entry<String, List<Map<String, Map<String, String>>>> entry : scenarios.entrySet()) {
            String scenarioName = entry.getKey();
            // get all variables for this scenario
            List<Map<String, String>> scenarioVariables = variables.get(scenarioName);
            // compute variables
            for (Map<String, String> scVar : scenarioVariables){
                String formattedpreQuery = "";
                String vname = (String) scVar.get("variable");
                String command = (String) scVar.get("from");
                String resultText = "";
                // we have different cases to check
                if (command.equals("preparation")) {
                    String vquery = ((Map<String, Map<String, String>>)preQueries.get(scenarioName)).get(vname).get("query");
                    // find the variable in preQueries
                    String qParams = ((Map<String, Map<String, String>>)preQueries.get(scenarioName)).get(vname).get("parameters");
                    if (qParams!=null) {
                        // replace the parameters of the query with their values
                        formattedpreQuery = replaceParameters(vquery, qParams);
                    }
                    else {
                        formattedpreQuery = vquery;
                    }
                    // execute query
                    List<String> resultRows = new ArrayList<>();
                    try(QueryExecution qe = queryConn.query(formattedpreQuery)) {
                        ResultSet result = qe.execSelect();
                        //ResultSetFormatter.outputAsCSV(System.out, result);
                        String resname = result.getResultVars().get(0);
                        while(result.hasNext()){
                            QuerySolution nextObj = result.next();
                            resultRows.add(nextObj.get(resname).toString());
                        }
                    }
                    // pick a random row from the results as final answer
                    if (!resultRows.isEmpty()) {
                        resultText = resultRows.get(random.nextInt(resultRows.size()));
                    }
                    else{
                        resultText = "";
                    }
                    // add to global variables
                    globalVariables.put(vname, resultText);
                }
                else if (command.startsWith("[")){
                    // time interval
                    String[] commandVars = command.replaceAll("\\[|\\]", "").split(",");
                    //System.out.println(globalVariables.get(commandVars[0].trim()));
                    long minTime = ((Calendar) globalVariables.get(commandVars[0].trim())).getTimeInMillis();
                    long maxTime = ((Calendar) globalVariables.get(commandVars[1].trim())).getTimeInMillis();
                    //Random random = new Random();

                    long randomTime = minTime +
                            (long)(random.nextDouble()*(maxTime - minTime));
                    Calendar randomTimestamp = Calendar.getInstance();
                    randomTimestamp.setTimeInMillis(randomTime);
                    globalVariables.put(vname, randomTimestamp);
                }
                else if (command.contains("eason")) {
                    if (command.contains("sub")) {
                        // associate the reason variable
                        String reason_name = "";
                        Matcher m = Pattern.compile("\\((.*?)\\)").matcher(command);
                        while(m.find()) {
                            reason_name = m.group(1);
                        }
                        String reasonClass = (String)globalVariables.get(reason_name);
                        // get a random sub reason
                        List<String> subreasons = reasonClasses.get(reasonClass);
                        String randomSubReason = (String)subreasons.get(new Random(randomSeed).nextInt((subreasons.size())));
                        //
                        globalVariables.put(vname, randomSubReason);

                    }
                    else if (command.contains("lass")) {
                        // choose class
                        ArrayList keysReasons = new ArrayList<String>(reasonClasses.keySet());
                        String randomClass = (String)keysReasons.get(new Random(randomSeed).nextInt(keysReasons.size()));
                        globalVariables.put(vname, randomClass);
                    }
                    else {
                        // pick any random reason
                        String randomReason = allReasons.get(new Random(randomSeed).nextInt(allReasons.size()));
                        globalVariables.put(vname, randomReason);
                    }

                }
                else if (command.startsWith("PT")){
                    globalVariables.put(vname, command);
                }
                else {
                    // execute command in string
                    Object result = executeMathExpr(command);
                    // add to global variables
                    globalVariables.put(vname, result);
                }
            }
        }
    }

    /**
     * Compute a variable value (mostly used for For_All variables)
     * @author gkatsimpras
     */
    private Object computeVariable(Map<String, String> entry) {
        String fquery = entry.get("query");
        Object finalValue = "";
        //Object finalValue = "0.0";

        try(QueryExecution qe = queryConn.query(fquery)) {
            ResultSet result = qe.execSelect();
            //ResultSetFormatter.outputAsCSV(System.out, result);
            String resname = result.getResultVars().get(0);
            if(result.hasNext()){
                RDFNode nodefinalValue = result.next().get(resname);
                String valueNode = null;
                if (nodefinalValue!=null) {
                    valueNode = nodefinalValue.toString();
                    LOGGER.error("String value node: " + valueNode);
                }
                //LOGGER.error("String : " + nodefinalValue.toString());
                if (valueNode!=null) {
                    if (valueNode.contains("date")) {
                        String dateString = valueNode.split("\\.")[0];
                        LOGGER.error("data: " + dateString);
                        Date fastDate = null;
                        FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");
                        try {
                            fastDate = fastDateFormat.parse(dateString);
                        } catch (ParseException e) {
                            LOGGER.error("Error while parsing date format!");
                        }
                        Calendar c = Calendar.getInstance();
                        c.setTime(fastDate);
                        finalValue = c;
                    }
                    else if (valueNode.contains("duration")){
                        finalValue = valueNode.split("\\^")[0];
                    }
                    else{
                        finalValue = valueNode.split("\\^")[0];
                    }
                }
            }
            else{
                finalValue = "";
            }
        }
        return finalValue;
    }

    /**
     * Evaluate a String math expression and return the result
     * @author gkatsimpras
     */
    private Object executeMathExpr(String expression){
        String[] commandVars = expression.split("[-+*/().\\d]");
        for (String var : commandVars){
            if (var.length()>2) {
                expression = expression.replace(var, (String)globalVariables.get(var));
            }
        }
        // evaluate expression
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        Object result = null;
        try {
            result = engine.eval(expression);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Replace the String parameters with their actual value
     * @author gkatsimpras
     */
    private String replaceParameters(String vquery, String qParams){
        String[] qS = qParams.replace(" ", "").split(",");
        for (int index =0; index < qS.length; index++){
            if (globalVariables.get(qS[index])!="") {
                Object calendar = globalVariables.get(qS[index]);
                if (calendar instanceof GregorianCalendar) {
                    Date calendarTime = ((GregorianCalendar) calendar).getTime();
                    String xmlDateTime = DatatypeConverter.printDateTime(((GregorianCalendar) calendar));
                    xmlDateTime = "\"" + xmlDateTime + "\"";
                    qS[index] = xmlDateTime;
                } else if (qS[index].contains("route") || qS[index].contains("trip")) {
                    qS[index] = "<" + (globalVariables.get(qS[index])).toString() + ">";
                } else if (qS[index].contains("reason")) {
                    String reasonValue = (globalVariables.get(qS[index])).toString();
                    if (reasonValue.contains("http")) {
                        reasonValue = "td:" + reasonValue.split("#")[1];
                    }
                    qS[index] = reasonValue;
                } else {
                    qS[index] = "\"" + (globalVariables.get(qS[index])).toString() + "\"";
                }
            } else {
                qS[index] = "\"" + (globalVariables.get(qS[index])).toString() + "\"";
            }
        }
        // construct query
        String replacedQuery = String.format(vquery, qS);
        return replacedQuery;
    }

//    /**
//     * Execute a sparql query on a defined server
//     * @author gkatsimpras
//     */
//    private ResultSet executeSparqlQuery(String query){
//        ResultSet result;
//        HttpAuthenticator authenticator = new SimpleAuthenticator("dba", "dba".toCharArray());
//        QueryExecution queryEx = QueryExecutionFactory.sparqlService(VIRTUOSO_GOLD_SERVICE_URL,
//                query, VIRTUOSO_GRAPHS, VIRTUOSO_GRAPHS, authenticator);
//        queryEx.setTimeout(VIRTUOSO_TIMEOUT_SECS, TimeUnit.SECONDS) ;
//
//        result = queryEx.execSelect();
//        return result;
//    }

    /**
     * Changes the format of the given data to byte array
     * @author gkatsimpras
     */
    private byte[] adjustFormat(String taskId,String scenarioId, String queryId, ResultSet resultModel){
        // format the result as (scenarioId, queryId, data)
        StringBuilder listString = new StringBuilder();
        while(resultModel.hasNext()) {
            String value = (resultModel.next().get(resultModel.getResultVars().get(0)).toString());
            listString.append(value+",");
        }
        byte[] resultsByteArray = listString.toString().getBytes(Charsets.UTF_8);
        byte[] taskIdBytes = taskId.getBytes(Charsets.UTF_8);
        byte[] scenario = (scenarioId.replaceAll("[^0-9]", "")).getBytes();
        byte[] query = queryId.getBytes();

        int capacity = 4 + 4 + 4 + 4 + taskIdBytes.length + scenario.length + query.length + resultsByteArray.length;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.putInt(taskIdBytes.length);
        buffer.put(taskIdBytes);
        buffer.putInt(scenario.length);
        buffer.put(scenario);
        buffer.putInt(query.length);
        buffer.put(query);
        buffer.putInt(resultsByteArray.length);
        buffer.put(resultsByteArray);
        byte[] finalArray = buffer.array();
        return finalArray;
    }

    /**
     * Converts a ArrayLIst to a byte array
     * @author gkatsimpras
     */
    private byte[] arrayListToByteArray(ArrayList<String> list){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        for (String element : list) {
            try {
                out.writeUTF(element);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] bytes = baos.toByteArray();
        return bytes;
    }


    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

/*


    @Override
    public void run() throws Exception {
        sendToCmdQueue(Commands.TASK_GENERATOR_READY_SIGNAL);
        // Wait for the start message
        startTaskGenMutex.acquire();
        generateTask(new byte[]{});
        sendToCmdQueue(Commands.TASK_GENERATION_FINISHED);
    }
*/


/*    @Override    public void receiveCommand(byte command, byte[] data) {
        // If this is the signal to start the data generation
        if (command == Commands.TASK_GENERATOR_START_SIGNAL) {
            LOGGER.info("Received signal to start.");
            // release the mutex
            // startTaskGenMutex.release();
        } else if (command == Commands.TASK_GENERATION_FINISHED) {
            LOGGER.info("Received signal to finish.");

                terminateMutex.release();

        } else if (command == Commands.DATA_GENERATION_FINISHED){
            LOGGER.info("Data generation finished");

        } else if (command == (byte) 150 ){
            try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) {}
            startTaskGenMutex.release();
        }
        super.receiveCommand(command, data);
    }

*/
    public void setQueryConn(SparqlQueryConnection queryConn) {
        this.queryConn = queryConn;
    }

    public static void main(String[] args) throws SQLException, IOException {

        //StaticTripleSupplier supplier = new StaticTripleSupplier("lc.ttl");

        String url = "jdbc:virtuoso://localhost:1112";
        String targetGraphIri = "http://mygraph.org/";


        try(Connection conn = DriverManager.getConnection(url, "dba", "dba")) {
            VirtuosoBulkLoad.logEnable(conn, 2, 0);
            VirtuosoBulkLoad.dropGraph(conn, targetGraphIri, true);
            VirtuosoBulkLoad.checkpoint(conn);

            Stream<Triple> stream = GraphUtils.createTripleStream("lc.ttl");

            VirtuosoBulkLoad.load(conn, stream, "http://mygraph.org/", 10000);
            VirtuosoBulkLoad.checkpoint(conn);
        }





        FacetedTaskGenerator gen = new FacetedTaskGenerator();

        SparqlQueryConnection queryConn = new RDFConnectionModular(
                new SparqlQueryConnectionJsa(
                        FluentQueryExecutionFactory.http("http://localhost:8891/sparql", targetGraphIri)
                .create()), null, null);

        gen.setQueryConn(queryConn);
        gen.initializeParameters();
        List<String> a = gen.generateTasks().collect(Collectors.toList());
        List<String> b = gen.generateTasks().collect(Collectors.toList());
        List<String> c = gen.generateTasks().collect(Collectors.toList());

        System.out.println(a.size());
        System.out.println(a.equals(b));
        System.out.println(a.equals(c));
//        gen.generateTasks().forEach(task -> System.out.println(task));
//        System.out.println(gen.generateTasks().count());
//        System.out.println(gen.generateTasks().count());
//        System.out.println(gen.generateTasks().count());
    }
}

class ValueComparator implements Comparator<String> {

    public int compare(String o1, String o2) {
        return new Integer(o1.replaceAll("Scenario_", ""))
                .compareTo(new Integer(o2.replaceAll("Scenario_", "")));
    }
}