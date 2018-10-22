package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.aksw.commons.accessors.SingleValuedAccessorDirect;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.impl.FacetNodeResource;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.impl.FacetedQueryResource;
import org.aksw.facete.v3.impl.PathAccessorImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.server.utils.FactoryBeanSparqlServer;
import org.apache.jena.graph.compose.Delta;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.jetty.server.Server;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.HierarchyCoreOnDemand;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.WeightedSelectorMutableOld;

class SupplierUtils {
	// Interleave in items of b between each pair of items of a
	// If there is 0 or 1 items in a, b will not be called
	// [a1] [b1] ... [an-1] [bn-1] [an]
	public static <T> Supplier<T> interleave(Supplier<T> a, Supplier<T> b) {
		int toggle[] = {0}; // 0: a-then-b, 1: b-then-a, 2: only-a, 3: only-null
		
		return () -> {
			T r;
			for(;;) {
				if(toggle[0] == 0 || toggle[0] == 2) {
					r = a.get();
					
					toggle[0] = r == null ? 3 : 1;
				} else if(toggle[0] == 1) {
					r = b.get();
					if(r == null) {
						toggle[0] = 2;
						continue;
					} else {
						toggle[0] = 0;
					}
				} else {
					r = null;
				}
				break;
			}

			return r;
		};
	}
	
	public static <T> Supplier<T> limit(int n, Supplier<T> supplier) {
		int i[] = {0};
		return () -> {
			T r = i[0]++ < n ? supplier.get() : null;
			return r;
		};
	}
	
	/**
	 * Flat maps a supplier of suppliers by assuming an inner supplier is finished upon returning null
	 * 
	 * @param supplier
	 * @return
	 */
	public static <T> Supplier<T> flatMap(Supplier<? extends Supplier<? extends T>> supplier) {
		
		// When done, ref's value will be set to null
		// On init, ref's value is a *reference* (to null)
		SingleValuedAccessorDirect<SingleValuedAccessorDirect<Supplier<? extends T>>> ref =
				new SingleValuedAccessorDirect<>(new SingleValuedAccessorDirect<>(null));
				
		return () -> {
			SingleValuedAccessorDirect<Supplier<? extends T>> held = ref.get();
			T r = null;
			if(held != null) {
				while(r == null) {
					Supplier<? extends T> current = held.get();
					
					if(current == null) {
						Supplier<? extends T> next = supplier.get();
						// If we get null for the next supplier, we assume we are done
						if(next == null) {
							ref.set(null);
							break;
						} else {
							held.set(next);
						}
						continue;
					}

					r = current.get();
				}
			}
			return r;
		};
	}

}

public class MainFacetedQueryApi {

	
	public static void main(String[] args) throws Exception {
		new MainFacetedQueryApi().testSimpleFacetedQuery();
	}
	
	
	public void testSimpleFacetedQuery() throws Exception {

		// Test of the selector - works
		{
//			Model distModel = ModelFactory.createDefaultModel();
//			distModel.createResource().addLiteral(RDF.subject, "a").addLiteral(RDF.object, 80);
//			distModel.createResource().addLiteral(RDF.subject, "b").addLiteral(RDF.object, 15);
//			distModel.createResource().addLiteral(RDF.subject, "c").addLiteral(RDF.object, 5);
//			Map<String, Integer> map = new MapFromBinaryRelation(distModel, BinaryRelationImpl.create("?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> ?k ; <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> ?v", "k", "v"));
			
			
			Map<String, Integer> map2 = new HashMap<>();
			map2.put("a", 80);
			map2.put("b", 15);
			map2.put("c", 5);
	
			Map<String, Integer> xx = new TreeMap<>();
			WeightedSelectorMutableOld<String> fn = WeightedSelectorMutableOld.create(map2.entrySet(), Entry::getKey, Entry::getValue);
			Random rand = new Random();
			for(int i = 0; i < 100000; ++i) {
				double x = rand.nextDouble();
				String v = fn.sample(x);
				if(v == null) {
					break;
				}

				Double weight = fn.getWeight(v);
				System.out.println("Sampled " + v + " [" + weight + "]");
				
				fn.setWeight(v, Optional.ofNullable(weight).map(w -> w.doubleValue() - 1.0).orElse(0.0));
				
				xx.compute(v, (kk, vv) -> vv == null ? 1 : vv + 1);
				//System.out.println();
			}
			
			System.out.println(xx);
		}

		Model m = RDFDataMgr.loadModel("path-data.ttl");
		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(m));		

