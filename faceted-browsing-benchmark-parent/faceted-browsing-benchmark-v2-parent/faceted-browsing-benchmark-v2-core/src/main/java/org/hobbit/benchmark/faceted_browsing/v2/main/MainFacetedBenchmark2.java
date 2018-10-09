package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.aksw.commons.util.compress.MetaBZip2CompressorInputStream;
import org.aksw.facete.v3.impl.FacetedBrowsingSessionImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.v2.domain.ExprPath;
import org.hobbit.benchmark.faceted_browsing.v2.domain.FactoryWithModel;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessorSPath;
import org.hobbit.benchmark.faceted_browsing.v2.domain.SPath;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.ExprUtilsGeo;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Range;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.vividsolutions.jts.geom.Envelope;

import io.reactivex.Flowable;
import io.reactivex.Single;


public class MainFacetedBenchmark2 {
	
//	public static <P> Set<P> getPathsMentioned(Expr expr, Class<P> pathClass) {
//		Set<P> result = Streams.stream(Traverser.forTree(ExprUtils::getSubExprs).depthFirstPreOrder(expr).iterator())
//			.filter(e -> e instanceof ExprPath)
//			.map(e -> ((ExprPath<?>)e).getPath())
//			.filter(p -> !Objects.isNull(p) && pathClass.isAssignableFrom(p.getClass()))
//			.map(p -> (P)p)
//			.collect(Collectors.toSet());
//		
//		return result;
//	}

	public static <R, C, V> Single<Table<R, C, V>> toTable(Flowable<Cell<R, C, V>> cell) {
		return cell.toList().map(list -> {
			Table<R, C, V> r = HashBasedTable.create();
			//r.cellSet().addAll(list);
			list.forEach(c -> r.put(c.getRowKey(), c.getColumnKey(), c.getValue()));
			return r;
		});
	}
	
	
	public static Query createQueryCycleTest(Path path) {
		Path p = PathFactory.pathZeroOrMore1(path);
		String ps = p.toString(); //"(" + PathWriter.asString(p) + ")";
		
		
		// TODO Build with objects
		String queryStr =
			"SELECT (GROUP_CONCAT(STR(?related)) AS ?cycle) ?member {\n" + 
			"  { SELECT ?member ?related {\n" + 
			"    { SELECT DISTINCT ?member {\n" + 
			"      ?member " + ps + " ?end\n" + 
			"      FILTER(?member = ?end)\n" + 
			"    } }\n" + 
			"    ?member " + ps + " ?related\n" + 
			"  } ORDER BY ?member ?related }\n" + 
			"} GROUP BY ?member";
		
		Query result = QueryFactory.create(queryStr);
		return result;
	}
	
	
	// gremlin uses 'is' for inequality comparison: count().is(gte(2))
	public static void main(String[] args) throws IOException {		
		
		{
			Model m = ModelFactory.createDefaultModel();
			RDFNode a = m.wrapAsResource(NodeFactory.createBlankNode("test"));
			RDFNode b = m.wrapAsResource(NodeFactory.createBlankNode("test"));
			System.out.println("equals? " + Objects.equals(a, b));
		}		
		
//		Concept c = Concept.create("?s a <http://example.org/Person>", "s");
//		Query q = ConceptAnalyser.checkDatatypes(c);
//		System.out.println(q);

				
		Envelope env = new Envelope(0, 10, 0, 10);
		System.out.println(org.apache.jena.sparql.util.ExprUtils.fmtSPARQL(ExprUtilsGeo.createExprOgcIntersects(Vars.s, env, null, null)));
		
		
		Model x = RDFDataMgr.loadModel("cyclic-data.nt");

		System.out.println(ResultSetFormatter.asText(QueryExecutionFactory.create(createQueryCycleTest(new P_Link(RDFS.subClassOf.asNode())), x).execSelect()));

//		System.out.println(ResultSetFormatter.asText(QueryExecutionFactory.create("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
//				+ "SELECT (GROUP_CONCAT(STR(?related)) AS ?cycle) ?member {\n" + 
//				"  { SELECT ?member ?related {\n" + 
//				"    { SELECT DISTINCT ?member {\n" + 
//				"      ?member rdfs:subClassOf+ ?end\n" + 
//				"      FILTER(?member = ?end)\n" + 
//				"    } }\n" + 
//				"    ?member rdfs:subClassOf+ ?related\n" + 
//				"  } ORDER BY ?member ?related }\n" + 
//				"} GROUP BY ?member", x).execSelect()));
		
		
		Model m = ModelFactory.createDefaultModel();
		RDFDataMgr.read(m, new MetaBZip2CompressorInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream("sensor-data.sample.nt.bz2")), null, Lang.NTRIPLES);
		
		System.out.println("Triples in model: " + m.size());

		//RDFDataMgr.write(new FileOutputStream("/tmp/wtf.nt"), m, RDFFormat.NTRIPLES_ASCII);
		
		Concept k = KeywordSearchUtils.createConceptBifContains(BinaryRelationImpl.create(new P_Link(RDFS.label.asNode())), "test");

		//ConceptUtils.createFilterConcept(nodes)
		System.out.println("Keyword search: " + k);

