package org.hobbit.benchmark.faceted_browsing.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.connection.TransactionalTmp;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraphQuads;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;

import com.github.davidmoten.rx2.flowable.Transformers;

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

class DatasetGraphFromCollection
	extends DatasetGraphQuads
	implements TransactionalTmp
{
	protected Collection<Quad> quads;
	
	public DatasetGraphFromCollection() {
		this(new ArrayList<Quad>());
	}	
	
	public DatasetGraphFromCollection(Collection<Quad> quads) {
		super();
		this.quads = quads;
	}

	@Override
	public boolean supportsTransactions() {
		return false;
	}

	public Stream<Quad> findStream(Node g, Node s, Node p, Node o) {
		Node gm = g == null ? Node.ANY : g;
		Triple t = Triple.createMatch(s, p, o);
		
		Stream<Quad> result = quads.stream()
					.filter(q -> gm.matches(q.getGraph()) && t.matches(q.asTriple()));
		
		return result;
	}

	@Override
	public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		Iterator<Quad> result = findStream(g, s, p, o).iterator();
		return result;
	}

	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		Iterator<Quad> result = findStream(g, s, p, o)
			.filter(q -> !Quad.isDefaultGraph(q.getGraph()))
			.iterator();
		return result;
	}

	@Override
	public void add(Quad quad) {
		quads.add(quad);
	}

	@Override
	public void delete(Quad quad) {
		quads.remove(quad);
	}

	@Override
	public Graph getDefaultGraph() {
		return GraphView.createDefaultGraph(this);
	}

	@Override
	public Graph getGraph(Node graphNode) {
		return GraphView.createNamedGraph(this, graphNode);
	}

	@Override
	public void addGraph(Node graphName, Graph graph) {
		graph.find().forEachRemaining(t -> add(new Quad(graphName, t)));
	}
}

public class TestConceptPathFinder {
	
	@Test
	public void testTrigStream() {
		Iterable<Quad> i = () -> RDFDataMgr.createIteratorQuads(TestConceptPathFinder.class.getClassLoader().getResourceAsStream("rdf-stream.trig"), Lang.TRIG, "http://www.example.org/");

		Flowable<Dataset> eventStream = Flowable.fromIterable(i)
				.compose(Transformers.<Quad>toListWhile(
			            (list, t) -> list.isEmpty() 
			                         || list.get(0).getGraph().equals(t.getGraph())))
				.map(DatasetGraphFromCollection::new)
				.map(DatasetFactory::wrap);
		
		eventStream.forEach(d -> {
			System.out.println("Got event");
			RDFDataMgr.write(System.out, d, RDFFormat.TRIG);
		});
		
		Dataset dg = DatasetFactory.wrap(new DatasetGraphFromCollection());//DatasetFactory.create();

//		while(it.hasNext()) {
//			Quad quad = it.next();
//			dg.asDatasetGraph().add(quad);
//			//dg.getNamedModel(quad.getGraph().getURI()).add
//			System.out.println(quad);
//		}
		
		
		//DatasetFactory.cr
		//RDFDataMgr.createDatasetWriter(Lang.TRIG).write(out, datasetGraph, prefixMap, baseURI, context);
	}
	
	
	//@Test
	public void testReverseProperties() {
		Model model = RDFDataMgr.loadModel("simple-reverse-path.ttl");
		
		// The source concept denotes the set of resources matching the facet constraints
		UnaryRelation src = Concept.create("VALUES (?s) { (eg:a) }", "s", PrefixMapping.Extended);
		UnaryRelation target = Concept.create("?s eg:p3 ?o", "s", PrefixMapping.Extended);

		// The target concept denotes the set of resources carrying numeric properties

		// TODO We need to wire up pathPattern with the path finder
		List<Path> paths = ConceptPathFinder.findPaths(
				FluentQueryExecutionFactory.from(model).create(),
				src,
				target,
				100,
				100);
	
		System.out.println(paths);
		//return paths;
	}
}
