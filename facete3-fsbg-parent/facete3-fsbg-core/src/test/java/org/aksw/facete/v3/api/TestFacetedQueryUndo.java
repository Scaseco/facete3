package org.aksw.facete.v3.api;

import java.util.List;

import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.changeset.util.ChangeSetGroupManager;
import org.aksw.jena_sparql_api.changeset.util.ChangeSetUtils;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestFacetedQueryUndo {
	@Test
	public void testFacetedQueryUndo() {
		Model changeModel = ModelFactory.createDefaultModel();
		
		Model dataModel = ModelFactory.createDefaultModel();
		
		Model m = RDFDataMgr.loadModel("path-data.ttl");
		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(m));		

		
		Resource fqState = dataModel.createResource();

		// Init
		FacetedQueryImpl.initResource(fqState);

		ChangeSetGroupManager csgm = new ChangeSetGroupManager(changeModel, dataModel);
		
		ChangeSetUtils.trackChangesInTxn(changeModel, dataModel, model -> {
			FacetedQuery fq = FacetedQueryImpl.create(fqState.inModel(model), conn);
			fq.root().fwd(RDF.type).one().constraints().eq(OWL.Class.asNode());
			
			
			List<?> facets = fq.focus().fwd().facets().exec().toList().blockingGet();
			System.out.println("Facets: " + facets);
		});

		FacetedQuery fq = FacetedQueryImpl.create(fqState, conn);
		List<?> facets = fq.focus().fwd().facets().exec().toList().blockingGet();
		System.out.println("Facets: " + facets);
		
		csgm.undo();
		
		facets = fq.focus().fwd().facets().exec().toList().blockingGet();
		System.out.println("Facets: " + facets);
		
	}
}
