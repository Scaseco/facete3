package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.changeset.util.RdfChangeSetTrackerImpl;
import org.aksw.jena_sparql_api.changeset.util.RdfChangeTracker;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ConverterFromNodeMapper;
import org.aksw.jena_sparql_api.utils.model.ConverterFromNodeMapperAndModel;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.aksw.jena_sparql_api.utils.views.map.MapFromBinaryRelation;
import org.aksw.jena_sparql_api.utils.views.map.MapFromKeyConverter;
import org.aksw.jena_sparql_api.utils.views.map.MapFromMultimap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.RangeSpec;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.SetSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import io.reactivex.Flowable;


public class TaskGenerator {

	private static final Logger logger = LoggerFactory.getLogger(TaskGenerator.class);
	
	protected List<SetSummary> numericProperties;
	
	protected RDFConnection conn;
	
	protected Random rand;

	protected RdfChangeTracker state;
	

	
	public TaskGenerator(RDFConnection conn, List<SetSummary> numericProperties) {
		this.conn = conn;
		this.numericProperties = numericProperties;
		this.rand = new Random(1000);
		
		generateScenario();
	}

	
	public static TaskGenerator configure(RDFConnection conn) {
		
		List<SetSummary> numericProperties = DatasetAnalyzerRegistry.analyzeNumericProperties(conn).toList().blockingGet();

		
		
//		System.out.println("Properties: " + DatasetAnalyzerRegistry.analyzeNumericProperties(conn).toList().blockingGet());

		TaskGenerator result = new TaskGenerator(conn, numericProperties);
		return result;
	}
	
	
	/**
	 * Wrap any function accepting a {@link FacetNode} argument such that
	 * a {@link FacetedQuery}'s focus is accepted instead.
	 * 
	 * @param fn
	 * @return
	 */
	public static <X> Function<? super FacetedQuery, X> wrap(Function<? super FacetNode, X> fn) {
		return fq -> fn.apply(fq.focus());
	}


	public static Map<RDFNode, RDFNode> viewResourceAsMap(Resource s) {
		Map<RDFNode, Collection<RDFNode>> multimap = new MapFromBinaryRelation(s.getModel(), new BinaryRelationImpl(
				ElementUtils.createElementGroup(
						ElementUtils.createElementTriple(
								new Triple(s.asNode(), RDFS.member.asNode(), Vars.e),
								new Triple(Vars.e, Vocab.key.asNode(), Vars.k),
								new Triple(Vars.e, Vocab.value.asNode(), Vars.v))),
				Vars.k, Vars.v));
		
		Map<RDFNode, RDFNode> result = MapFromMultimap.createView(multimap);
		return result;
	}
	
