package org.hobbit.benchmark.faceted_browsing.v2;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.api.ResolverDirNode;
import org.aksw.jena_sparql_api.data_query.impl.NodePathletPath;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;
import org.junit.Before;
import org.junit.Test;

public class TestFacetedQuery {

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

    @Test
    public void testNaming() {
        Model model = RDFDataMgr.loadModel("path-data-simple.ttl");
        RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(model));

        Model dataModel = ModelFactory.createDefaultModel();
        XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
        FacetedQueryImpl.initResource(facetedQuery);

        fq = FacetedQueryImpl.create(facetedQuery, conn);

        List<FacetCount> facetCounts = fq
                .root().fwd(RDF.type).one().constraints().eqIri("http://www.example.org/City").activate().end()
                .root().fwd()
                    .facetCounts().exclude(RDF.type).exec().toList().blockingGet();

        System.out.println("Available values: " + facetCounts);



    }

    /**
     * Test to ensure that constraints on the same path are combined using OR (rather than AND)
     *
     */
    @Test
    public void testConstraintDisjunction() {
        fq.root().fwd(RDF.type).one().constraints().eq(OWL.Class).activate();
        fq.root().fwd(RDF.type).one().constraints().eq(RDFS.Class).activate();

        System.out.println("Available values: " + fq.root().availableValues().exec().toList().blockingGet());

    }

    @Test
    public void testSimpleQuery2() {
        Query q = fq.root().fwd(RDF.type).one().availableValues().toConstructQuery().getValue();

        System.out.println("Query: " + q);

    }


    @Test
    public void testHeteroDimensionalConstraints() {
        fq.root().fwd(RDF.type).one().constraints().eq(OWL.Class);
        System.out.println("Available values: " + fq.root().availableValues().exec().toList().blockingGet());

        fq.root().fwd(RDFS.label).one().constraints().eqStr("ThingA");
        System.out.println("Label Available values: " + fq.root().fwd(RDFS.label).one().availableValues().exec().toList().blockingGet());
    }

    @Test
    public void testComplexQuery() {
        System.out.println("Complex Query");
        FacetValueCount fc =
                // -- Faceted Browsing API
                fq.root()
                .fwd(RDF.type).one()
                    .constraints()
//					.range(Range.atLeast(0)).activate()
                        .eq(OWL.Class).activate()
                    .end()
                .parent()
                .fwd()
                .facetValueCounts()
                // --- DataQuery API
                //.sample()
                .randomOrder()
                .limit(1)
                .exec()
                // --- RxJava API
                .firstElement()
                .timeout(10, TimeUnit.SECONDS)
                .blockingGet();
        System.out.println("FacetValueCount: " + fc);

    }

    @Test
    public void testSimpleQuery() {
        System.out.println("Simple Query");
        RDFNode fc =
                // -- Faceted Browsing API
                fq.focus().availableValues()
                // --- DataQuery API
                .sample().randomOrder()
                .limit(1).exec()
                // --- RxJava API
                .firstElement()
                .timeout(10, TimeUnit.SECONDS)
                .blockingGet();
        System.out.println("FacetValueCount: " + fc);

    }

    @Test
    public void testNegatedFacetValues() {
        fq.root().fwd(RDF.type).one().constraints().eq(OWL.Class);
        System.out.println("Available values: " + fq.root().availableValues().exec().toList().blockingGet());

        fq.root().fwd(RDFS.label).one().constraints().eqStr("ThingA");
        System.out.println("Label Available values: " + fq.root().fwd(RDFS.label).one().availableValues().exec().toList().blockingGet());


        // Listing the nonConstrainedValues must not include
        // 'rdf:type owl:Class' and
        // 'rdfs:label

        List<FacetValueCount> fvcs = fq.root().fwd().nonConstrainedFacetValueCounts().exec().toList().blockingGet();
        System.out.println("Non constrained values: " + fvcs);


    }


    @Test
    public void testFacetValueApi() {

        fq.focus().fwd(RDF.type).one().constraints().eq(OWL.Class).activate();

        System.out.println("Got values: " + fq.focus().availableValues().exec().toList().blockingGet());
        System.out.println("Facet values of these values: " + fq.focus().fwd().facetValueCounts().exec().toList().blockingGet());

        //FacetedQuery fq2 =
        DataQuery<?> dq = fq.root().fwd()
//		.facetValues()
//			.withCounts()
//			.withAbsent()
//			.itemsAs(Resource.class)
        .facetValueCounts();

        ResolverDirNode rdn = dq.resolver().fwd(Vocab.predicate).one().fwd();
        List<?> x = rdn.toFacetedQuery().focus().fwd().facetCounts().exec()
                .toList().blockingGet();

        //rdn.via(Vocab.facetValueCount).one().getPaths();
        //dq.resolver().fwd().getContrib()

        System.out.println(x.size() + " results");
        for(Object item : x) {
            System.out.println("Item: " + item);

        }


        dq.addOrderBy(NodePathletPath.create(Path.newPath().optional().fwd("http://www.example.org/facetCount")), Query.ORDER_ASCENDING);
        System.out.println(dq.toConstructQuery());

//
//		DataQuery<? extends RDFNode> fv = fq2.focus().fwd().facets().query2();
//
//		Node pick = null; // Pick the facetCount property
//
//		fv.orderBy(pick); // In place operation
//
//		fv.exec();







        System.out.println("Label Available values: " + fq.root().fwd(RDFS.label).one().availableValues().exec().toList().blockingGet());


        // Listing the nonConstrainedValues must not include
        // 'rdf:type owl:Class' and
        // 'rdfs:label

        List<FacetValueCount> fvcs = fq.root().fwd().nonConstrainedFacetValueCounts().exec().toList().blockingGet();
        System.out.println("Non constrained values: " + fvcs);


    }

}


// Maybe we can have an intermediate object with domain specific filtering options
// in a fashion like this:
//class FacetValueCountQuery
//	extends DataQuery<FacetValueCount>
//{
//	constraintPredicates()
//}
//


