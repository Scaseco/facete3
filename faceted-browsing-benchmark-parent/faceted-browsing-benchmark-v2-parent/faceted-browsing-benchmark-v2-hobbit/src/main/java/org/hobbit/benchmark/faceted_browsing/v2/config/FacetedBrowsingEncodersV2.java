package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.jena.ext.com.google.common.primitives.Bytes;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingEncoders;
import org.hobbit.core.rabbit.RabbitMQUtils;

import com.google.gson.Gson;

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
	public static final Property TASK_PROPERTY = RDFS.label;
	
	public static ByteBuffer encodeTaskForSystemAdapter(Resource r) {
    	String queryStr = r.getProperty(TASK_PROPERTY).getString();

    	// Create a copy of the input resource's model, and remove the task attribute from the resource
    	Model copy = ModelFactory.createDefaultModel();
    	copy.add(r.getModel());
    	Resource s = r.inModel(copy);
    	s.removeAll(TASK_PROPERTY);

    	String taskIdStr = FacetedBrowsingEncoders.resourceToJson(s).toString();

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
        Resource result = FacetedBrowsingEncoders.jsonToResource(taskId, new Gson());
        
        //Resource result = ModelFactory.createDefaultModel().createResource(taskId)
        //		.addProperty(RDFS.label, taskStr);
        result
        		.addProperty(TASK_PROPERTY, taskStr);
        
        return result;
    }
}
