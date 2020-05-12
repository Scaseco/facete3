package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.function.Function;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.ConceptAnalyser;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.SetSummary;

import io.reactivex.rxjava3.core.Flowable;

//
//class Something {
//	protected Model metaModel;
//
//	Set<Resource> getProperties() {
//		return metaModel.listSubjectsWithProperty(RDF.type, RDF.Property).toSet();
//	}
//}
//
///**
// * A dataset analyser just yields a model (possible partitioned into multiple chunks)
// * for an rdf connection + dataset description
// *
// *
// * @author raven
// *
// */
//interface Analyzer
//	extends Function<SparqlQueryConnection, Flowable<Triple>>
//{
//	//Flowable<Model> analyze(RDFConnection conn);
//}
//
//interface AnalyzerComponent {
//	String deriveGraphName();
//	boolean canViewAs(Class<?> clazz);
//	<T> T viewAs(Class<T> clazz);
//}
//
//class AnalyzerProperyJoinSummaryFwd
//	implements Analyzer
//{
//	@Override
//	public Flowable<Triple> apply(SparqlQueryConnection conn) {
////        String queryStr = "Select Distinct ?x ?y { ?a ?x ?b . ?b ?y ?c . Filter(!regex(str(?x), '^http://www.w3.org/1999/02/22-rdf-syntax-ns#_') && !regex(str(?y), '^http://www.w3.org/1999/02/22-rdf-syntax-ns#_')) }";
//
//		String queryStr = "CONSTRUCT { ?x <" + VocabPath.joinsWith.getURI() + " ?y } { ?a ?x ?b . ?b ?y ?c . Filter(!regex(str(?x), '^http://www.w3.org/1999/02/22-rdf-syntax-ns#_') && !regex(str(?y), '^http://www.w3.org/1999/02/22-rdf-syntax-ns#_')) }";
//
//		//"CONSTRUCT { ?x <" + VocabPath.joinsWith.getURI() + "> ?y { ?x <" + VocabPath.joinsWith.getURI() + "> ?y }
//        Flowable<Triple> result = ReactiveSparqlUtils.execConstructTriples(() -> conn.query(queryStr));
//
//
//        //Multimaps.newSetMultimap(map, factory)
//        return result;
//	}
//}
//
//class AnalyzerProperties
//	implements Analyzer
//{
//
//
//	@Override
//	public Flowable<Triple> apply(SparqlQueryConnection conn) {
//
//
//
////		Flowable<Triple> result = Flowable.concat(
////			ReactiveSparqlUtils.execConstructTriples(() -> conn.query(propertyLiteralRangeQueryStr)),
////			ReactiveSparqlUtils.execConstructTriples(() -> conn.query(numericRangeQueryStr))
////		);
//
//		return result;
//
//
//		//Flowable.concat()
//
//		// Derive numeric property ranges based on existing data
//		//
//
//		// Derive range annotation for numeric values
//		// PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
//
//	}
//}
//
//
////class MapFromGraph
////	extends AbstractMap<Node, Set<Node>>
////{
////	protected BinaryRelation relation;
////	protected SparqlQueryConnection conn;
//////	protected Graph graph;
////
////	@Override
////	public Set<Entry<RDFNode, Set<RDFNode>>> entrySet() {
////		conn.
////
////	}
////}
////
////
////class MyMultimap
////	extends AbstractSetMultimap<K, V>
////{
////
////}
//
//class AnalyzerNumericProperties {
//	@Override
//	public Flowable<Triple> apply(SparqlQueryConnection conn) {
//		BinaryRelation br = new BinaryRelationImpl(ElementUtils.createElement(new Triple(Vars.s, Vars.p, Vars.o)), Vars.p, Vars.o);
//		ConceptAnalyser.checkDatatypes(br).connection(conn).exec().toList().blockingGet();
//
////		System.out.println("Analysis: " + );
//	}
//}

//class AnalyzerFactory {
//	Analyzer create(Resource config);
//}



public class DatasetAnalyzerRegistry {

    public static Flowable<SetSummary> analyzeNumericProperties(SparqlQueryConnection conn) {
        Model model = ModelFactory.createDefaultModel();

        model.add(RDFDataMgr.loadModel("xsd-facets.ttl"));

        String inferPropertiesQueryStr = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> CONSTRUCT { ?p a rdf:Property} { SELECT DISTINCT ?p { ?s ?p ?o } }";
        model.add(conn.queryConstruct(inferPropertiesQueryStr));

        // xsd uses a 'numeric'-facet to annotate types
        // converting this to rdf would be the proper way to do this

        // PREFIX schema: <http://schema.org/>
        String propertyLiteralRangeQueryStr = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> CONSTRUCT { ?p rdfs:range ?r } { SELECT DISTINCT ?p ?r { ?s ?p ?o . FILTER(isLiteral(?o)) . BIND(datatype(?o) AS ?r) } }";
        model.add(conn.queryConstruct(propertyLiteralRangeQueryStr));


        Relation r = new BinaryRelationImpl(ElementUtils.createElement(new Triple(Vars.s, Vars.p, Vars.o)), Vars.p, Vars.o);
        model.add(ConceptAnalyser.checkDatatypes(r).connection(conn).execConstruct().blockingGet());


        String numericRangeQueryStr = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT  ?p { ?p rdfs:range [ rdfs:subClassOf* xsd:numeric ] }";
        Flowable<SetSummary> result = SparqlRx.execSelect(() -> QueryExecutionFactory.create(numericRangeQueryStr, model))
            .map(b -> b.getResource("p").as(SetSummary.class));


        return result;

//		String numericRangeQueryStr = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX schema: <http://schema.org/> SELECT ?P { ?p rdfs:range [ rdfs:subClassOf xsd:numeric ] }";
//		Model enrichment = QueryExecutionFactory.create(numericRangeQueryStr, m).execConstruct();
//		m.add(enrichment);

    }

    public void registerAnalyzer(Function<SparqlQueryConnection, Flowable<Triple>> analyzer) {

    }

    public Dataset analyze(RDFConnection conn) {

//		datasetMetadata.obtain(PropertyJoinSummary.class);


        BinaryRelation br = new BinaryRelationImpl(ElementUtils.createElement(new Triple(Vars.s, Vars.p, Vars.o)), Vars.p, Vars.o);
        System.out.println("Analysis: " + ConceptAnalyser.checkDatatypes(br).connection(conn).exec().toList().blockingGet());

        return null;
        // Obtain numeric properties
        // Get the property join summary
        //ConceptPathFinder
    }
}
