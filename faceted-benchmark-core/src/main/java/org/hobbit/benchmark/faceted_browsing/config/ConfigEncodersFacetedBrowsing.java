package org.hobbit.benchmark.faceted_browsing.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.jena.rdf.model.Resource;
import org.hobbit.benchmark.faceted_browsing.components.FacetedBrowsingEncoders;
import org.hobbit.benchmark.faceted_browsing.components.TaskGeneratorModuleFacetedBrowsing;
import org.hobbit.core.component.TaskGeneratorModule;
import org.springframework.context.annotation.Bean;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ConfigEncodersFacetedBrowsing {

    @Bean
	public BiFunction<Resource, Long, ByteBuffer> taskEncoderForEvalStorage() {
    	return FacetedBrowsingEncoders::formatForEvalStorage;
    }
    
    @Bean
    public Function<Resource, ByteBuffer> taskEncoderForSystemAdapter(Gson gson) {
    	return (subResource) -> {
    		JsonObject json = FacetedBrowsingEncoders.resourceToJson(subResource);
    		ByteBuffer r = ByteBuffer.wrap(gson.toJson(json).getBytes(StandardCharsets.UTF_8));    
    		return r;
    	};
    }
    
    @Bean
    public TaskGeneratorModule taskGeneratorModule() {
    	return new TaskGeneratorModuleFacetedBrowsing();
    }
    

    @Bean
    public Function<ByteBuffer, Resource> taskResourceDeserializer(Gson gson) {
    	return (buffer) -> {
    		String jsonStr = new String(buffer.array(), StandardCharsets.UTF_8);
    		org.apache.jena.rdf.model.Resource r = FacetedBrowsingEncoders.jsonToResource(jsonStr, gson);
    		return r;
    	};
    }

}
