package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.SetSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;

import io.reactivex.Flowable;


public class TaskGenerator {

	private static final Logger logger = LoggerFactory.getLogger(TaskGenerator.class);
	
	protected List<SetSummary> numericProperties;
	
	protected RDFConnection conn;
	
	protected Random rand;

	
	// Based on https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java
	// May also EnumeratedDistribution
	public static <T> T chooseRandomItem(Random rand, Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		double totalWeight = getTotalWeight(items, getWeight);
        double r = rand.nextDouble() * totalWeight;
		T result = chooseItem(r, items, getWeight);
			
        return result;
    }
	
	public static <T> double getTotalWeight(Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		double result = Streams.stream(items).mapToDouble(x -> getWeight.apply(x).doubleValue()).sum();	
		return result;
	}

	public static <T> T chooseItem(double score, Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		T result = null;

        double countWeight = 0.0;
        for (T item : items) {
            countWeight += getWeight.apply(item).doubleValue();
            if (countWeight >= score) {
            	result = item;
            	break;
            }
        }
        
        return result;
    }

	
	public TaskGenerator(RDFConnection conn, List<SetSummary> numericProperties) {
		this.conn = conn;
		this.numericProperties = numericProperties;
		this.rand = new Random(1000);
	}

	
	public static TaskGenerator configure(RDFConnection conn) {
		
		List<SetSummary> numericProperties = DatasetAnalyzerRegistry.analyzeNumericProperties(conn).toList().blockingGet();

		
		
//		System.out.println("Properties: " + DatasetAnalyzerRegistry.analyzeNumericProperties(conn).toList().blockingGet());

		TaskGenerator result = new TaskGenerator(conn, numericProperties);
		return result;
	}
	
	
	public Flowable<Query> generate() {
		
//		return Flowable.create(emitter -> {				
//			int index = rand.nextInt(pool.size());
//						
//			Consumer<FacetNode> action = pool.get(index);
//			
//			// TODO: How to manage selection of a facet node? 
//			action.apply(session, fn);
//			
//			session.undo();
//			
//			
//			
//			
//			emitter.onComplete();
//		}, BackpressureStrategy.BUFFER);

		// Zoom into the map and find a region with amount of data in certain ranges
		
		
		
		
		
		return null;
	}
	

	/**
	 * Cp1: Select a facet + value and add it as constraint
	 */
	public static void applyCp1(FacetNode fn) {
		FacetValueCount fc = fn.fwd().facetValueCounts().sample(true).limit(1).exec().firstElement().blockingGet();
		if(fc != null) {
			fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
			
			// Pick one of the facet values
			logger.info("Applying cp1: " + fn.root().availableValues().exec().toList().blockingGet());
		}
	}
	
	/**
	 * Find all instances which additionally realize this property path with any property value
	 */
	public static void applyCp2(FacetNode fn) {
		//System.out.println("cp2 item: " + fn.fwd().facets().sample(true).limit(1).exec().firstElement().map(RDFNode::asNode).blockingGet().getClass());
		
		Node node = fn.fwd().facets().sample(true).limit(1).exec().firstElement().map(x -> x.asNode()).blockingGet();
		if(node != null) {
			fn.fwd(node).one().constraints().exists();
			
			// Pick one of the facet values
			logger.info("Applying cp2) " + fn.root().availableValues().exec().toList().blockingGet());
		}
	}


