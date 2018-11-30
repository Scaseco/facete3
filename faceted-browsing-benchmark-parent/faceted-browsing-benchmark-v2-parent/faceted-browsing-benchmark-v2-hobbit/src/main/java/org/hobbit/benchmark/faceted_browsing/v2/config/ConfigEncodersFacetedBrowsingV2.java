package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingEncoders;
import org.hobbit.core.data.Result;
import org.springframework.context.annotation.Bean;

import com.google.gson.Gson;

public class ConfigEncodersFacetedBrowsingV2 {

    @Bean
	public BiFunction<Resource, Long, ByteBuffer> taskEncoderForEvalStorage(Gson gson) {
    	return (r, timestamp) -> FacetedBrowsingEncodersV2.formatForEvalStorage(r, timestamp, gson);
    }
    
    
    
    @Bean
    public Function<Resource, ByteBuffer> taskEncoderForSystemAdapter() {
    	return FacetedBrowsingEncoders::encodeTaskForSystemAdapter;
    }
    
    @Bean
    public Function<ByteBuffer, Resource> taskResourceDeserializer() {
    	return FacetedBrowsingEncoders::decodeTaskForSystemAdapter;
    }
    

    // sa - to es
    @Bean
    public BiFunction<String, ResultSet, Stream<ByteBuffer>> actualResultEncoder() {
    	return FacetedBrowsingEncoders::formatActualSparqlResults;
    }
    
    
    // es - from tg
    @Bean
    public Function<ByteBuffer, Entry<String, Result>> expectedResultDecoder() {
    	return FacetedBrowsingEncoders::parseExpectedOrActualTaskResult;
    }

    // es - from sa
    @Bean
    public Function<ByteBuffer, Entry<String, Result>> actualResultDecoder() {
    	return FacetedBrowsingEncoders::parseExpectedOrActualTaskResult;
    }

}
