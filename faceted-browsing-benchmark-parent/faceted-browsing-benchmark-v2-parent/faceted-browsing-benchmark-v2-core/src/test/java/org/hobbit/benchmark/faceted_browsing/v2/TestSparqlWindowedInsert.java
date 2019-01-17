package org.hobbit.benchmark.faceted_browsing.v2;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.main.SimpleSparqlInsertRequestFactory;
import org.hobbit.benchmark.faceted_browsing.v2.main.SimpleSparqlInsertRequestFactoryWindowedInMemory;
import org.junit.Assert;
import org.junit.Test;

import io.reactivex.processors.PublishProcessor;



public class TestSparqlWindowedInsert {
	
	@Test
	public void testSparqlWindowedInsert() {
		// Set up a connection to a triple store
		// (for the sake of the demo this is in memory, but the factory also supports remote sparql access)
		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create());

		// Set up a flow that transform insert requests of a collection of quads into
		// corresponding update requests
		PublishProcessor<Collection<Quad>> quadsInserter = PublishProcessor.create();

		// ... thereby remove old records once the data grows too large
		int expectedModelSize = 3;
		SimpleSparqlInsertRequestFactory insertHandler = new SimpleSparqlInsertRequestFactoryWindowedInMemory(expectedModelSize);

		quadsInserter
			//.map(SetDatasetGraph::new)
			.map(insertHandler::createUpdateRequest)
			.forEach(conn::update);
		
		
		// Generate some example data and put it into the pipeline
		for(int i = 0; i < 10; ++i) {
			Node s = NodeFactory.createURI("http://example.org/observation-" + i);
			Collection<Quad> insertQuads = Arrays.asList(
				new Quad(Quad.defaultGraphIRI, s, RDF.type.asNode(), OWL.Thing.asNode()));
			
			
			quadsInserter.onNext(insertQuads);
		}
		
		
		// Fetch the data we have generated
		Model model = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
		
		// Output the data for convenience
		RDFDataMgr.write(System.err, model, RDFFormat.TURTLE_PRETTY);

		int actualModelSize = (int)model.size(); 
		Assert.assertEquals(expectedModelSize, actualModelSize);
	}
}
