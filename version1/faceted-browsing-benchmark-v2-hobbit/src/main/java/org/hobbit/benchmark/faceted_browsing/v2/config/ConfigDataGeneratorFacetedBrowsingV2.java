package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.main.HobbitBenchmarkUtils;
import org.hobbit.core.Constants;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderFactory;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;


public class ConfigDataGeneratorFacetedBrowsingV2 {
//    @Bean
//    public TripleStreamSupplier dataGenerationMethod(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory, @Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr) {
//    	return () -> Stream.of(new Triple(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property));
//    }
	
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigDataGeneratorFacetedBrowsingV2.class);
	

    @Bean
    public TripleStreamSupplier dataGenerationMethod(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory, @Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr)
    	throws Exception {
    	

        logger.info("DG: Supplied param model is: " + paramModelStr);
        
        // Load the benchmark.ttl config as it contains the parameter mapping
        Model paramModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(paramModel, new ByteArrayInputStream(paramModelStr.getBytes()), Lang.JSONLD);

        //Model meta = RDFDataMgr.loadModel("faceted-browsing-benchmark-v1-benchmark.ttl");
        
        Model model = ModelFactory.createDefaultModel();
        model.add(paramModel);
        //model.add(meta);
        
        String preconfig = model.listStatements(null, FacetedBrowsingVocab.preconfData, (RDFNode)null).nextOptional().map(Statement::getString).orElse("").trim(); 
        if(Strings.isNullOrEmpty(preconfig)) {
        	throw new RuntimeException("No dataset configured in config model");
        }
        
        // Try to open the resource now
        try(InputStream rawIn = HobbitBenchmarkUtils.openResource(preconfig)) { }
        
    	return () -> {
    		InputStream in;
    		try {
    			in = HobbitBenchmarkUtils.openResource(preconfig);
    		} catch(Exception e) {
    			throw new RuntimeException("Failed to open resource", e);
    		}
			Iterator<Triple> it = RDFDataMgr.createIteratorTriples(in, Lang.TURTLE, "http://www.example.org/");
    		Stream<Triple> r = Streams.stream(it)
    				.onClose(() -> {
    					// Consume the underlying iterator to trigger jena's
    					// closing mechanism
    					Iterators.size(it);
    				});
    		return r;
    		
    	};
    }
	
	
	
	
	
	
	
	
	
//    @Bean
//    public TripleStreamSupplier dataGenerationMethod(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory, @Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr) {
//    	
//
//    	return () -> {
//    		InputStream in;
//			try {
//				in = HobbitBenchmarkUtils.openBz2InputStream("hobbit-sensor-stream-150k-events-data.trig.bz2");//new MetaBZip2CompressorInputStream(MainFacetedBrowsingBenchmarkV2Run.class.getClassLoader().getResourceAsStream("hobbit-sensor-stream-150k-events-data.trig.bz2"));
//			} catch (IOException e) {
//				throw new  RuntimeException(e);
//			}
//
//			logger.info("WARNING: LIMIT ON DATASET SIZE IN PLACE");
//			
//			Iterator<Quad> it = RDFDataMgr.createIteratorQuads(in, Lang.TRIG, "http://www.example.org/");
//    		Stream<Triple> r = Streams.stream(it)
//    				//.limit(412747) for the 75K dataset
//    				.limit(103156)
//    				.map(Quad::asTriple)
//    				.onClose(() -> {
//    					// Consume the underlying iterator to trigger jena's
//    					// closing mechanism
//    					Iterators.size(it);
//    				});
//    		return r;
//    		
//    	};
    

}
