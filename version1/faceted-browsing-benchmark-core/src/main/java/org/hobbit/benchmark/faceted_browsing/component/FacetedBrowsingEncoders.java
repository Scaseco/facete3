package org.hobbit.benchmark.faceted_browsing.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.ext.com.google.common.primitives.Bytes;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.hobbit.core.component.BenchmarkVocab;
import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultImpl;
import org.hobbit.core.data.Result;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jersey.repackaged.com.google.common.primitives.Ints;

public class FacetedBrowsingEncoders {
	
    public static String resultSetToJsonStr(ResultSet rs) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ResultSetFormatter.outputAsJSON(baos, rs); //resultSet);
    	//baos.flush();
    	String resultSetStr;
    	try {
    		resultSetStr = baos.toString(StandardCharsets.UTF_8.name());
    	} catch(UnsupportedEncodingException e) {
    		throw new RuntimeException(e);
    	}

    	return resultSetStr;
    }
    


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

    
    
    // TODO Make a unit test
    /**
     * Test a round trip of the encoding for the system adapter
     * @param args
     */
    public static void main(String[] args) {
    	Resource a = ModelFactory.createDefaultModel().createResource("http://example.org/task1")
    			.addProperty(BenchmarkVocab.taskPayload, "task specification string");
    	
    	Resource b = decodeTaskForSystemAdapter(encodeTaskForSystemAdapter(a));
    	System.out.println(a.getProperty(BenchmarkVocab.taskPayload).getString().equals(b.getProperty(BenchmarkVocab.taskPayload).getString()));
    	System.out.println(a.getURI().equals(b.getURI()));
    }
    
    
    public static ByteBuffer encodeTaskForSystemAdapter(Resource r) {
    	String taskIdStr = r.getURI();
    	String queryStr = r.getProperty(BenchmarkVocab.taskPayload).getString();

//        byte[] tmp = RabbitMQUtils.writeByteArrays(
//                new byte[][] { RabbitMQUtils.writeString(taskIdStr), queryStr.getBytes(StandardCharsets.UTF_8) });

    	byte[] taskIdBytes = taskIdStr.getBytes(StandardCharsets.UTF_8);
    	byte[] queryBytes = queryStr.getBytes(StandardCharsets.UTF_8);

    	byte[] payloadBytes = Bytes.concat(Ints.toByteArray(queryBytes.length), queryBytes);
    	 
     	ByteBuffer result = ByteBuffer.wrap(Bytes.concat(
     			Ints.toByteArray(taskIdBytes.length),
     			taskIdBytes,
    			Ints.toByteArray(payloadBytes.length),
    			payloadBytes));
//        ByteBuffer buffer = ByteBuffer.wrap(data);
//        String taskId = RabbitMQUtils.readString(buffer);
//        byte[] taskData = RabbitMQUtils.readByteArray(buffer);

        
        return result;
    }
    
    
    public static Resource decodeTaskForSystemAdapter(ByteBuffer buffer) {
    	
        String taskId = RabbitMQUtils.readString(buffer);
        byte[] taskData = RabbitMQUtils.readByteArray(buffer);
        String taskStr = RabbitMQUtils.readString(ByteBuffer.wrap(taskData));
        //String taskStr = RabbitMQUtils.readString(buffer);
        
        //byte[] payload = RabbitMQUtils.readByteArray(buffer);
        //String taskStr = RabbitMQUtils.readString(payload);
        //byte[] taskData = RabbitMQUtils.readByteArray(buffer);
        
        //String taskStr = new String(taskData, StandardCharsets.UTF_8);
        
        Resource result = ModelFactory.createDefaultModel().createResource(taskId)
        		.addProperty(BenchmarkVocab.taskPayload, taskStr);
        
        return result;
    }
    
    
    public static ByteBuffer writeResourceJson(Resource subResource, Gson gson) {
		JsonObject json = FacetedBrowsingEncoders.resourceToJson(subResource);
		ByteBuffer r = ByteBuffer.wrap(gson.toJson(json).getBytes(StandardCharsets.UTF_8));    
		return r;
    }

 
    public Resource readResourceJson(ByteBuffer buffer, Gson gson) {
		String jsonStr = new String(buffer.array(), StandardCharsets.UTF_8);
		org.apache.jena.rdf.model.Resource r = FacetedBrowsingEncoders.jsonToResource(jsonStr, gson);
		return r;	
    }
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
        
        String taskIdStr = r.getURI();
        
        byte[] data = formatTaskForEvalStorageCore(r);
        