		Delta delta = new Delta(m.getGraph());
		Model model = ModelFactory.createModelForGraph(delta);
		FacetedQueryResource fq = FacetedQueryImpl.create(model, conn);

		if(false) {

		BgpNode test = model.createResource().as(BgpNode.class);
		
		for(int i = 0; i < 20; ++i) {
			System.out.println("Loop: " + i);
			
			test.fwd(RDF.type).one();
			
			//TaskGenerator.applyCp1(fq.root());

			System.out.println("Additions");
			RDFDataMgr.write(System.out, delta.getAdditions(), RDFFormat.NTRIPLES);

			System.out.println("Removals");
			RDFDataMgr.write(System.out, delta.getDeletions(), RDFFormat.NTRIPLES);
		}
		}
		
		
		boolean runServer = false;
		if(runServer) {
			Server server = FactoryBeanSparqlServer.newInstance()
				.setSparqlServiceFactory(new QueryExecutionFactorySparqlQueryConnection(conn))	
				.setPort(7531)
				.create();
		}

		ReactiveSparqlUtils.execSelect(() -> 
			conn.query("" + ConceptUtils.createQueryList(HierarchyCoreOnDemand.createConceptForRoots(PathFactory.pathLink(RDFS.subClassOf.asNode())))))
			.toList().blockingGet().forEach(x -> System.out.println("Reverse Root: " + x));

		
		System.out.println("Done listing roots");
		fq
			.connection(conn)
			.baseConcept(Concept.create("?s a <http://www.example.org/ThingA>", "s"));
		
		// One time auto config based on available data
		TaskGenerator taskGenerator = TaskGenerator.autoConfigure(conn);
		
		int scenarioIdxCounter[] = {0};
		Supplier<Supplier<SparqlTaskResource>> scenarioSupplier = () -> {

			int scenarioIdx = scenarioIdxCounter[0]++;
			Supplier<SparqlTaskResource> core = taskGenerator.generateScenario();
			
			
			Supplier<SparqlTaskResource> taskSupplier = () -> {
				SparqlTaskResource s = core.get();
				if(s != null) {
					// add scenario id
		            s.addLiteral(FacetedBrowsingVocab.scenarioId, scenarioIdx);
	
					String queryId = s.getProperty(FacetedBrowsingVocab.queryId).getString();
		            String scenarioName = "scenario" + scenarioIdx;
					
		            s = ResourceUtils.renameResource(s, "http://example.org/" + scenarioName + "-" + queryId)
		            		.as(SparqlTaskResource.class);
				}
				return s;

			};

//            logger.info("Generated task:\n" + toString(r.getModel(), RDFFormat.TURTLE_PRETTY));

            return taskSupplier;
		};
		
		Supplier<SparqlTaskResource> querySupplier = SupplierUtils.flatMap(scenarioSupplier);
		
		// Now wrap the scenario supplier with the injection of sparql update statements
		
		Supplier<SparqlTaskResource> updateSupplier = () -> null;
		
		
		Supplier<SparqlTaskResource> taskSupplier = SupplierUtils.interleave(querySupplier, updateSupplier);

		
		
		SparqlTaskResource tmp = null;
		for(int i = 0; (tmp = taskSupplier.get()) != null; ++i) {
			System.out.println(i + ": " + SparqlTaskResource.parse(tmp));
		}
		
		System.out.println("DONE");

		
		
//		ReactiveSparqlUtils.execSelect(() -> 
//		conn.query("SELECT DISTINCT ?root {\n" + 
//				"  [] <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?root\n" + 
//				"  FILTER(NOT EXISTS { ?root (<http://www.w3.org/2000/01/rdf-schema#subClassOf>)+ ?ancestor . FILTER(NOT EXISTS {?ancestor <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent }) . })\n" + 
//				"}")).toList().blockingGet().forEach(x -> System.out.println("Reverse Root: " + x));

//		ReactiveSparqlUtils.execSelect(() -> 
//		conn.query("SELECT ?root {\n" + 
//				"  [] <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?root\n" + 
//				"    FILTER(NOT EXISTS { ?root (<http://www.w3.org/2000/01/rdf-schema#subClassOf>)* ?ancestor . ?ancestor <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . FILTER(?root = ?ancestor) . })\n" + 
//				"}")).toList().blockingGet().forEach(x -> System.out.println("Reverse Root: " + x));

		
		
//		ReactiveSparqlUtils.execSelect(() -> 
//		conn.query("SELECT  *\n" + 
//				"WHERE\n" + 
//				"  { { ?v_1  a                     ?v_2 . \n" + 
//				"      ?v_2  a                     ?v_3\n" + 
//				"      FILTER ( ?v_3 = <http://www.w3.org/2002/07/owl#Class> )\n" + 
//				"    }\n" + 
//				"    ?v_1  ?p  ?o\n" + 
//				"  }\n" + 
//				"")).toList().blockingGet().forEach(x -> System.out.println("Item: " + x));
		// One .out() method moves along a given property
		// Another .out() method makes the api state head 'forth' or 'back' and the getFacetCounts method
		// then yields these facets
		
		
		// .getOutgoingFacets
		
