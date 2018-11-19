package org.hobbit.benchmark.faceted_browsing.v2;

import org.aksw.facete.v3.api.DataQuery;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

public class TestFacetedQuery2 {
	
	protected FacetedQuery fq;
	
	@Before
	public void beforeTest() {
		Model model = RDFDataMgr.loadModel("path-data-simple.ttl");
		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(model));		

		Model dataModel = ModelFactory.createDefaultModel();
		XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
		FacetedQueryImpl.initResource(facetedQuery);
		
		fq = new FacetedQueryImpl(facetedQuery, null, conn);

		//FacetedQueryResource fq = FacetedQueryImpl.create(model, conn);
	}

	@Test
	public void testFacetCounts() {
		//final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd().facetCounts();
		//
		final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd("http://www.example.org/contains").one().fwd().facetCounts();
		System.out.println(facetCountDataQuery.only("http://www.example.org/population").exec().toList().blockingGet());
	}
	
}
