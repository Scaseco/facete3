package org.hobbit.benchmark.faceted_browsing.v2;

import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetNodeResource;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests whether the toString() methods return the expected results
 * 
 * @author Claus Stadler, Jan 12, 2019
 *
 */
public class TestFacetedQueryToString {
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
	public void testToStringConstraintDisjunction() {
		fq.root().fwd(RDF.type).one().constraints().eq(OWL.Class);
		fq.root().fwd(RDF.type).one().constraints().eq(RDFS.Class);

		// FacetNode
		String facetNodeStr = "" + fq.root().fwd(RDF.type).one().fwd(RDFS.label).one();
		System.out.println("FacetNodeStr: " + facetNodeStr);
		
		// FacetNode state (BgpNode)
		String bgpNodeStr = "" + fq.root().fwd(RDF.type).one().fwd(RDFS.label).one().as(FacetNodeResource.class).state();
		System.out.println("BgpNodeStr: " + bgpNodeStr);
		

		// HLConstraint
		String constraintStr = "" + fq.root().fwd(RDF.type).one().constraints().eq(OWL.Class);
		System.out.println("ConstraintStr: " + constraintStr);

		// Constraint
		String constraintNodeStr = "" + fq.root().fwd(RDF.type).one().constraints().eq(OWL.Class).state();
		System.out.println("ConstraintNodeStr: " + constraintNodeStr);

		//System.out.println("Available values: " + fq.root().availableValues().exec().toList().blockingGet());
		
	}

}