//        String resultSetJsonStr = r.getProperty(RDFS.comment).getString();
//        byte[] data = resultSetJsonStr.getBytes(StandardCharsets.UTF_8);
        
        ByteBuffer result = ByteBuffer.wrap(RabbitMQUtils.writeByteArrays(null,
                new byte[][] { RabbitMQUtils.writeString(taskIdStr), data }, RabbitMQUtils.writeLong(timestamp)));
        
        return result;
    }

    // It sucks that the formats at present have to differ because of ... reasons.
    // The eval storage gets the legacy format
    // the system adapter gets the more convenient format

    public static byte[] formatTaskForEvalStorageCore(Resource r) { //, ResultSet resultSet) {
        String taskId = r.getURI();
        //long timestamp = r.getProperty(DCTerms.created).getLong();
        //String replacedQuery = r.getProperty(RDFS.label).getString();
        String queryId = "" + ResourceUtils.getLiteralPropertyValue(r, FacetedBrowsingVocab.queryId, Integer.class);
        String scenarioId = "" + ResourceUtils.getLiteralPropertyValue(r, FacetedBrowsingVocab.scenarioId, Integer.class);
        String resultSetJsonStr = r.getProperty(BenchmarkVocab.expectedResult).getString();

        ResultSet resultSet = ResultSetFactory.fromJSON(new ByteArrayInputStream(resultSetJsonStr.getBytes(StandardCharsets.UTF_8)));
        
        byte[] result = FacetedBrowsingEncoders.adjustFormatForEvalStorage(taskId, scenarioId, queryId, resultSet);

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
    public static byte[] adjustFormatForEvalStorage(String taskId,String scenarioId, String queryId, ResultSet resultModel){
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


    // HACK From stack overflow (TODO add link)
//    private String convertStreamToString(InputStream is) {
//        Scanner s = new Scanner(is).useDelimiter("\\A");
//        return s.hasNext() ? s.next() : "";
//    }

    
    
    public static Entry<String, Result> parseExpectedOrActualTaskResult(ByteBuffer buffer) {
        String taskId = RabbitMQUtils.readString(buffer);
        byte[] taskData = RabbitMQUtils.readByteArray(buffer);

        //System.out.println("For " + consumer + " Received taskId " + taskId);
        
        // FIMXE hack for timestamps
        long timestamp = buffer.hasRemaining() ? buffer.getLong() : System.currentTimeMillis();

        Result r = new ResultImpl(timestamp, taskData);

        Entry<String, Result> result = new SimpleEntry<>(taskId, r);
        return result;
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
    
    
	private static final Logger logger = LoggerFactory.getLogger(FacetedBrowsingEncoders.class);

	
	
	
	public static ByteBuffer formatActualResults(String taskIdStr, byte[] data) {
        byte[] taskIdBytes = taskIdStr.getBytes(StandardCharsets.UTF_8);
        // + 4 for taskIdBytes.length
        // + 4 for data.length
        int capacity = 4 + 4 + taskIdBytes.length + data.length;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.putInt(taskIdBytes.length);
        buffer.put(taskIdBytes);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.rewind();
        
        return buffer;
	}
    
    public static Stream<ByteBuffer> formatActualSparqlResults(String taskIdStr, ResultSet rs) {
        //String taskIdStr = task.getURI();
    	
    	ResultSetMem rsMem = new ResultSetMem(rs);
    	int numRows = ResultSetFormatter.consume(rsMem);
    	rsMem.rewind();
        logger.info("Actual Number of result set rows for task " + taskIdStr + ": " + numRows);

    	
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(out, rsMem);

        //sendResultToEvalStorage(taskId, outputStream.toByteArray());

        //String taskIdStr = "task-id-foobar";
        byte[] data = out.toByteArray();

        ByteBuffer b = formatActualResults(taskIdStr, data);

        
        Collection<ByteBuffer> result = Collections.singleton(b);
        return result.stream();
    }
    
    
    
}