	public void generateScenario() {
		
		
		// Maps a chokepoint id to a function that given a faceted query
		// yields a supplier. Invoking the supplier applies the action and yields a runnable for undo.
		// if an action is not applicable, the supplier is null
		Map<String, Function<? super FacetedQuery, Boolean>> cpToAction = new HashMap<>();
		
		Model dataModel = ModelFactory.createDefaultModel();
		
		Model changeModel = ModelFactory.createDefaultModel();
		
		XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
		FacetedQueryImpl.initResource(facetedQuery);
		
		RdfChangeTracker rdfChangeTracker = new RdfChangeSetTrackerImpl(dataModel);
		
		// How to wrap the actions such that changes go into the change Model?
		
		cpToAction.put("cp1", wrap(this::applyCp1));
		cpToAction.put("cp2", wrap(this::applyCp2));

//		cpToAction.put("cp3", wrap(this::applyCp3));
//		cpToAction.put("cp4", wrap(this::applyCp4));
//		cpToAction.put("cp5", wrap(this::applyCp5));
//		cpToAction.put("cp6", wrap(this::applyCp6));
//		cpToAction.put("cp7", wrap(this::applyCp7));
//		cpToAction.put("cp8", wrap(this::applyCp8));
//		cpToAction.put("cp9", wrap(this::applyCp9));
//		cpToAction.put("cp10", wrap(this::applyCp10));
//		cpToAction.put("cp11", wrap(this::applyCp11));
//		cpToAction.put("cp12", wrap(this::applyCp12));
//		cpToAction.put("cp13", wrap(this::applyCp13));
//		cpToAction.put("cp14", wrap(this::applyCp14));

		Model weightModel = RDFDataMgr.loadModel("task-generator-config.ttl");
		Set<Resource> configs = weightModel.listResourcesWithProperty(RDF.type, Vocab.ScenarioConfig).toSet();
		
		Resource config = Optional.ofNullable(configs.size() == 1 ? configs.iterator().next() : null)
				.orElseThrow(() -> new RuntimeException("Exactly 1 config required"));
		
		Map<RDFNode, RDFNode> map = viewResourceAsMap(config.getPropertyResourceValue(Vocab.weights));
		
		Map<String, RDFNode> mmm = new MapFromKeyConverter<>(map, new ConverterFromNodeMapperAndModel<>(weightModel, RDFNode.class, new ConverterFromNodeMapper<>(NodeMapperFactory.string)));
		//new MapFromValueConverter<>(mmm, converter);
		
		Map<String, Range<Double>> xxx = Maps.transformValues(mmm, n -> n.as(RangeSpec.class).toRange(Double.class));//new MapFromValueConverter<>(mmm, new ConverterFromNode)
		//Map<String >
		//RangeUtils.
		
		System.out.println("Lookup: " + map.get(weightModel.createLiteral("cp1")));
		System.out.println("Lookup2: " + mmm.get("cp1"));
		
		System.out.println("Map content: " + xxx);
		
		
		// Derive a concrete map
		Map<String, Double> concreteWeights = xxx.entrySet().stream()
				.map(x -> {
					Range<Double> r = x.getValue().intersection(Range.closedOpen(0.0, 1.0));
					double rr = r.lowerEndpoint() + rand.nextDouble() * (r.upperEndpoint() - r.lowerEndpoint());
					return Maps.immutableEntry(x.getKey(), rr);
				})
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		System.out.println("Concrete weights " + concreteWeights);
		
		WeightedSelector<String> s = WeightedSelector.create(concreteWeights);
		
		int scenarioLength = 10; // TODO Obtain value from config
		
		FacetedQuery fq = FacetedQueryImpl.create(conn);
		//fq.connection(conn);

		for(int i = 0; i < scenarioLength; ++i) {
			double w = rand.nextDouble();
			String step = s.sample(w);
			System.out.println("Step: " + step);
			
			Function<? super FacetedQuery, Boolean> actionFactory = cpToAction.get(step);
			if(actionFactory == null) {
				// TODO Prevent encountering this case using prior check
				logger.warn(step + " not associated with an implementation");
			}
			
			if(actionFactory != null) {
				boolean success = actionFactory.apply(fq);

				if(!success) {
					// TODO deal with that case ; pick another action instead or even backtrack
				}
			} else {
				logger.info("Skipping " + step + "; not applicable");
			}
			
			// TODO Check whether the step is applicable - if not, retry with that step removed. Bail out if no applicable step.
		}
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
	public boolean applyCp1(FacetNode fn) {

		FacetValueCount fc = fn.fwd().facetValueCounts().sample(true).limit(1).exec().firstElement().blockingGet();
		if(fc != null) {
			fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
			
			// Pick one of the facet values
			logger.info("Applying cp1: " + fn.root().availableValues().exec().toList().blockingGet());

			fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
		}
		
		return true;
	}

	
	/**
	 * Find all instances which additionally realize this property path with any property value
	 */
	public boolean applyCp2(FacetNode fn) {
		//System.out.println("cp2 item: " + fn.fwd().facets().sample(true).limit(1).exec().firstElement().map(RDFNode::asNode).blockingGet().getClass());
		
		Node node = fn.fwd().facets().sample(true).limit(1).exec().firstElement().map(x -> x.asNode()).blockingGet();
		if(node != null) {
			fn.fwd(node).one().constraints().exists();
			
			// Pick one of the facet values
			logger.info("Applying cp2) " + fn.root().availableValues().exec().toList().blockingGet());
		}
		
		return true;
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