	/**
	 * (Find all instances which additionally have a certain value at the end of a property path)
     * This is CP1 with a property path instead of a property.
	 * @param fn
	 */
	public static void applyCp3(FacetNode fn) {
		//System.out.println("cp2 item: " + fn.fwd().facets().sample(true).limit(1).exec().firstElement().map(RDFNode::asNode).blockingGet().getClass());

// TODO We need a session state object to hold information about virtual predicates....
// The old question that needs answering is: On which level(s) to allow virtual predicates - level are:
		// Global: These predicates will be injected into evey query - this may needlessly negatively affect performance 
		// FacetDirNode: At this level, the predicate will only be part when retrieving facets of the given FacetDirNode
		// Containment based (more general than type-based) - use the query containment system to inject facets
		
//		Node node = fn.fwd().facets().sample(true).limit(1).exec().firstElement().map(x -> x.asNode()).blockingGet();
//		if(node != null) {
//			fn.fwd(node).one().constraints().exists();
//			
//			// Pick one of the facet values
//			logger.info("Applying cp3) " + fn.root().availableValues().exec().toList().blockingGet());
//		}
	}

	
	/** 
	 * Property class value based transition
     * (Find all instances which additionally have a property value lying in a certain class)
	 */
	public static void applyCp4(FacetNode fn) {
		//System.out.println("cp2 item: " + fn.fwd().facets().sample(true).limit(1).exec().firstElement().map(RDFNode::asNode).blockingGet().getClass());
		
		// TODO Exclude values that denote meta vocabulary, such as 'owl:Class', 'rdf:Property' etc
		FacetNode typeFacetNode = fn.fwd(RDF.type).one();
		Node node = typeFacetNode.remainingValues().exclude(OWL.Class).sample(true).limit(1).exec().firstElement().map(x -> x.asNode()).blockingGet();
		if(node != null) {
			typeFacetNode.constraints().eq(node);
			
			// Pick one of the facet values
			logger.info("Applying cp4) " + fn.root().remainingValues().exec().toList().blockingGet());
		}
	}


	/** 
     * Transition of a selected property value class to one of its subclasses
     * (For a selected class that a property value should belong to, select a subclass) 
	 */
	public static void applyCp5(FacetNode fn) {
		// Applicability check: There must be at least constraint on the type facet
		List<Node> typeConstraints = fn.root().fwd(RDF.type).one().constraints().stream()
				.map(FacetConstraint::expr)
				.filter(e -> e instanceof E_Equals)
				// HACK To get rid of blank nodes in exprs
				.map(e -> e.applyNodeTransform(n -> n.isBlank() ? Var.alloc("hack") : n))
//				.peek(x -> System.out.println("Peek: " + x))
//				.map(e -> new E_Equals(new ExprVar("hack"), ))
				.map(ExprUtils::tryGetVarConst)
				.filter(e -> e != null)
				.map(Entry::getValue)
				.collect(Collectors.toList());
				
		//boolean isApplicable = !typeConstraints.isEmpty();
		
		// TODO Use deterministic random function here
		// Pick a random type for which there is a subclass
		Collections.shuffle(typeConstraints);

		
		//HierarchyCoreOnDemand.
		
		
		
		// TODO What is the best way to deal with hierarchical data?
		// Probably we need some wrapper object with the two straight forward implementations:
		// fetch relations on demand, and fetch the whole hierarchy once and answer queries from cache

	}

	
	public static UnaryRelation createConcept(Collection<? extends RDFNode> nodes) {
		UnaryRelation result = new Concept(
				new ElementFilter(new E_OneOf(new ExprVar(Vars.p), ExprListUtils.nodesToExprs(nodes.stream().map(RDFNode::asNode).collect(Collectors.toSet())))),
				Vars.p);
		
		return result;
	}

	public List<Path> findPathsToResourcesWithNumericProperties(FacetNode fn) {

		// The source concept denotes the set of resources matching the facet constraints
		UnaryRelation valuesConcept = fn.remainingValues().baseRelation().toUnaryRelation();

		// The target concept denotes the set of resources carrying numeric properties
		UnaryRelation numericValuesConcept = new Concept(
			ElementUtils.createElementGroup(
				ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o),
				createConcept(numericProperties).getElement()),
			Vars.s);

		List<Path> paths = ConceptPathFinder.findPaths(
				new QueryExecutionFactorySparqlQueryConnection(conn),
				valuesConcept,
				numericValuesConcept,
				100,
				100);
	