		//fq.getRoot().out().getFacetsAndCounts();
		//fq.root().fwd(RDF.type).one().as("test").availableValues();
		
		
//		FacetNode facetNode = fq.root().fwd(RDF.type).one()
//			.fwd(RDF.type).one()
//				.constraints()
//					.eq(OWL.Class.asNode())
//				.end();
		
//		FacetNode facetNode = fq.root().fwd(RDF.type).one()
//			.constraints()
//				.eq(NodeFactory.createURI("http://www.example.org/ThingA"))
//			.end();

//		FacetNode facetNode = fq.root().fwd("http://www.opengis.net/ont/geosparql#geometry").one()
//				.constraints()
//					.exists()
//				.end();

		//TaskGenerator.applyCp1(facetNode);
		
		//TaskGenerator.applyCp4(fq.root());

		//Stream<SparqlTaskResource> scenario = Stream.generate(taskSupplier);
//		
//		Supplier<Iterator<SparqlTaskResource>> it = () -> new AbstractIterator<SparqlTaskResource>() {
//			@Override
//			protected SparqlTaskResource computeNext() {
//				SparqlTaskResource result = taskSupplier.get();
//				result = result == null ? endOfData() : result;
//				return result;
//			}
//		};


//		Flowable<SparqlTaskResource> flow = Flowable.fromIterable(() -> it.get());
//		flow
//			.zipWith(() -> Stream.iterate(0, i -> i + 1).iterator(), (a, b) -> Maps.immutableEntry(a, b))
//			.forEach(t -> {
//				System.out.println("Got query: " + t);
//			});
//		
		
		//taskGenerator.applyCp6(fq.root());

		
		//ConceptPathFinder.
		
		//fq.root().fwd().facetValueCounts();
		
		if(false) { 
//		System.out.println("Available values: " + facetNode.availableValues().exec().toList().blockingGet());
//		System.out.println("Remaining values: " + facetNode.remainingValues().exec().toList().blockingGet());
		
		System.out.println("Test: " + new PathAccessorImpl(fq.modelRoot().getBgpRoot()).isReverse(fq.root().fwd(RDF.type).one().as(FacetNodeResource.class).state()));
		System.out.println("Test: " + new PathAccessorImpl(fq.modelRoot().getBgpRoot()).isReverse(fq.root().bwd(RDF.type).one().as(FacetNodeResource.class).state()));
		
		FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<BgpNode>(new PathAccessorImpl(fq.modelRoot().getBgpRoot()));
		
		fq.constraints().forEach(c -> qgen.addConstraint(c.expr()));
//		fq.constraints().forEach(c -> qgen.getConstraints().add(c.expr()));
		//qgen.getConstraints()
		
		System.out.println("Query Fwd: " + qgen.createMapFacetsAndValues(fq.root().fwd(RDF.type).one().as(FacetNodeResource.class).state(), false, false));
		System.out.println("Query Bwd: " + qgen.createMapFacetsAndValues(fq.root().fwd(RDF.type).one().as(FacetNodeResource.class).state(), true, false));
		
		//fq.root().fwd(RDF.type).one().constraints().eq("foo").addEq("bar").end()

	
		
		// Test whether we get the correct alias
		System.out.println("Alias: " + fq.root().fwd(RDF.type).one().alias());
		}
		
		//fq.root().fwd(RDF.type).one().bwd(RDF.type).one().as("foobar");
		
			//.orderByCount()
//			.filter(null) //KeywordSearchUtils.createConceptBifContains( ))
//			.limit(10)
//			.orderBy()
//			.exec();
		
	}
}