		// Fetch the available properties (without counts)
//		RDFConnection conn = RDFConnectionFactory.connect("http://dbpedia.org/sparql");
		//RDFConnection conn = RDFConnectionFactory.connect("http://localhost:8890/sparql");
		
		{
			RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(RDFDataMgr.loadModel("path-data.ttl")));

			
			System.out.println("Paths: " + ConceptPathFinder.findPaths(
				new QueryExecutionFactorySparqlQueryConnection(conn),
				Concept.create("?s a <http://www.example.org/ThingB>", "s"),
				//Concept.create("?s <http://www.opengis.net/ont/geosparql#asWKT> ?o ", "s"),
				Concept.create("?s <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?x ; <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?y", "s"),
				100,
				100
			));
			
			System.out.println("Paths: " + ConceptPathFinder.findPaths(
				new QueryExecutionFactorySparqlQueryConnection(conn),
				Concept.create("?s a <http://www.example.org/ThingA>", "s"),
				//Concept.create("?s <http://www.opengis.net/ont/geosparql#asWKT> ?o ", "s"),
				Concept.create("?s <http://www.opengis.net/ont/geosparql#geometry> ?y", "s"),
				100,
				100
			));
	
		}

		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(m));
		
				
		FacetedBrowsingSessionImpl session = new FacetedBrowsingSessionImpl(conn);
		
		SPath rootPath = session.getRoot();//session.getRoot().get(RDF.type.getURI(), false);
		SPath typePath = rootPath.get(RDF.type.getURI(), false);
		
		Concept pFilter = ConceptUtils.createFilterConcept(RDF.type.asNode(), RDFS.label.asNode());;

//		Map<Node, Range<Long>> facets = new HashMap<>();
		Map<Node, Range<Long>> facets = session.getFacetsAndCounts(rootPath, false, pFilter)
				.toMap(Entry::getKey, Entry::getValue).blockingGet();

		
		
//		System.out.println(map);
		
		FactoryWithModel<SPath> dimensionFactory = new FactoryWithModel<>(SPath.class);
		
		SPath root = dimensionFactory.get();		
		SPath somePath = root.get(RDF.type.getURI(), false);//.get(RDFS.seeAlso.toString(), true);

		PathAccessor<SPath> pathAccessor = new PathAccessorSPath();
		FacetedQueryGenerator<SPath> g = new FacetedQueryGenerator<>(pathAccessor);
		

		Expr tmp = new E_Equals(new ExprPath<>(somePath), NodeValue.makeNode(OWL.Class.asNode()));
//		Expr resolved = ExprTransformer.transform(exprTransform, tmp);


		g.addConstraint(tmp);

		
		//g.getFacets(path, isReverse)
		//Map<String, BinaryRelation> facets = g.getFacets(somePath.getParent(), false);
		
		
		//g.getFacets(tr);
		
		//Map<String, TernaryRelation> facetValues = g.getFacetValues(somePath.getParent(), somePath.getParent(), false);

		
//		Map<String, TernaryRelation> map = facetValues.entrySet().stream()
//		.collect(Collectors.toMap(Entry::getKey, e -> FacetedQueryGenerator.countFacetValues(e.getValue(), -1)));

		
		
		
		System.out.println("FACETS: " + facets);
		
		Table<Node, Node, Range<Long>> facetValues = toTable(session.getFacetValues(rootPath, false, pFilter, null)).blockingGet();
		
		for(Cell<Node, Node, Range<Long>> cell : facetValues.cellSet()) {
			System.out.println("FACET VALUES: " + cell);			
		}

//		System.out.println("Path mentioned: " + getPathsMentioned(tmp, SPath.class));
//		
//		System.out.println("Relation: " + br);
//		System.out.println("Resolved: " + resolved);
		
		//CriteriaBuilder cb;
		//cb.
		//FacetedQuery q = new FacetedQuery();
		
		//constraintBlock.add(new E_Equals(mapper.getExpr(somePath), NodeValue.makeNode(OWL.Class.asNode())));
		
//		Set<Element> elements = pathToBgpMapper.getElements();
//		for(Element t : elements) {
//			System.out.println("Element: " + t);
//		}
		
//		SPath p1 = spathFactory.newInstance();
//		p1.setParent(root);
//		p1.setReverse(true);
//		p1.setPredicate(RDF.type);

		
		//System.out.println("Property = " + p1.getPredicate());
		RDFDataMgr.write(System.out, dimensionFactory.getModel(), RDFFormat.TURTLE);
	}
}



//PathToRelationMapper<SPath> mapper = new PathToRelationMapper<>(pathAccessor);
//DimensionConstraintBlock constraintBlock = new DimensionConstraintBlock();
//constraintBlock.getPaths().add(somePath);
//QueryFragment.toElement(somePath, new PathAccessorSPath(), elements, pathToNode, varGen)
//QueryFragment aq = QueryFragment.createForFacetCountRemainder(constraintBlock, Vars.s, Arrays.asList(), false);
//System.out.println(aq);
//ExprTransform exprTransform = new ExprTransformViaPathMapper<>(mapper);
//
//BinaryRelation br = mapper.getOverallRelation(somePath); //pathToBgpMapper.getOrCreate(somePath);
