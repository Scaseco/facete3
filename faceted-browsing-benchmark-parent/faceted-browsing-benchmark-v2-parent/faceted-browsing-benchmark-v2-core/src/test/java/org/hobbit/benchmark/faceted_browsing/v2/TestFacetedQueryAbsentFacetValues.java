package org.hobbit.benchmark.faceted_browsing.v2;

import java.util.List;

import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class TestFacetedQueryAbsentFacetValues {

	@Test
	public void testAbsentConstraintIssueDisappearingFacet() {
		FacetedQuery fq =
				FacetedQueryBuilder.builder()
					.configDataConnection().setSource(RDFDataMgr.loadModel(TestFacetedQuery2.DS_S_L_IN_G)).end()
				.create();

		fq.baseConcept(Concept.parse("?s | ?s a eg:Foobar", PrefixMapping.Extended));
		fq.focus().fwd(RDF.type).one().constraints().eqIri("http://www.example.org/City").activate();
		fq.focus().fwd(RDF.type).one().constraints().eqIri("http://www.example.org/Country").activate();
		fq.focus().fwd("http://www.example.org/contains").one().constraints().absent().activate();

		// We are expecting 2 facet values for the type property:
		// [Germany, Leipzig]

		// Due to the issue we get an empty result
		
		List<FacetValueCount> fvcs = fq.focus().fwd().facetValueCountsWithAbsent(true).only(RDF.type).exec().toList().blockingGet();
		System.out.println(fvcs);
		
		//System.out.println("Facet counts: " + fq.focus().fwd().facetCounts(true).only("http://www.example.org/contains").exec().toList().blockingGet());
		
	}

	
	// This issue was fixed, turn into a proper test case
	@Test
	public void testAbsentConstraintIssueSpottedInFacete3() {
		FacetedQuery fq =
				FacetedQueryBuilder.builder()
					.configDataConnection().setSource(RDFDataMgr.loadModel(TestFacetedQuery2.DS_S_L_IN_G)).end()
				.create();

		fq.focus().fwd(RDF.type).one().constraints().eqIri("http://www.example.org/City").activate();
		fq.focus().fwd(RDF.type).one().constraints().eqIri("http://www.example.org/Country").activate();
		//fq.focus().fwd("http://www.example.org/contains").one().constraints().absent().activate();

		// We are expecting 2 facet values for the contains property:
		// [null, Leipzig]
		
		// Reason for the issue: There are group graph patterns which break the optional
		// '{ OPTIONAL { foo }' } yields LEFT_JOIN(unit, foo)
		List<FacetValueCount> fvcs = fq.focus().fwd().facetValueCountsWithAbsent(true).only("http://www.example.org/contains").exec().toList().blockingGet();
		System.out.println(fvcs);
		
		System.out.println("Facet counts: " + fq.focus().fwd().facetCounts(true).only("http://www.example.org/contains").exec().toList().blockingGet());
		
	}
	
	@Test
	public void testAbsentConstraint() {
		FacetedQuery fq =
				FacetedQueryBuilder.builder()
					.configDataConnection().defaultModel().end()
				.create();

		fq.focus().fwd(RDF.type).one().constraints().eq(OWL.Class).activate();
		fq.focus().fwd(RDFS.label).one().constraints().eqStr("foo").activate();
		fq.focus().fwd(RDFS.label).one().constraints().exists().activate();
		fq.focus().fwd(RDFS.label).one().constraints().absent().activate();
		fq.focus().fwd(RDFS.label).one().fwd(RDFS.comment).one().constraints().eqStr("foo").activate();

		
		// TODO Absence constraints on the focus should be ignored, and it seems this is working
		// but more testing should be done
		//fq.focus().constraints().absent().activate();
		
		System.out.println(fq.focus().availableValues().toConstructQuery());
		
//		fq.focus().fwd(RDFS.label).one().constraints().absent().deactivate();
//
//		System.out.println(fq.focus().availableValues().toConstructQuery());
		
		
//		FacetDirNode facetDirNode = fq.root().fwd();
//		String str = "" + facetDirNode.facetValueCountsWithAbsent().toConstructQuery();
//		
//		System.out.println(str);
		
		
//		FacetNode s = root.fwd(RDF.type).one();
//		FacetNode y = s.bwd(RDF.type).one().constraints().exists().activate().end()	
	}

	@Test
	public void testAbsentFacetValues() {
		FacetedQuery fq =
				FacetedQueryBuilder.builder()
					.configDataConnection().defaultModel().end()
				.create();

//		fq.focus().fwd(RDF.type).one().constraints().eq(OWL.Class).activate();
//		fq.focus().fwd(RDFS.label).one().constraints().eqStr("foo").activate();
//		fq.focus().fwd(RDFS.label).one().constraints().exists().activate();
//		fq.focus().fwd(RDFS.label).one().constraints().absent().activate();
//		fq.focus().fwd(RDFS.label).one().fwd(RDFS.comment).one().constraints().eqStr("foo").activate();


		fq.focus().fwd(RDF.type).one().chFocus();
		
		
//		System.out.println(fq.focus().availableValues().toConstructQuery());
//
		FacetDirNode facetDirNode = fq.root().fwd();
		facetDirNode.via(RDFS.label).one().constraints().eqStr("test").activate();
		System.out.println("" + facetDirNode.facetValueCountsWithAbsent(true).toConstructQuery());
//		
//		System.out.println(str);
		
		
//		FacetNode s = root.fwd(RDF.type).one();
//		FacetNode y = s.bwd(RDF.type).one().constraints().exists().activate().end();
	}
}
