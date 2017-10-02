package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.core.rabbit.RabbitMQUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class FacetedBrowsingEncoders {

    // Functions for the legacy protocol...

    public static JsonObject resourceToJson(Resource r) {
        JsonObject result = new JsonObject();
        result.addProperty("subject", "" + r.asNode());
        result.addProperty("graphStr", RabbitMQUtils.writeModel2String(r.getModel()));
        return result;
    }
    
    
    public static Resource jsonToResource(String jsonStr, Gson gson) {
    	JsonObject json = gson.fromJson(jsonStr, JsonObject.class);
    	Resource result = jsonToResource(json);
    	return result;
    }
    
    public static Resource jsonToResource(JsonObject json) {
    	String subjectStr = json.get("subject").getAsString();
    	String graphStr = json.get("graphStr").getAsString();
    	
    	
    	Model model = RabbitMQUtils.readModel(graphStr);
    	Resource result = model.createResource(subjectStr);
    	
    	return result;
    }
//
//    public static ByteBuffer formatForSystemAdapter(Resource r) {
//    	String taskIdStr = r.getURI();
//    	String queryStr = r.getProperty(RDFS.label).getString();
//
//        byte[] tmp = RabbitMQUtils.writeByteArrays(
//                new byte[][] { RabbitMQUtils.writeString(taskIdStr), queryStr.getBytes(StandardCharsets.UTF_8) });
//
//        ByteBuffer result = ByteBuffer.wrap(tmp);
//        return result;
//
//    }
//
//    public static Resource readSystemAdapter(ByteBuffer r) {
//    	Resource result = 
//    	
//    	ByteBuffer result;
//        
//        ByteBuffer buffer = ByteBuffer.wrap(data);
//        String taskId = RabbitMQUtils.readString(buffer);
//        byte[] taskData = RabbitMQUtils.readByteArray(buffer);
//        receiveGeneratedTask(taskId, taskData);
//    	
//    }
    
    public static ByteBuffer formatForEvalStorage(Resource r, long timestamp) { //ResultSet resultSet,
        //String taskIdString = r.getURI();
        byte[] data = formatTaskForEvalStorageCore(r); //, resultSet);
        //RabbitMQUtils.writeString(taskIdString)
        byte[] tmp = RabbitMQUtils.writeByteArrays(
                new byte[][] { data , RabbitMQUtils.writeLong(timestamp)});

        ByteBuffer result = ByteBuffer.wrap(tmp);
        return result;
    }

    // It sucks that the formats at present have to differ because of ... reasons.
    // The eval storage gets the legacy format
    // the system adapter gets the more convenient format

    public static byte[] formatTaskForEvalStorageCore(Resource r) { //, ResultSet resultSet) {
        String taskId = r.getURI();
        //long timestamp = r.getProperty(DCTerms.created).getLong();
        //String replacedQuery = r.getProperty(RDFS.label).getString();
        String queryId = r.getProperty(FacetedBrowsingVocab.queryId).getString();
        String scenarioId = r.getProperty(FacetedBrowsingVocab.scenarioId).getString();
        String resultSetJsonStr = r.getProperty(RDFS.comment).getString();

        ResultSet resultSet = ResultSetFactory.fromJSON(new ByteArrayInputStream(resultSetJsonStr.getBytes(StandardCharsets.UTF_8)));
        
        byte[] result = FacetedBrowsingEncoders.adjustFormat(taskId, scenarioId, queryId, resultSet);

        return result;
    }

//    public static ByteBuffer formatTaskForSystemAdapter(Resource r, Gson gson) {
//        String payload = gson.toJson(resourceToJson(r));
//        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
//        ByteBuffer result = ByteBuffer.wrap(data);
//        return result;
//    }




       /**
     * Changes the format of the given data to byte array
     * @author gkatsimpras
     */
    public static byte[] adjustFormat(String taskId,String scenarioId, String queryId, ResultSet resultModel){
        // format the result as (scenarioId, queryId, data)
        StringBuilder listString = new StringBuilder();
        while(resultModel.hasNext()) {
            String value = (resultModel.next().get(resultModel.getResultVars().get(0)).toString());
            listString.append(value+",");
        }
        byte[] resultsByteArray = listString.toString().getBytes(StandardCharsets.UTF_8);
        byte[] taskIdBytes = taskId.getBytes(StandardCharsets.UTF_8);
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
    public static byte[] arrayListToByteArray(ArrayList<String> list){
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

}