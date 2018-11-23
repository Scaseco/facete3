package org.hobbit.benchmark.faceted_browsing.v2;

import com.google.common.collect.Range;
import org.aksw.facete.v3.api.DataQuery;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetNodeImpl;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.utils.NodeHolder;
import org.apache.jena.graph.Node;
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
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

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
	public void testCp1() {
		final TaskGenerator taskGenerator = new TaskGenerator(null, null);
		taskGenerator.setPseudoRandom(new Random(1234l));
		final FacetNode node = fq.root();
		final Query v1 = ((FacetNodeImpl) node).createValueQuery(false).toConstructQuery().getValue();
		assertEquals( "{ ?v_1  ?p  ?o }" , v1.getQueryPattern().toString() );

		//System.out.println("---");
		taskGenerator.applyCp1(node);

		assertEquals( "{ ?v_1  <http://www.example.org/population>  500000\n" +
						"  { ?v_1  ?p  ?o }\n" +
						"}" ,
				((FacetNodeImpl) node).createValueQuery(false).toConstructQuery().getValue().getQueryPattern().toString()
		);

		//System.out.println("---");
		taskGenerator.applyCp1(node);
		assertEquals( "{ ?v_1  <http://www.w3.org/2000/01/rdf-schema#label>  \"Leipzig\" ;\n" +
						"        <http://www.example.org/population>  500000\n" +
						"  { ?v_1  ?p  ?o }\n" +
						"}" ,
				((FacetNodeImpl) node).createValueQuery(false).toConstructQuery().getValue().getQueryPattern().toString()
		);

	}


	@Test
	public void testRangeConstraint() {
		//final DataQuery<FacetCount> facetCountDataQuery = fq.root().fwd().facetCounts();
		//
		final FacetNode facetCountDataQuery = fq.root()
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

		final DataQuery<?> valueQuery = ((FacetNodeImpl) facetCountDataQuery).createValueQuery(false);
		final Map.Entry<Node, Query> x = valueQuery.toConstructQuery();

		assertEquals( "SELECT DISTINCT  ?v_1\n" +
				"WHERE\n" +
				"  { { ?v_1  <http://www.example.org/population>  ?v_2\n" +
				"      FILTER ( ?v_2 <= 80000000 )\n" +
				"      FILTER ( ?v_2 >= 50000 )\n" +
				"    }\n" +
				"    ?v_1  ?p  ?o\n" +
				"  }\n" ,
				x.getValue().toString() );
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
