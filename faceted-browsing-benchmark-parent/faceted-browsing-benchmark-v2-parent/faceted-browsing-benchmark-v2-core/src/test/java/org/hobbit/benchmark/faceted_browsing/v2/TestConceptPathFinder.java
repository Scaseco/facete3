package org.hobbit.benchmark.faceted_browsing.v2;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.rx.DatasetGraphQuadsImpl;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.hobbit.benchmark.faceted_browsing.v2.main.MainTestFacetedBrowsingBenchmarkWithPavelsDataGenerator;
import org.junit.Test;

import com.github.davidmoten.rx2.flowable.Transformers;
import com.google.common.collect.Streams;

import io.reactivex.Flowable;
//
///**
// * GraphView
// * @author Claus Stadler, Oct 30, 2018
// *
// */
//class GraphFromDatasetGraph
//	extends GraphBase
//	implements TransactionalTmp
//{
//	protected Node graphNode;
//	protected DatasetGraph dataset;
//	
//	public GraphFromDatasetGraph(Node graphNode, DatasetGraph dataset) {
//		super();
//		this.graphNode = graphNode;
//		this.dataset = dataset;
//	}
//
//	@Override
//	protected ExtendedIterator<Triple> graphBaseFind(Triple t) {
//		ExtendedIterator<Triple> result = WrappedIterator.create(
//				dataset.find(graphNode, t.getMatchSubject(), t.getMatchPredicate(), t.getMatchObject()))
//				.mapWith(Quad::asTriple);
//
//		return result;
//	}
//
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
//		result = prime * result + ((graphNode == null) ? 0 : graphNode.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		GraphFromDatasetGraph other = (GraphFromDatasetGraph) obj;
//		if (dataset == null) {
//			if (other.dataset != null)
//				return false;
//		} else if (!dataset.equals(other.dataset))
//			return false;
//		if (graphNode == null) {
//			if (other.graphNode != null)
//				return false;
//		} else if (!graphNode.equals(other.graphNode))
//			return false;
//		return true;
//	}
//}

public class TestConceptPathFinder {
	
	//@Test
	public void testTrigDateFormat()
	{
		String data = "<http://www.example.org/> { <http://www.agtinternational.com/resources/livedData#House39274.device6_SmartMeter_Power.Observation_0> <http://www.w3.org/ns/ssn/#observationResultTime> \"2018-10-30 09:41:53\"^^<http://www.w3.org/2001/XMLSchema#dateTime> . }\n";
		data += data;
		data = MainTestFacetedBrowsingBenchmarkWithPavelsDataGenerator.substituteSpaceWithTInTimestamps(data);
		System.out.println("Data:\n" + data);
//		{
//			Model m = ModelFactory.createDefaultModel();
//			RDFDataMgr.read(m, new ByteArrayInputStream("<http://www.agtinternational.com/resources/livedData#House39274.device6_SmartMeter_Power.Observation_0> <http://www.w3.org/ns/ssn/#observationResultTime> \"2018-10-30T09:41:53\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .".getBytes()), Lang.NTRIPLES);
//		}
		Dataset d = DatasetFactory.create();
		RDFDataMgr.read(d, new ByteArrayInputStream(data.getBytes()), Lang.TRIG);
		System.out.println("Data in model:");
		RDFDataMgr.write(System.out, d, RDFFormat.TRIG);
	}
	
	@Test
	public void testTrigStream() {
		Iterable<Quad> i = () -> RDFDataMgr.createIteratorQuads(TestConceptPathFinder.class.getClassLoader()
				.getResourceAsStream("rdf-stream.trig"), Lang.TRIG, "http://www.example.org/");

		Flowable<Dataset> eventStream = Flowable.fromIterable(i)
				.compose(Transformers.<Quad>toListWhile(
			            (list, t) -> list.isEmpty() 
			                         || list.get(0).getGraph().equals(t.getGraph())))
				.map(DatasetGraphQuadsImpl::create)
				.map(DatasetFactory::wrap);

		Dataset dg = DatasetFactory.wrap(new DatasetGraphQuadsImpl());//DatasetFactory.create();

		eventStream.forEach(d -> {
			System.out.println("Got event");
			RDFDataMgr.write(System.out, d, RDFFormat.TRIG);

			Streams.stream(d.asDatasetGraph().find()).forEach(dg.asDatasetGraph()::add);
		});
		

//		while(it.hasNext()) {
//			Quad quad = it.next();
//			dg.asDatasetGraph().add(quad);
//			//dg.getNamedModel(quad.getGraph().getURI()).add
//			System.out.println(quad);
//		}
		
		System.out.println("Roundtrip result");
		RDFDataMgr.write(System.out, dg, RDFFormat.TRIG);
		//DatasetFactory.cr
	}
	
	
	//@Test
	public void testReverseProperties() {
		Model model = RDFDataMgr.loadModel("simple-reverse-path.ttl");
		
		// The source concept denotes the set of resources matching the facet constraints
		UnaryRelation src = Concept.create("VALUES (?s) { (eg:a) }", "s", PrefixMapping.Extended);
		UnaryRelation target = Concept.create("?s eg:p3 ?o", "s", PrefixMapping.Extended);

		// The target concept denotes the set of resources carrying numeric properties

		// TODO We need to wire up pathPattern with the path finder
		List<SimplePath> paths = ConceptPathFinder.findPaths(
				FluentQueryExecutionFactory.from(model).create(),
				src,
				target,
				100,
				100);
	
		System.out.println(paths);
		//return paths;
	}
}
