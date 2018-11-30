package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.jena.ext.com.google.common.primitives.Bytes;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingEncoders;
import org.hobbit.core.component.BenchmarkVocab;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jersey.repackaged.com.google.common.primitives.Ints;

/**
 * V2 uses a different task encoding: the task id is used to store a rooted rdf graph,
 * so that metadata associated with the task can be sent around
 * 
 * 
 * @author Claus Stadler, Oct 23, 2018
 *
 */
public class FacetedBrowsingEncodersV2 {
	
	private static final Logger logger = LoggerFactory.getLogger(FacetedBrowsingEncodersV2.class);
	
	
    public static ByteBuffer formatForEvalStorage(Resource r, long timestamp, Gson gson) { //ResultSet resultSet,
        
    	Resource s = copyTaskResourceWithoutPayload(r);
    	
    	JsonObject json = FacetedBrowsingEncoders.resourceToJson(s);
    	
    	String taskIdStr = s.getURI();
        String payloadStr = gson.toJson(json);
        byte[] payloadBytes = payloadStr.getBytes(StandardCharsets.UTF_8);
        
//        String resultSetJsonStr = r.getProperty(RDFS.comment).getString();
//        byte[] data = resultSetJsonStr.getBytes(StandardCharsets.UTF_8);
        
        ByteBuffer result = ByteBuffer.wrap(RabbitMQUtils.writeByteArrays(null,
                new byte[][] { RabbitMQUtils.writeString(taskIdStr), payloadBytes }, RabbitMQUtils.writeLong(timestamp)));
        
        return result;
    }

	
    public static Resource decodeExpectedDataV2(ByteBuffer bufferExp, Gson gson) {

        //String taskidGold = RabbitMQUtils.readString(bufferExp);
        //logger.info("Eval_mod task Id: "+ taskidGold);
//        String scenarioStr = RabbitMQUtils.readString(bufferExp);
//        logger.info("Scenario id: "+ scenarioStr);
//        String queryIdStr = RabbitMQUtils.readString(bufferExp);
//        logger.info("query: "+ queryIdStr); // This is just the query id - not the string
//
//        String goldsString = RabbitMQUtils.readString(bufferExp);
//         LOGGER.info("goldsString: "+ goldsString);

    	
    	String payloadStr = new String(bufferExp.array(), StandardCharsets.UTF_8); //RabbitMQUtils.readString(bufferExp);
    	
    	
    	Resource result = FacetedBrowsingEncoders.jsonToResource(payloadStr, gson);
//        int scenarioId = Integer.parseInt(scenarioStr);
//        int queryId = Integer.parseInt(queryIdStr);
        
//        Resource result = ModelFactory.createDefaultModel()new String(buffer.array(), StandardCharsets.UTF_8).createResource(taskidGold)
//        		.addLiteral(FacetedBrowsingVocab.scenarioId, scenarioId)
//        		.addLiteral(FacetedBrowsingVocab.queryId, queryId)
//        		.addLiteral(BenchmarkVocab.expectedResult, goldsString);
//
//        QueryID key = new QueryID(scenarioId, queryId);
//
//        if(scenarioId != 0) {
//	        Set<Integer> cps = ChokePoints.getChokpointsForQueryId(key);
//	        if(cps == null) {
//	        	throw new RuntimeException("No chokepoint for query id: " + key);
//	        }
//
//	        for(Integer cp : cps) {
//	        	result.addLiteral(FacetedBrowsingVocab.chokepointId, cp);
//	        }
//        }

        return result;
    }
	
    
    public static Resource copyTaskResourceWithoutPayload(Resource r) {
    	// Create a copy of the input resource's model, and remove the task attribute from the resource
    	Model closure = ResourceUtils.reachableClosure(r);
    	Resource result = r.inModel(closure);
       	result.removeAll(BenchmarkVocab.taskPayload);

       	return result;
    }


//	public static ByteBuffer encodeTaskForSystemAdapter(Resource r) {
//    	String queryStr = r.getProperty(BenchmarkVocab.taskPayload).getString();
//
//    	Resource s = copyTaskResourceWithoutPayload(r);
//    	// Create a copy of the input resource's model, and remove the task attribute from the resource
////    	Model copy = ModelFactory.createDefaultModel();
////    	copy.add(r.getModel());
////    	Resource s = r.inModel(copy);
////    	s.removeAll(BenchmarkVocab.taskPayload);
//
//    	String taskIdStr = FacetedBrowsingEncoders.resourceToJson(s).toString();
//
////        byte[] tmp = RabbitMQUtils.writeByteArrays(
////                new byte[][] { RabbitMQUtils.writeString(taskIdStr), queryStr.getBytes(StandardCharsets.UTF_8) });
//
//    	byte[] taskIdBytes = taskIdStr.getBytes(StandardCharsets.UTF_8);
//    	byte[] queryBytes = queryStr.getBytes(StandardCharsets.UTF_8);
//
//    	byte[] payloadBytes = Bytes.concat(Ints.toByteArray(queryBytes.length), queryBytes);
//    	 
//     	ByteBuffer result = ByteBuffer.wrap(Bytes.concat(
//     			Ints.toByteArray(taskIdBytes.length),
//     			taskIdBytes,
//    			Ints.toByteArray(payloadBytes.length),
//    			payloadBytes));
////        ByteBuffer buffer = ByteBuffer.wrap(data);
////        String taskId = RabbitMQUtils.readString(buffer);
////        byte[] taskData = RabbitMQUtils.readByteArray(buffer);
//
//        
//        return result;
//    }
//    
    
    public static Resource decodeTaskForSystemAdapter(ByteBuffer buffer) {
    	
        String taskId = RabbitMQUtils.readString(buffer);
        byte[] taskData = RabbitMQUtils.readByteArray(buffer);
        String taskStr = RabbitMQUtils.readString(ByteBuffer.wrap(taskData));
        //String taskStr = RabbitMQUtils.readString(buffer);
        
        //byte[] payload = RabbitMQUtils.readByteArray(buffer);
        //String taskStr = RabbitMQUtils.readString(payload);
        //byte[] taskData = RabbitMQUtils.readByteArray(buffer);
        
        //String taskStr = new String(taskData, StandardCharsets.UTF_8);
        Resource result = FacetedBrowsingEncoders.jsonToResource(taskId, new Gson());
        
        //Resource result = ModelFactory.createDefaultModel().createResource(taskId)
        //		.addProperty(RDFS.label, taskStr);
        result
        		.addProperty(BenchmarkVocab.taskPayload, taskStr);
        
        return result;
    }
}
