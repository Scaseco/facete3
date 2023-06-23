package org.hobbit.benchmark.faceted_browsing.v2;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestFacetedQueryDeterminism {
	
	protected FacetedQuery fq;
	
	@Before
	public void beforeTest() {
		Model model = RDFDataMgr.loadModel("path-data.ttl");
		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(model));		

		Model dataModel = ModelFactory.createDefaultModel();
		XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
		FacetedQueryImpl.initResource(facetedQuery);
		
		fq = FacetedQueryImpl.create(facetedQuery, conn);

		//FacetedQueryResource fq = FacetedQueryImpl.create(model, conn);
	}

	/**
	 * Test to ensure that constraints on the same path are combined using OR (rather than AND)
	 * 
	 */
	@Test
	public void testDeterminism() {
		fq.root().fwd(RDF.type).one().enterConstraints().eq(OWL.Class);
		fq.root().fwd(RDF.type).one().enterConstraints().eq(RDFS.Class);
		
		List<?> before = null;
		for(int i = 0; i < 10; ++i) {
			Random random = new Random(0);
			List<?> now = fq.root().availableValues().randomOrder().pseudoRandom(random).exec().toList().blockingGet();
			if(before == null) {
				before = now;
			} else {
				Assert.assertEquals(now, before);
			}
		}
		
	}
	
	
}

