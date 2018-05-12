package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import com.google.common.collect.EvictingQueue;

import io.reactivex.processors.PublishProcessor;



class TimeWindowUpdateHandler {
	protected EvictingQueue<Collection<Quad>> evictingQueue;
	
	public TimeWindowUpdateHandler(int maxSize) {
		this(EvictingQueue.create(maxSize));
	}
	
	public TimeWindowUpdateHandler(EvictingQueue<Collection<Quad>> evictingQueue) {
		this.evictingQueue = evictingQueue;
	}
	
	public synchronized UpdateRequest createUpdateRequest(Collection<Quad> insertQuads) {
		int remainingCapacity = evictingQueue.remainingCapacity();

		Collection<Quad> removalQuads = remainingCapacity == 0
				? evictingQueue.remove()
				: Collections.emptyList();

		evictingQueue.add(insertQuads);
		UpdateRequest result = UpdateRequestUtils.createUpdateRequest(insertQuads, removalQuads);
		return result;
	}
}

public class MainFacetedBrowsingBenchmarkV2 {
	public static void main(String[] args) {
		PublishProcessor<Collection<Quad>> graphWindowProcessor = PublishProcessor.create();
		
		
		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create());
		
		TimeWindowUpdateHandler insertHandler = new TimeWindowUpdateHandler(3);
		graphWindowProcessor
			//.map(SetDatasetGraph::new)
			.map(insertHandler::createUpdateRequest)
			.forEach(conn::update);
		
		for(int i = 0; i < 10; ++i) {
			Node s = NodeFactory.createURI("http://example.org/observation-" + i);
			Collection<Quad> insertQuads = Arrays.asList(
				new Quad(Quad.defaultGraphIRI, s, RDF.type.asNode(), OWL.Thing.asNode()));
			
			
			graphWindowProcessor.onNext(insertQuads);
		}
		
		Model model = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
	}
}
