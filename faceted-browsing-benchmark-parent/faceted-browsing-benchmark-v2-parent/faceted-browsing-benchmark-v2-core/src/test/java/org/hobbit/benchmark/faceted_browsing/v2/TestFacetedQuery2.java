package org.hobbit.benchmark.faceted_browsing.v2;

import com.google.common.collect.Range;
import org.aksw.facete.v3.api.DataQuery;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetNodeImpl;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
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
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

	static String getQueryPattern(FacetNode node) {
		return ((FacetNodeImpl) node).createValueQuery(false).toConstructQuery().getValue().getQueryPattern().toString();
	}

	@Test
	public void testPathFinder() {
		final TaskGenerator taskGenerator = TaskGenerator.autoConfigure((RDFConnection) fq.connection());
		final ConceptPathFinder conceptPathFinder = taskGenerator.getConceptPathFinder();
		//new Concept()
		final Concept targetConcept = new Concept(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s);
		final PathSearch<SimplePath> pathSearch = conceptPathFinder.createSearch(fq.root().remainingValues().baseRelation().toUnaryRelation(), targetConcept);
		pathSearch.setMaxPathLength(10);
		final List<SimplePath> paths = pathSearch.exec().toList().blockingGet();

		final int[] i = {1};
		paths.forEach(path -> {
			System.out.println("Path " + i[0] + ": " + path.toPathString());
			i[0]++;
		});
		System.out.println(paths);
	}

	@Test
	public void testCp3() {
		final TaskGenerator taskGenerator = TaskGenerator.autoConfigure((RDFConnection) fq.connection());
		taskGenerator.setPseudoRandom(new Random(1234l));
		final FacetNode node = fq.root();

		assertEquals( "{ ?v_1  ?p  ?o }" ,
				getQueryPattern(node) );

		taskGenerator.applyCp3(node);

		assertNotEquals( "{ ?v_1  ?p  ?o }" , getQueryPattern(node) );
		System.out.println(getQueryPattern(node));

	}

	@Test
	public void testCp2() {
		final TaskGenerator taskGenerator = TaskGenerator.autoConfigure((RDFConnection) fq.connection());
		taskGenerator.setPseudoRandom(new Random(1234l));
		final FacetNode node = fq.root();

		assertEquals( "{ ?v_1  ?p  ?o }" ,
				getQueryPattern(node) );

		taskGenerator.applyCp2(node);

		assertEquals( "{ { ?v_1  <http://www.example.org/locatedIn>  ?v_2\n" +
				"    FILTER bound(?v_2)\n" +
				"  }\n" +
				"  ?v_1  ?p  ?o\n" +
				"}" , getQueryPattern(node) );

		taskGenerator.applyCp2(node);

		assertEquals( "{ { ?v_1  <http://www.example.org/population>  ?v_2 ;\n" +
				"          <http://www.example.org/locatedIn>  ?v_3\n" +
				"    FILTER bound(?v_3)\n" +
				"    FILTER bound(?v_2)\n" +
				"  }\n" +
				"  ?v_1  ?p  ?o\n" +
				"}" , getQueryPattern(node) );
	}

	@Test
	public void testCp1() {
		final TaskGenerator taskGenerator = TaskGenerator.autoConfigure((RDFConnection) fq.connection());
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


	@Test
	public void testRangeConstraint() {
		//final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd().facetCounts();
		//
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

	@Test
	public void testConstraints() {
		//final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd().facetCounts();
		//
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

	@Test
	public void testFacetCounts() {
		//final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd().facetCounts();
		//
		final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd("http://www.example.org/contains").one().fwd().facetCounts();
		final List<FacetCount> facetCounts = facetCountDataQuery.only("http://www.example.org/population").exec().toList().blockingGet();

		assertEquals( 1 , facetCounts.size() );
		assertEquals( 1 , facetCounts.get(0).getDistinctValueCount().getCount() );
	}
	
}
