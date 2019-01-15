package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Stream;

import org.aksw.commons.util.compress.MetaBZip2CompressorInputStream;
import org.aksw.jena_sparql_api.core.utils.RDFDataMgrRx;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.main.MainFacetedBrowsingBenchmarkV2Run;
import org.hobbit.core.Constants;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderFactory;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import io.reactivex.Flowable;


public class ConfigDataGeneratorFacetedBrowsingV2 {
//    @Bean
//    public TripleStreamSupplier dataGenerationMethod(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory, @Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr) {
//    	return () -> Stream.of(new Triple(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property));
//    }

    @Bean
    public TripleStreamSupplier dataGenerationMethod(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory, @Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr) {
    	

    	return () -> {
    		InputStream in;
			try {
				in = new MetaBZip2CompressorInputStream(MainFacetedBrowsingBenchmarkV2Run.class.getClassLoader().getResourceAsStream("hobbit-sensor-stream-150k-events-data.trig.bz2"));
			} catch (IOException e) {
				throw new  RuntimeException(e);
			}
    		Iterator<Quad> it = RDFDataMgr.createIteratorQuads(in, Lang.TRIG, "http://www.example.org/");
    		Stream<Triple> r = Streams.stream(it)
    				.map(Quad::asTriple)
    				.onClose(() -> {
    					// Consume the underlying iterator to trigger jena's
    					// closing mechanism
    					Iterators.size(it);
    				});
    		return r;
    		
    	};
    	
//		Flowable<Dataset> flow = RDFDataMgrRx.createFlowableDatasets(
//				() -> new MetaBZip2CompressorInputStream(MainFacetedBrowsingBenchmarkV2Run.class.getClassLoader().getResourceAsStream("hobbit-sensor-stream-150k-events-data.trig.bz2")),
////				() -> new FileInputStream("/home/raven/Projects/Data/Hobbit/hobbit-sensor-stream-150k.trig"),
//				Lang.TRIG,
//				"http://www.example.org/");
//
//		int initSample = 1000;
//		//flow.onBackpressureBuffer().blockingNext();
//		//flow.forEach(x -> System.out.println("Next: " + x));
//		flow.forEach(batch -> {
////		flow.limit(initSample).forEach(batch -> {
//			
//			// Its probably more efficient (not scientifially evaluated)
//			// to create an indexed copy 
//			Dataset tmp = DatasetFactory.create();						
//			DatasetGraphUtils.addAll(tmp.asDatasetGraph(), batch.asDatasetGraph());
//			
//			Model m = tmp.getUnionModel();
//		});
//
//    	
//    	return () -> Stream.of(new Triple(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property));
    }

}
