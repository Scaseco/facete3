package org.hobbit.benchmark.faceted_browsing.v2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.aksw.facete.v3.api.*;
import org.aksw.facete.v3.impl.DataQueryImpl;
import org.aksw.facete.v3.impl.FacetNodeImpl;
import org.aksw.jena_sparql_api.changeset.util.RdfChangeTrackerWrapper;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.NodeHolder;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.HierarchyCoreOnDemand;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

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
		Model model = RDFDataMgr.loadModel(uri);
		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(model));

		taskGenerator = TaskGenerator.autoConfigure(conn);
		changeTracker = taskGenerator.getChangeTracker();
		fq = taskGenerator.getCurrentQuery();
		changeTracker.commitChangesWithoutTracking();
	}

	static String getQueryPattern(FacetNode node) {
		return ((FacetNodeImpl) node).createValueQuery(false).toConstructQuery().getValue().getQueryPattern().toString();
	}

	
	@Test
	public void testHierarchy() {
		load(DS_SIMPLE_3);

		Path path = new P_Link(RDFS.subClassOf.asNode());

		UnaryRelation classes = fq.root().fwd(RDF.type).one().availableValues().baseRelation().toUnaryRelation();
		UnaryRelation subClasses = HierarchyCoreOnDemand.createConceptForDirectlyRelatedItems(
				classes,
				path);
		
		DataQuery<Resource> dq = new DataQueryImpl<>(fq.connection(), subClasses, null, Resource.class);
		System.out.println("Subclasses: " + dq.exec().toList().blockingGet());
		

		UnaryRelation subClasses2 = HierarchyCoreOnDemand.createConceptForDirectlyRelatedItems(
				Concept.parse("?s | VALUES(?s) { (eg:Foobar) }", PrefixMapping.Extended),
				path);
		
		System.out.println(subClasses);
	}
	
	@Test//done
	public void testFocusNode() {
		// TODO: test case with films,characters,actors
		load(DS_SIMPLE_3);

		final FacetNode one = fq.root().bwd("http://xmlns.com/foaf/0.1/based_near").one();
		//final FacetNode one = fq.root().fwd("http://www.example.org/inhabitants").one();
		fq.focus(one);

		fq.root().fwd(RDF.type).one().constraints().eqIri("http://www.example.org/City");
		//final List<FacetValueCount> facetValueCounts = fq.root().fwd().facetValueCounts().only(RDFS.label).exec().toList().blockingGet();
		final Map<Node, Long> facetValueCounts = fq.root().fwd().facetValueCounts().only("http://www.example.org/inhabitants")
				.exec()
				.toMap(xk -> xk.getValue(), xv -> xv.getFocusCount().getCount(), LinkedHashMap::new)
				.blockingGet();

		final Map<Node, Long> solution = ImmutableMap.<Node, Long>builder()
				.put(ResourceFactory.createResource("http://www.example.org/LorenzStadler").asNode(), 3L)
				.put(ResourceFactory.createResource("http://www.example.org/BurkhardJung").asNode(),  3L)
				.put(ResourceFactory.createResource("http://www.example.org/MarieSchmidt").asNode(),  3L)
				.put(ResourceFactory.createResource("http://www.example.org/DirkHilbert").asNode(),   1L)
				.build();

		assertArrayEquals(((ImmutableMap<Node, Long>) solution).asMultimap().entries().toArray(), facetValueCounts.entrySet().toArray());
	}

	@Test
	public void testFacetConstraintAccess() {
		load(DS_SIMPLE_3);
		fq.root();
		fq.focus().fwd(RDF.type).one().constraints().eqIri("http://www.example.org/City");
		
		System.out.println("FROM CONSTRAINT: " + fq.root().fwd().facetValueCounts().only(RDF.type).exec().toList().blockingGet());

		for(HLFacetConstraint fc : fq.focus().fwd(RDF.type).one().constraints().listHl()) {
			Set<FacetNode> fns = fc.mentionedFacetNodes();
			System.out.println("GOT MENTIONED: " + fns.size());
			for(FacetNode fn : fns) {
				System.out.println("FROM CONSTRAINT: " + fn.availableValues().exec().toList().blockingGet());
			}
		}
	}
	
	@Test//done
	public void testPathFinder() {
		load(DS_SIMPLE_1);
		final ConceptPathFinder conceptPathFinder = taskGenerator.getConceptPathFinder();
		//new Concept()
		final Concept targetConcept = new Concept(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s);
		final PathSearch<SimplePath> pathSearch = conceptPathFinder.createSearch(fq.root().remainingValues().baseRelation().toUnaryRelation(), targetConcept);

		pathSearch.setMaxPathLength(2);
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

	@Test//done
	public void testCp14() {
		load(DS_SIMPLE_3);
		taskGenerator.setPseudoRandom(new Random(1l));
		final FacetNode node = fq.root();

		assertEquals( "{ ?v_1  ?p  ?o }" ,
				getQueryPattern(node) );


		taskGenerator.setRandom(new Random(6128191552201113548L));
		final boolean b = taskGenerator.applyCp14(node);
		assertEquals( "{ ?v_2  <http://www.example.org/inhabitants>  ?v_1 ;\n" +
				"        <http://www.example.org/population>  560472\n" +
				"  { ?v_1  ?p  ?o }\n" +
				"}" , getQueryPattern(node) );

		changeTracker.commitChanges();


		long i;
		final SolutionTracker solutions = new SolutionTracker(
				"{ { ?v_2  <http://www.example.org/inhabitants>  ?v_1 ;\n" +
						"          <http://www.example.org/population>  560472 .\n" +
						"    ?v_4  <http://www.example.org/mayor>  ?v_1 ;\n" +
						"          <http://www.example.org/inhabitants>  ?v_5 .\n" +
						"    ?v_5  <http://xmlns.com/foaf/0.1/age>  ?v_6\n" +
						"    FILTER ( ?v_6 <= 60 )\n" +
						"    FILTER ( ?v_6 >= 10 )\n" +
						"  }\n" +
						"  ?v_1  ?p  ?o\n" +
						"}",

				"{ { ?v_2  <http://www.example.org/inhabitants>  ?v_1 ;\n" +
						"          <http://www.example.org/population>  560472 ;\n" +
						"          <http://www.example.org/inhabitants>  ?v_4 .\n" +
						"    ?v_4  <http://xmlns.com/foaf/0.1/age>  ?v_5\n" +
						"    FILTER ( ?v_5 >= 10 )\n" +
						"    FILTER ( ?v_5 <= 60 )\n" +
						"  }\n" +
						"  ?v_1  ?p  ?o\n" +
						"}"
		);
		for (i = 0; i < 2l; i++) {
			final boolean c = taskGenerator.applyCp14(node);
			final String qp = getQueryPattern(node);
			solutions.assertSolution(qp);
			System.out.println(">>>"+ TaskGenerator.findExistingNumericConstraints(node.constraints()));
			changeTracker.discardChanges();
		}
		solutions.assertAllSeen();
	}

	@Test//done
	public void testCp13() {
		load(DS_SIMPLE_2);
		taskGenerator.setPseudoRandom(new Random(1234l));
		final FacetNode node = fq.root();

		assertEquals( "{ ?v_1  ?p  ?o }" ,
				getQueryPattern(node) );

		taskGenerator.applyCp13(node);

		assertEquals("{ <http://www.example.org/Leipzig>\n" +
				"            <http://www.example.org/mayor>  ?v_1\n" +
				"  { ?v_1  ?p  ?o }\n" +
				"}", getQueryPattern(node));

		changeTracker.discardChanges();

		taskGenerator.applyCp13(node);

		assertEquals( "{ ?v_1      <http://www.example.org/locatedIn>  ?v_2 .\n" +
				"  <http://www.example.org/Leipzig>\n" +
				"            <http://www.example.org/locatedIn>  ?v_2\n" +
				"}", getQueryPattern(node));
	}

	@Test//done
	public void testCp10() {
		load(DS_SIMPLE);
		final FacetNode node = fq.root();

		changeTracker.commitChanges();
		assertEquals( "{ ?v_1  ?p  ?o }" , getQueryPattern(node) );

		taskGenerator.applyCp1(node);
		changeTracker.commitChanges();

		assertNotEquals( "{ ?v_1  ?p  ?o }" , getQueryPattern(node) );

		taskGenerator.applyCp10();

		assertEquals( "{ ?v_1  ?p  ?o }" , getQueryPattern(node) );

		fq.focus(fq.root().fwd("http://www.example.org/locatedIn").one());
		changeTracker.commitChanges();

		taskGenerator.applyCp3(node);
		changeTracker.commitChanges();
		assertNotEquals("{ ?v_1  <http://www.example.org/locatedIn>  ?v_2 }", getQueryPattern(node));

		taskGenerator.applyCp10();
		assertEquals("{ ?v_1  <http://www.example.org/locatedIn>  ?v_2 }", getQueryPattern(node));

		taskGenerator.applyCp10();
		assertEquals( "{ ?v_1  ?p  ?o }" , getQueryPattern(node) );
	}

	@Test//done
	public void testCp6part() {
		load(DS_SIMPLE_3);
		taskGenerator.setPseudoRandom(new Random(123L));

		final FacetNode node = fq.root();
		/*
		taskGenerator.setRandom(new Random(6128191552201113548L));
		final boolean b = taskGenerator.applyCp14(node);
		*/

		Map.Entry<FacetNode, Range<NodeHolder>> r = TaskGenerator.pickRange(taskGenerator.getRandom(), taskGenerator.getPseudoRandom(), taskGenerator.getNumericProperties(),
				taskGenerator.getConceptPathFinder(), node, null, 0, 2, false, true, false);

		System.out.println("Pick: " + r);

		if (r != null) {
			r.getKey().constraints().range(r.getValue());
		}

		System.out.println(node);

		final Collection<HLFacetConstraint> hlFacetConstraints = fq.root().constraints().listHl();
		System.out.println(hlFacetConstraints);
		Map<HLFacetConstraint, Map<Character, Node>> numericConstraints =
				TaskGenerator.findExistingNumericConstraints(fq.root().constraints());

	/*
			taskGenerator.getRandom().nextLong();
			taskGenerator.getPseudoRandom().nextLong();
			taskGenerator.getRandom().nextLong();
			taskGenerator.getPseudoRandom().nextLong();
			taskGenerator.getRandom().nextLong();
			taskGenerator.getPseudoRandom().nextLong();
			taskGenerator.getRandom().nextLong();
			taskGenerator.getPseudoRandom().nextLong();
			taskGenerator.getRandom().nextLong();
			taskGenerator.getPseudoRandom().nextLong();
			*/

		System.out.println(">>>"+numericConstraints);
		if (!numericConstraints.isEmpty()) {
			taskGenerator.modifyNumericConstraintRandom(hlFacetConstraints, numericConstraints);
		}
		assertEquals("{ { ?v_2  <http://www.example.org/inhabitants>  ?v_1 ;\n" +
				"          <http://www.example.org/inhabitants>  ?v_3 .\n" +
				"    ?v_3  <http://xmlns.com/foaf/0.1/age>  ?v_4\n" +
				"    FILTER ( ?v_4 <= 33 )\n" +
				"    FILTER ( ?v_4 >= 10 )\n" +
				"  }\n" +
				"  ?v_1  ?p  ?o\n" +
				"}", getQueryPattern(fq.root()));
		//fq.constraints().forEach(Resource::removeProperties);
		//fq.constraints().clear();
/*
		{
			final StringWriter sw = new StringWriter();
			RDFDataMgr.write(sw, ((FacetedQueryResource)fq).modelRoot().getModel(), RDFFormat.TURTLE_PRETTY);
			System.out.println(sw.toString());
		}
		*/
		//System.out.println(fq.constraints());

	}

	@Test
	public void testCp8() {
		load(DS_SIMPLE_3);
		taskGenerator.setPseudoRandom(new Random(1234L));

		final FacetNode node = fq.root();

		taskGenerator.applyCp8(node);

		assertEquals("{ ?v_1  <http://xmlns.com/foaf/0.1/based_near>  ?v_2 .\n" +
				"  ?v_2  <http://www.example.org/mayor>  ?v_3 .\n" +
				"  ?v_3  <http://xmlns.com/foaf/0.1/based_near>  ?v_4 .\n" +
				"  ?v_4  <http://www.example.org/population>  ?v_5\n" +
				"  FILTER ( ?v_5 >= 543825 )\n" +
				"  FILTER ( ?v_5 <= 560472 )\n" +
				"}", getQueryPattern(node));
		taskGenerator.applyCp8(node);

		assertEquals("{ ?v_1  <http://xmlns.com/foaf/0.1/age>  10 ;\n" +
				"        <http://xmlns.com/foaf/0.1/based_near>  ?v_3 .\n" +
				"  ?v_3  <http://www.example.org/mayor>  ?v_4 .\n" +
				"  ?v_4  <http://xmlns.com/foaf/0.1/based_near>  ?v_5 .\n" +
				"  ?v_5  <http://www.example.org/population>  ?v_6\n" +
				"  FILTER ( ?v_6 >= 543825 )\n" +
				"  FILTER ( ?v_6 <= 560472 )\n" +
				"}", getQueryPattern(node));
		taskGenerator.applyCp8(node);

		assertEquals("{ ?v_1  <http://xmlns.com/foaf/0.1/age>  ?v_2 ;\n" +
				"        <http://xmlns.com/foaf/0.1/based_near>  ?v_3 .\n" +
				"  ?v_3  <http://www.example.org/mayor>  ?v_4 .\n" +
				"  ?v_4  <http://xmlns.com/foaf/0.1/based_near>  ?v_5 .\n" +
				"  ?v_5  <http://www.example.org/population>  ?v_6\n" +
				"  FILTER ( ?v_2 <= 33 )\n" +
				"  FILTER ( ?v_2 >= 10 )\n" +
				"  FILTER ( ?v_6 >= 543825 )\n" +
				"  FILTER ( ?v_6 <= 560472 )\n" +
				"}", getQueryPattern(node));
		taskGenerator.applyCp8(node);

		assertEquals("", getQueryPattern(node));

	}

	@Test//done
	public void testCp7() {
		load(DS_SIMPLE_3);
		taskGenerator.setPseudoRandom(new Random(1234L));

		final FacetNode node = fq.root();

		taskGenerator.applyCp7(node);

		assertEquals("{ ?v_1  <http://xmlns.com/foaf/0.1/based_near>  ?v_2 .\n" +
				"  ?v_2  <http://www.example.org/mayor>  ?v_3 .\n" +
				"  ?v_4  <http://www.example.org/mayor>  ?v_3 ;\n" +
				"        <http://www.example.org/population>  ?v_5\n" +
				"  FILTER ( ?v_5 >= 543825 )\n" +
				"  FILTER ( ?v_5 <= 560472 )\n" +
				"}", getQueryPattern(node));
		taskGenerator.applyCp7(node);

		assertEquals("{ ?v_1  <http://xmlns.com/foaf/0.1/based_near>  ?v_2 .\n" +
				"  ?v_2  <http://www.example.org/mayor>  ?v_3 .\n" +
				"  ?v_4  <http://www.example.org/mayor>  ?v_3 ;\n" +
				"        <http://www.example.org/population>  560472\n" +
				"}", getQueryPattern(node));

	}

	@Test//done
	public void testCp6() {
		load(DS_SIMPLE_3);
		taskGenerator.setPseudoRandom(new Random(1234L));


		final FacetNode node = fq.root();

		System.out.println(getQueryPattern(node));

		taskGenerator.applyCp6(node);

		assertEquals("{ ?v_1  <http://xmlns.com/foaf/0.1/age>  ?v_2\n" +
				"  FILTER ( ?v_2 >= 10 )\n" +
				"  FILTER ( ?v_2 <= 47 )\n" +
				"}", getQueryPattern(node));

		taskGenerator.applyCp6(node);

		assertEquals("{ ?v_1  <http://xmlns.com/foaf/0.1/age>  ?v_2\n" +
				"  FILTER ( ?v_2 >= 33 )\n" +
				"  FILTER ( ?v_2 <= 47 )\n" +
				"}", getQueryPattern(node));

		taskGenerator.applyCp6(node);

		assertEquals("{ ?v_1  <http://xmlns.com/foaf/0.1/age>  ?v_2\n" +
				"  FILTER ( ?v_2 <= 33 )\n" +
				"  FILTER ( ?v_2 >= 10 )\n" +
				"}", getQueryPattern(node));
	}

	@Test//done
	public void testCp4() {
		load(DS_SIMPLE_2);
		taskGenerator.setPseudoRandom(new Random(1234L));


		final FacetNode node = fq.root();

		assertEquals( "{ ?v_1  ?p  ?o }" ,
				getQueryPattern(node) );

		changeTracker.commitChanges();
		final SolutionTracker solutions = new SolutionTracker(
				"{ ?v_1  <http://xmlns.com/foaf/0.1/age>  ?v_2 ;\n" +
						"        a                     <http://xmlns.com/foaf/0.1/Person>\n" +
						"  FILTER bound(?v_2)\n" +
						"}",

				"{ ?v_1  <http://www.w3.org/2000/01/rdf-schema#label>  ?v_2 ;\n" +
						"        a                     <http://xmlns.com/foaf/0.1/Person>\n" +
						"  FILTER bound(?v_2)\n" +
						"}"
				);
		long i;
		for (i = 0L; i < 2L; i++) {
			System.out.println(i);
			//taskGenerator.setRandom(new Random(i));
			//taskGenerator.setPseudoRandom(new Random(~i));
			taskGenerator.applyCp4(node);
			final String qp = getQueryPattern(node);
			solutions.assertSolution(qp);
			changeTracker.discardChanges();
		}
		solutions.assertAllSeen();
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

		assertEquals( "{ ?v_1  <http://www.example.org/contains>  ?v_2 .\n" +
				"  ?v_2  <http://www.example.org/mayor>  <http://www.example.org/BurkhardJung>\n" +
				"}" , getQueryPattern(node) );
		System.out.println(getQueryPattern(node));

		changeTracker.discardChanges();

		taskGenerator.getRandom().nextLong();
		taskGenerator.getRandom().nextLong();
		taskGenerator.getRandom().nextLong();

		taskGenerator.applyCp3(node);

		assertEquals( "{ ?v_1  <http://www.example.org/locatedIn>  ?v_2 .\n" +
				"  ?v_2  <http://www.example.org/contains>  <http://www.example.org/Leipzig>\n" +
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

		assertEquals( "{ ?v_1  <http://www.example.org/contains>  ?v_2 .\n" +
				"  ?v_2  <http://www.example.org/locatedIn>  ?v_3 .\n" +
				"  ?v_3  <http://www.example.org/contains>  ?v_4\n" +
				"  FILTER bound(?v_4)\n" +
				"}" , getQueryPattern(node) );

		changeTracker.discardChanges();

		taskGenerator.getRandom().nextLong();
		taskGenerator.getRandom().nextLong();

		taskGenerator.applyCp2(node);
		final String queryPattern = getQueryPattern(node);
		assertEquals("{ ?v_1  <http://www.example.org/contains>  ?v_2 .\n" +
				"  ?v_2  <http://www.example.org/locatedIn>  ?v_3\n" +
				"  FILTER bound(?v_3)\n" +
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

		assertEquals( "{ ?v_1  <http://www.example.org/population>  500000 }" ,
				getQueryPattern(node)
		);

		//System.out.println("---");
		taskGenerator.applyCp1(node);
		assertEquals( "{ ?v_1  <http://www.example.org/mayor>  <http://www.example.org/BurkhardJung> ;\n" +
						"        <http://www.example.org/population>  500000\n" +
						"}" ,
				getQueryPattern(node)
		);

//		assertEquals( "{ ?v_1  <http://www.w3.org/2000/01/rdf-schema#label>  \"Leipzig\" ;\n" +
//		"        <http://www.example.org/population>  500000\n" +
//		"}" ,
//getQueryPattern(node)
//);

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

		assertEquals( "{ ?v_1  <http://www.example.org/population>  ?v_2\n" +
						"  FILTER ( ?v_2 <= 80000000 )\n" +
						"  FILTER ( ?v_2 >= 50000 )\n" +
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

	class Seen {
		private boolean f = false;
		boolean seen() {
			return this.f = true;
		}
		boolean wasSeen() {
			return this.f;
		}
	}

	class SolutionTracker {
		ImmutableMap<Object, Seen> solutions;
		SolutionTracker(Object... solutions) {
			final ImmutableMap.Builder<Object, Seen> builder = ImmutableMap.<Object, Seen>builder();
			for (Object s : solutions) {
				builder.put(s, new Seen());
			}
			this.solutions = builder.build();
		}

		void assertAllSeen() {
			assertArrayEquals( solutions.entrySet().stream().map(Map.Entry::getKey).toArray() ,
					solutions.entrySet().stream().map( es -> es.getValue().wasSeen() ? es.getKey() : "[]").toArray() );
		}

		void assertSolution(Object o) {
			final Seen seen = solutions.get(o);
			final boolean ok = seen != null && seen.seen();
			assertEquals( ok ? o : ""  , o );
		}
	}
}