		return paths;
	}
	
	public Entry<FacetNode, Map<Node, Long>> selectNumericFacet(FacetNode fn, int pathLength) {
		Entry<FacetNode, Map<Node, Long>> result = null;
		
		List<Path> paths = findPathsToResourcesWithNumericProperties(fn);
		
		FacetNode target = null;
		if(!paths.isEmpty()) {
			int index = rand.nextInt(paths.size());
			Path path = paths.get(index);
		
			target = fn.nav(Path.toJena(path));
		}

		if(target != null) {
			UnaryRelation numProps = createConcept(numericProperties);
			
			Node p = target.fwd().facets().filter(numProps).sample(true).exec().map(n -> n.asNode()).firstElement().blockingGet();
			
			System.out.println("Chose numeric property " + p);
			//System.out.println("Target: " + target.fwd().facetCounts().exec().toList().blockingGet());

			// Sample the set of values and create a range constraint from it
			
			FacetNode v = target.fwd(p).one();
			Map<Node, Long> distribution = target.fwd().facetValueCounts().filter(Concept.parse("?s | FILTER(?s = <" + p.getURI() + ">)")).exec().toMap(FacetValueCount::getPredicate, x -> x.getFocusCount().getCount()).blockingGet();
			//List<Double> vals = v.availableValues().filter(Concept.parse("?s | FILTER(isNumeric(?s))")).sample(true).limit(2).exec().map(nv -> Double.parseDouble(nv.asNode().getLiteralLexicalForm())).toList().blockingGet();

			System.out.println("Values: " + distribution);
		
			result = Maps.immutableEntry(v, distribution);
		}
		
		return result;
	}
	
   /**
    * Change of bounds of directly related numerical data\\
    * (Find all instances that additionally have numerical data lying within a certain interval behind a directly related property)
    * 
    * @param fn
    */
	public void applyCp6(FacetNode fn) {

		Entry<FacetNode, Map<Node, Long>> cand = selectNumericFacet(fn, 1);
		
		
		// TODO If fewer than 2 values remain, indicate n/a 
		
		// 0: lower bound, 1 upper bound
		rand.nextInt(2);
		
		
//		SetSummary summary = ConceptAnalyser.checkDatatypes(fn.fwd().facetValueRelation())
//		.connection(fn.query().connection()).exec().blockingFirst();
//		
//		System.out.println("CP6 Summary: " + summary);
	}
	

	/**
     * Change of numerical data related via a property path of length strictly greater than one edge\\
     * (Similar to 7, but now the numerical data is indirectly related to the instances via a property path)
	 * 
	 * @param fn
	 */
	public static void applyCp7(FacetNode fn) {
		
	}

	
	/**
	 * Restrictions of numerical data where multiple dimensions are involved\\
     * (Choke points 7 and 8 under the assumption that bounds have been chosen for more than one dimension of numerical data,
     * here, we count latitude and longitude numerical values together as one dimension)
	 * 
	 * @param fn
	 */
	public static void applyCp8(FacetNode fn) {
		
	}

	/**
	 * Unbounded intervals involved in numerical data
     * (Choke points 7,8,9 when intervals are unbounded and only an upper or lower bound is chosen)
	 */
	public static void applyCp9(FacetNode fn) {
		
	}

	/**
	 * Undoing former restrictions to previous state\\
	 * (Go back to instances of a previous step)
	 * @param fn
	 */
	public static void applyCp10(FacetNode fn) {
		
	}

	/**
	 * Entity-type switch changing the solution space
	 * (Change of the solution space while keeping the current filter selections)
	 * @param fn
	 */
	public static void applyCp11(FacetNode fn) {
		
		// TODO Start at root or focus?
		// Check which entity types are available from the current root
		
		
		// Iterate the available types until we find one for whose corresponding
		// concept there is a path from the current concept
		
	}

	/**
	 * Complicated property paths or circles
	 * (Choke points 3 and 4 with advanced property paths involved)
	 * 
	 * @param fn
	 */
	public static void applyCp12(FacetNode fn) {
		// n/a
	}

	/**
	 * Inverse direction of an edge involved in property path based transition
	 * (Property path value and property value based transitions where the property path involves traversing edges in the inverse direction)
	 * @param fn
	 */
	public static void applyCp13(FacetNode fn) {
		
	}

	/**
	 * Numerical restriction over a property path involving the inverse direction of an edge
	 * (Additional numerical data restrictions at the end of a property path where the property path involves traversing edges in the inverse direction)
	 * @param fn
	 */
	public static void applyCp14(FacetNode fn) {
		
	}

	
	
	
	
//		System.out.println("Facets and counts: " + fn.fwd().facetValueCounts().exec().toList().blockingGet());


		//List<? extends RDFNode> available = fn.availableValues().sample(true).limit(1).exec().toList().blockingGet();
		
		//System.out.println("CP1 Available: " + available);
		
		//RDFNode value = Iterables.getFirst(available, null);
//		if(value != null) {
//			//fn.fwd
//		}
		
		//fq.root().out(property).constraints().eq(value).end().availableValues().exec()	
	
	
	public void simulateNavigation() {
		// perform an entity type switch
		// -> 
		
		
		
		
		
	}
	
}
