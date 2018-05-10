package org.hobbit.benchmark.faceted_browsing.config;

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

public class ConfigEncodersFacetedBrowsing {

    @Bean
	public BiFunction<Resource, Long, ByteBuffer> taskEncoderForEvalStorage() {
    	return FacetedBrowsingEncoders::formatForEvalStorage;
    }
    
    
    
    @Bean
    public Function<Resource, ByteBuffer> taskEncoderForSystemAdapter() {
    	return FacetedBrowsingEncoders::formatResourceForSystemAdapter;
    }
    
    @Bean
    public Function<ByteBuffer, Resource> taskResourceDeserializer() {
    	return FacetedBrowsingEncoders::readResourceForSystemAdapter;
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
