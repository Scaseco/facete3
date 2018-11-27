package org.hobbit.benchmark.faceted_browsing.v2;

import com.google.common.collect.Range;
import org.aksw.facete.v3.api.*;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetNodeImpl;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.changeset.util.RdfChangeTrackerWrapper;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.NodeHolder;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.RdfChangeTrackerWrapperImpl;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestFacetedQuery2 {

	//protected FacetedQuery fq;
	final String DS_SIMPLE = "path-data-simple.ttl";
	final String DS_SIMPLE_1 = "path-data-simple-1.ttl";
	final String DS_SIMPLE_2 = "path-data-simple-2.ttl";
	final String DS_SIMPLE_3 = "path-data-simple-3.ttl";
	final String DS_SIMPLE_4 = "path-data-simple-4.ttl";

	protected RdfChangeTrackerWrapper changeTracker;
	protected FacetedQuery fq;
	private TaskGenerator taskGenerator;

	@Before
	public void beforeTest() {
		fq = null;
		changeTracker = null;
		taskGenerator = null;
	}

	protected void load(String uri) {
 		Model baseModel = ModelFactory.createDefaultModel();
		Model changeModel = ModelFactory.createDefaultModel();
		//RdfChangeTrackerWrapper
		changeTracker = RdfChangeTrackerWrapperImpl.create(changeModel, baseModel);
		Model dataModel = changeTracker.getDataModel();

		Model model = RDFDataMgr.loadModel(uri);
		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(model));

		// RDF Resource with state
		XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
		FacetedQueryImpl.initResource(facetedQuery);

		fq = new FacetedQueryImpl(facetedQuery, null, conn);

		changeTracker.commitChangesWithoutTracking();

		taskGenerator = TaskGenerator.autoConfigure((RDFConnection) fq.connection());
	}

	static String getQueryPattern(FacetNode node) {
		return ((FacetNodeImpl) node).createValueQuery(false).toConstructQuery().getValue().getQueryPattern().toString();
	}

	@Test
	public void testFocusNode() {
		// TODO: focus tests
		load(DS_SIMPLE_3);

		final FacetNode one = fq.root().bwd("http://xmlns.com/foaf/0.1/based_near").one();
		//final FacetNode one = fq.root().fwd("http://www.example.org/inhabitants").one();
		fq.focus(one);

		fq.root().fwd(RDF.type).one().constraints().eqIri("http://www.example.org/City");

		//final List<FacetValueCount> facetValueCounts = fq.root().fwd().facetValueCounts().only(RDFS.label).exec().toList().blockingGet();
		final List<FacetValueCount> facetValueCounts = fq.root().fwd().facetValueCounts().only("http://www.example.org/inhabitants").exec().toList().blockingGet();
		System.out.println(facetValueCounts);
	}

	@Test
	public void testPathFinder() {
		load(DS_SIMPLE_1);
		final ConceptPathFinder conceptPathFinder = taskGenerator.getConceptPathFinder();
		//new Concept()
		final Concept targetConcept = new Concept(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s);
		final PathSearch<SimplePath> pathSearch = conceptPathFinder.createSearch(fq.root().remainingValues().baseRelation().toUnaryRelation(), targetConcept);

		pathSearch.setMaxPathLength(3);
		final List<SimplePath> paths = pathSearch.exec().filter(x -> x.getSteps().stream().noneMatch(p ->
			!p.isForward()
		) ).toList().blockingGet();

		final int[] i = {1};
		paths.forEach(path -> {
			System.out.println("Path " + i[0] + ": " + path.toPathString());
			i[0]++;
		});
		final String[] result = {
				"",
				"<http://www.example.org/contains>",
				"<http://www.example.org/locatedIn>",
				"<http://www.example.org/mayor>",
				"<http://xmlns.com/foaf/0.1/based_near>",
				"<http://www.example.org/contains> <http://www.example.org/locatedIn>",
				"<http://www.example.org/contains> <http://www.example.org/mayor>",
				"<http://www.example.org/locatedIn> <http://www.example.org/contains>",
				"<http://www.example.org/mayor> <http://xmlns.com/foaf/0.1/based_near>",
				"<http://xmlns.com/foaf/0.1/based_near> <http://www.example.org/locatedIn>",
				"<http://xmlns.com/foaf/0.1/based_near> <http://www.example.org/mayor>",
		};
		assertArrayEquals( result , paths.stream().map(SimplePath::toPathString).toArray() );
		//System.out.println(paths);
	}

	@Test
	public void testCp14() {
		load(DS_SIMPLE_2);
		taskGenerator.setPseudoRandom(new Random(1234l));
		final FacetNode node = fq.root();

		assertEquals( "{ ?v_1  ?p  ?o }" ,
				getQueryPattern(node) );

		taskGenerator.setRandom(new Random(1234l));
		final boolean b = taskGenerator.applyCp14(node);
		assertEquals( "{ ?v_1  <http://xmlns.com/foaf/0.1/based_near>  ?v_2 .\n" +
				"  ?v_3  <http://xmlns.com/foaf/0.1/based_near>  ?v_2 ;\n" +
				"        <http://xmlns.com/foaf/0.1/age>  10\n" +
				"  { ?v_1  ?p  ?o }\n" +
				"}" , getQueryPattern(node) );

		changeTracker.commitChanges();

		long i;
		final String[] solutions = {
				"{ ?v_1  <http://xmlns.com/foaf/0.1/based_near>  ?v_2 .\n" +
						"  ?v_3  <http://xmlns.com/foaf/0.1/based_near>  ?v_2 ;\n" +
						"        <http://xmlns.com/foaf/0.1/age>  10 .\n" +
						"  ?v_5  <http://www.example.org/mayor>  ?v_1 ;\n" +
						"        <http://www.example.org/population>  500000\n" +
						"  { ?v_1  ?p  ?o }\n" +
						"}",
		};
		taskGenerator.setRandom(new Random(6399312698163894396L));
		final boolean c = taskGenerator.applyCp14(node);
		final String qp = getQueryPattern(node);
		final boolean ok = Arrays.stream(solutions).anyMatch(s -> s.equals(qp));
		assertEquals( ok ? qp : ""  , qp );
		changeTracker.discardChanges();
	}

	@Test//done
	public void testCp4() {
		load(DS_SIMPLE_2);


		final FacetNode node = fq.root();

		assertEquals( "{ ?v_1  ?p  ?o }" ,
				getQueryPattern(node) );

		changeTracker.commitChanges();
		final String[] solutions = {
				"{ { ?v_1  <http://www.w3.org/2000/01/rdf-schema#label>  ?v_2 ;\n" +
						"          a                     <http://xmlns.com/foaf/0.1/Person>\n" +
						"    FILTER bound(?v_2)\n" +
						"  }\n" +
						"  ?v_1  ?p  ?o\n" +
						"}",

				"{ { ?v_1  <http://xmlns.com/foaf/0.1/based_near>  ?v_2 ;\n" +
						"          a                     <http://xmlns.com/foaf/0.1/Person>\n" +
						"    FILTER bound(?v_2)\n" +
						"  }\n" +
						"  ?v_1  ?p  ?o\n" +
						"}",
		};
		long i;
		for (i = 0L; i < 2L; i++) {
			System.out.println(i);
			//taskGenerator.setRandom(new Random(i));
			//taskGenerator.setPseudoRandom(new Random(~i));
			taskGenerator.applyCp4(node);
			final String qp = getQueryPattern(node);
			final boolean ok = Arrays.stream(solutions).anyMatch(s -> s.equals(qp));
			assertEquals( ok ? qp : ""  , qp );
			changeTracker.discardChanges();
		}
		//assertEquals("" , getQueryPattern(node) );
	}

	@Test//done
	public void testCp3() {
		load(DS_SIMPLE_1);
		taskGenerator.setPseudoRandom(new Random(1234l));
		final FacetNode node = fq.root();

		assertEquals( "{ ?v_1  ?p  ?o }" ,
				getQueryPattern(node) );

		changeTracker.commitChanges();

		taskGenerator.applyCp3(node);

		assertEquals( "{ ?v_1  <http://xmlns.com/foaf/0.1/based_near>  ?v_2 .\n" +
				"  ?v_2  <http://www.example.org/locatedIn>  <http://www.example.org/Germany>\n" +
				"  { ?v_1  ?p  ?o }\n" +
				"}" , getQueryPattern(node) );
		System.out.println(getQueryPattern(node));

		changeTracker.discardChanges();

		taskGenerator.getRandom().nextLong();
		taskGenerator.getRandom().nextLong();
		taskGenerator.getRandom().nextLong();

		taskGenerator.applyCp3(node);

		assertEquals( "{ ?v_1  <http://www.example.org/mayor>  ?v_2 .\n" +
				"  ?v_2  <http://xmlns.com/foaf/0.1/based_near>  <http://www.example.org/Leipzig>\n" +
				"  { ?v_1  ?p  ?o }\n" +
				"}" , getQueryPattern(node) );
		//System.out.println(getQueryPattern(node));

		changeTracker.discardChanges();
	}

	@Test//done
	public void testCp2() {
		load(DS_SIMPLE);
		taskGenerator.setPseudoRandom(new Random(1234l));
		final FacetNode node = fq.root();

		assertEquals( "{ ?v_1  ?p  ?o }" ,
				getQueryPattern(node) );
		changeTracker.commitChanges();

		taskGenerator.applyCp2(node);

		assertEquals( "{ { ?v_1  <http://www.example.org/contains>  ?v_2\n" +
				"    FILTER bound(?v_2)\n" +
				"  }\n" +
				"  ?v_1  ?p  ?o\n" +
				"}" , getQueryPattern(node) );

		taskGenerator.applyCp2(node);

		assertEquals( "{ { ?v_1  <http://www.example.org/contains>  ?v_2 .\n" +
				"    ?v_2  <http://www.example.org/locatedIn>  ?v_3\n" +
				"    FILTER bound(?v_3)\n" +
				"    FILTER bound(?v_2)\n" +
				"  }\n" +
				"  ?v_1  ?p  ?o\n" +
				"}" , getQueryPattern(node) );

		changeTracker.discardChanges();

		taskGenerator.getRandom().nextLong();
		taskGenerator.getRandom().nextLong();

		taskGenerator.applyCp2(node);
		final String queryPattern = getQueryPattern(node);
		assertEquals("{ { ?v_1  <http://www.example.org/locatedIn>  ?v_2 .\n" +
				"    ?v_2  <http://www.example.org/contains>  ?v_3\n" +
				"    FILTER bound(?v_3)\n" +
				"  }\n" +
				"  ?v_1  ?p  ?o\n" +
				"}", queryPattern);

	}

	@Test//done
	public void testCp1() {
		load(DS_SIMPLE);
		taskGenerator.setPseudoRandom(new Random(1234l));
		final FacetNode node = fq.root();
		final Query v1 = ((FacetNodeImpl) node).createValueQuery(false).toConstructQuery().getValue();
		assertEquals( "{ ?v_1  ?p  ?o }" , v1.getQueryPattern().toString() );

		//System.out.println("---");
		taskGenerator.applyCp1(node);

		assertEquals( "{ ?v_1  <http://www.example.org/population>  500000\n" +
						"  { ?v_1  ?p  ?o }\n" +
						"}" ,
				getQueryPattern(node)
		);

		//System.out.println("---");
		taskGenerator.applyCp1(node);
		assertEquals( "{ ?v_1  <http://www.w3.org/2000/01/rdf-schema#label>  \"Leipzig\" ;\n" +
						"        <http://www.example.org/population>  500000\n" +
						"  { ?v_1  ?p  ?o }\n" +
						"}" ,
				getQueryPattern(node)
		);

	}


	@Test//done
	public void testRangeConstraint() {
		//final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd().facetCounts();
		//
		load(DS_SIMPLE);
		final FacetNode node = fq.root()
				.fwd("http://www.example.org/population")
				.one()
				.constraints()
//				.gt(NodeValue.makeInteger(50000).asNode())
				    .range(Range.closed(
						new NodeHolder(NodeValue.makeInteger(50000).asNode()),
						new NodeHolder(NodeValue.makeInteger(80000000).asNode())))
				.end()
				.parent()
				;

		assertEquals( "{ { ?v_1  <http://www.example.org/population>  ?v_2\n" +
						"    FILTER ( ?v_2 <= 80000000 )\n" +
						"    FILTER ( ?v_2 >= 50000 )\n" +
						"  }\n" +
						"  ?v_1  ?p  ?o\n" +
						"}" ,
				getQueryPattern(node) );
	}

	@Test//done
	public void testConstraints() {
		//final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd().facetCounts();
		//
		load(DS_SIMPLE);
		final DataQuery<FacetCount> facetCountDataQuery = fq.root()
				.constraints()
				    .eqIri("http://www.example.org/Leipzig")
				    .eqIri("http://www.example.org/Germany")
				.end()

				//.fwd("http://www.example.org/contains")
				//.one()
				.fwd().facetCounts();
		final List<FacetCount> facetCounts = facetCountDataQuery.only("http://www.example.org/population").exec().toList().blockingGet();

		assertEquals( 2 , facetCounts.get(0).getDistinctValueCount().getCount() );
	}

	@Test//done
	public void testFacetCounts() {
		//final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd().facetCounts();
		//
		{
			load(DS_SIMPLE);
			final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd("http://www.example.org/contains").one().fwd().facetCounts();
			final List<FacetCount> facetCounts = facetCountDataQuery.only("http://www.example.org/population").exec().toList().blockingGet();

			assertEquals(1, facetCounts.size());
			assertEquals(1, facetCounts.get(0).getDistinctValueCount().getCount());
		}

	}
	
}
