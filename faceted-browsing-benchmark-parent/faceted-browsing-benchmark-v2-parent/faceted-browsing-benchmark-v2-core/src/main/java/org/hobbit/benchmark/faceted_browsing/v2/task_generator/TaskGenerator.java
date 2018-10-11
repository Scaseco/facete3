package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetValueCountImpl_;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.changeset.util.RdfChangeTrackerWrapper;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.NodeHolder;
import org.aksw.jena_sparql_api.utils.RangeUtils;
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
import org.apache.jena.rdfconnection.SparqlQueryConnection;
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

	//protected RdfChangeTracker state;

	protected RdfChangeTrackerWrapper changeTracker;


	protected FacetedQuery currentQuery;

	
	public TaskGenerator(RDFConnection conn, List<SetSummary> numericProperties) {
		this.conn = conn;
		this.numericProperties = numericProperties;
		this.rand = new Random(1000);

		try {
			generateScenario();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
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
	public static <X> Function<? super FacetedQuery, X> facetedQueryToFocusNode(Function<? super FacetNode, X> fn) {
		return fq -> fn.apply(fq.focus());
	}

	
	public <X> Callable<X> bindActionToActiveFacetedQuery(Function<? super FacetedQuery, X> fn) {
		return () -> fn.apply(currentQuery);
	}

	public <X> Callable<X> bindActionToFocusNode(Function<? super FacetNode, X> fn) {
		return bindActionToActiveFacetedQuery(facetedQueryToFocusNode(fn));
	}
	
	
	public <X> Callable<X> wrapWithCommitChanges(Callable<X> supplier) {
		return () -> {
			X r = supplier.call();

//			System.out.println("BEFORE CHANGES");
//			RDFDataMgr.write(System.out, changeTracker.getDataModel(), RDFFormat.TURTLE_PRETTY);

			
			
			changeTracker.commitChanges();

//			System.out.println("AFTER CHANGES");
//			RDFDataMgr.write(System.out, changeTracker.getDataModel(), RDFFormat.TURTLE_PRETTY);
			
			return r;
		};
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
	
	public void generateScenario() throws Exception {
		
		
		// Maps a chokepoint id to a function that given a faceted query
		// yields a supplier. Invoking the supplier applies the action and yields a runnable for undo.
		// if an action is not applicable, the supplier is null
		Map<String, Callable<Boolean>> cpToAction = new HashMap<>();
		
		Model baseModel = ModelFactory.createDefaultModel();
		Model changeModel = ModelFactory.createDefaultModel();

		//RdfChangeTrackerWrapper
		changeTracker = RdfChangeTrackerWrapperImpl.create(changeModel, baseModel);		
		
		Model dataModel = changeTracker.getDataModel();
		
		XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
		FacetedQueryImpl.initResource(facetedQuery);
		
		currentQuery = new FacetedQueryImpl(facetedQuery, null, conn);
		
		changeTracker.commitChangesWithoutTracking();
				
		// How to wrap the actions such that changes go into the change Model?
		
//		addAction(cpToAction, "cp1", TaskGenerator::applyCp1);
		
//		cpToAction.put("cp1", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp1)));
//		cpToAction.put("cp2", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp2)));
//		cpToAction.put("cp3", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp3)));
//		cpToAction.put("cp4", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp4)));
//		cpToAction.put("cp5", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp5)));
		cpToAction.put("cp6", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp6)));
//		cpToAction.put("cp7", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp7)));
//		cpToAction.put("cp8", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp8)));
//		cpToAction.put("cp9", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp9)));
//		
		//cpToAction.put("cp10", this::applyCp10);
//
//		cpToAction.put("cp11", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp11)));
//		cpToAction.put("cp12", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp12)));
//		cpToAction.put("cp13", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp13)));
//		cpToAction.put("cp14", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp14)));


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
		
		WeightedSelectorMutable<String> s = WeightedSelectorMutable.create(concreteWeights);
		
		Range<Double> range = config.getPropertyResourceValue(Vocab.scenarioLength).as(RangeSpec.class).toRange(Double.class);
		int scenarioLength = (int)RangeUtils.pickDouble(range, rand); // TODO Obtain value from config
		//scenarioLength = 100;
		System.out.println("Scenario length: " + scenarioLength);
		
		FacetedQuery fq = FacetedQueryImpl.create(conn);
		//fq.connection(conn);

		List<String> chosenActions = new ArrayList<>();
		for(int i = 0; i < scenarioLength; ++i) {
			

			// Simplest recovery strategy: If an action could not be applied
			// repeat the process and hope that due to randomness we can advance
			int maxRandomRetries = 1000;
			int j;
			for(j = 0; j < maxRandomRetries; ++j) {
				double w = rand.nextDouble();			
				String step = s.sample(w);
				logger.info("Next randomly selected action: " + step);
				
				Callable<Boolean> actionFactory = cpToAction.get(step);
				if(actionFactory == null) {
					// TODO Prevent encountering this case using prior check
					logger.warn(step + " not associated with an implementation");
				}
				
				if(actionFactory != null) {
					
					boolean success = actionFactory.call();
	
					if(!success) {
						// TODO deal with that case ; pick another action instead or even backtrack
						logger.info("Skipping " + step + "; application failed");
						changeTracker.undo();
						changeTracker.clearRedo();
						continue;
					} else {
						logger.info("Successfully applied " + step + "");
						chosenActions.add(step);
						break;
					}
				} else {
					logger.info("Skipping " + step + "; no implementation provided");
					continue;
				}
			}	

			if(j >= maxRandomRetries) {
				System.out.println("Early abort of benchmark due to no applicable action found");
				break;
			}

			// TODO Check whether the step is applicable - if not, retry with that step removed. Bail out if no applicable step.
			
		}
		
		System.out.println("Chosen actions: " + chosenActions);
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
	 * Undoing former restrictions to previous state
	 * (Go back to instances of a previous step)
	 * @param fn
	 */
	public boolean applyCp10() {
		boolean result;

		if((result = changeTracker.canUndo())) {
			changeTracker.undo();
		}
		
		return result;
	}
	
	


	
	/**
	 * Cp1: Select a facet + value and add it as constraint
	 */
	public static boolean applyCp1(FacetNode fn) {

		// Initially assume failure
		boolean result = false;
		
		//.filter("!isBlank(?x)").
		boolean isBwd = false;

		// Exclude all facet-values for which there are constraints
		// This is a constraint over a binary relation
		FacetValueCount fc = fn
				.step(isBwd)
				.nonConstrainedFacetValueCounts()
				//.sample() TODO We should try whether using sample() in addition makes the process faster
				.randomOrder()
				.limit(1)
				.exec()
				.firstElement()
				.timeout(10, TimeUnit.SECONDS)
				.blockingGet();

		// FacetValueCount fc = fn.fwd().facetValueCounts().sample(true).limit(1).exec().firstElement().blockingGet();
		if(fc != null) {
			// Find a facet value for which the filter does not yet exist
			
			
			//fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
			Node p = fc.getPredicate();
			Node o = fc.getValue();
			
			fn.walk(p, isBwd).one().constraints().eq(o);

			// Pick one of the facet values
			
			logger.info("Applying cp1: " + fn.root().availableValues().exec().toList().blockingGet());

			//fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
			result = true;
		}
		
		return result;
	}

	
	/**
	 * Find all instances which additionally realize this property path with any property value
	 */
	public static boolean applyCp2(FacetNode fn) {
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
	public static boolean applyCp3(FacetNode fn) {
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
		
		boolean result = false;
		return result;
	}

	
	/** 
	 * Property class value based transition
     * (Find all instances which additionally have a property value lying in a certain class)
	 */
	public static boolean applyCp4(FacetNode fn) {
		//System.out.println("cp2 item: " + fn.fwd().facets().sample(true).limit(1).exec().firstElement().map(RDFNode::asNode).blockingGet().getClass());
		
		// TODO Exclude values that denote meta vocabulary, such as 'owl:Class', 'rdf:Property' etc
		FacetNode typeFacetNode = fn.fwd(RDF.type).one();
		Node node = typeFacetNode.remainingValues().exclude(OWL.Class, RDFS.Class).sample(true).limit(1).exec().firstElement().map(x -> x.asNode()).blockingGet();
		if(node != null) {
			typeFacetNode.constraints().eq(node);
			
			// Pick one of the facet values
			logger.info("Applying cp4) " + fn.root().remainingValues().exec().toList().blockingGet());
		}
		
		boolean result = false;
		return result;
	}


	/** 
     * Transition of a selected property value class to one of its subclasses
     * (For a selected class that a property value should belong to, select a subclass) 
	 */
	public static boolean applyCp5(FacetNode fn) {
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

		boolean result = false;
		return result;
	}

	
	public static UnaryRelation createConcept(Collection<? extends RDFNode> nodes) {
		UnaryRelation result = new Concept(
				new ElementFilter(new E_OneOf(new ExprVar(Vars.p), ExprListUtils.nodesToExprs(nodes.stream().map(RDFNode::asNode).collect(Collectors.toSet())))),
				Vars.p);
		
		return result;
	}

	public static List<Path> findPathsToResourcesWithNumericProperties(FacetNode fn, List<SetSummary> numericProperties) {

		SparqlQueryConnection conn = fn.query().connection();
		
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

	/**
	 * This method yields ALL reachable numeric facets together with ALL value distributions.
	 * 
	 * @param fn
	 * @param pathLength
	 * @param numericProperties
	 * @return
	 */
	public static Map<FacetNode, Map<Node, Long>> selectNumericFacets(FacetNode fn, int pathLength, List<SetSummary> numericProperties) {
		Map<FacetNode, Map<Node, Long>> result = new LinkedHashMap<>();
		
		List<Path> paths = findPathsToResourcesWithNumericProperties(fn, numericProperties);
		logger.info("Found " + paths.size() + " paths leading to numeric facets: " + paths);
		
		for(Path path : paths) {
			FacetNode target = fn.walk(Path.toJena(path));

			if(target != null) {
				UnaryRelation numProps = createConcept(numericProperties);
				
				List<Node> ps = target.fwd().facets().filter(numProps).exec().map(n -> n.asNode()).toList().blockingGet();

				for(Node p : ps) {
				
					System.out.println("Chose numeric property " + p);
					//System.out.println("Target: " + target.fwd().facetCounts().exec().toList().blockingGet());
		
					// Sample the set of values and create a range constraint from it
					
					FacetNode v = target.fwd(p).one();
					Map<Node, Long> distribution = target.fwd().facetValueCounts()
							.filter(Concept.parse("?s | FILTER(?s = <" + p.getURI() + ">)"))
							.exec()
							.toMap(FacetValueCount::getValue, x -> x.getFocusCount().getCount())
							.blockingGet();
					//List<Double> vals = v.availableValues().filter(Concept.parse("?s | FILTER(isNumeric(?s))")).sample(true).limit(2).exec().map(nv -> Double.parseDouble(nv.asNode().getLiteralLexicalForm())).toList().blockingGet();
		
					System.out.println("Values: " + distribution);
				
//					result = Maps.immutableEntry(v, distribution);

					// Only yield non-empty distributions
					if(!distribution.isEmpty()) {
						result.put(v, distribution);
					}
				}
			}
			
		}
		return result;
	}

	public static Entry<FacetNode, Map<Node, Long>> selectNumericFacet(FacetNode fn, int pathLength, Random rand, List<SetSummary> numericProperties) {
		Entry<FacetNode, Map<Node, Long>> result = null;
		
		List<Path> paths = findPathsToResourcesWithNumericProperties(fn, numericProperties);
		
		FacetNode target = null;
		if(!paths.isEmpty()) {
			int index = rand.nextInt(paths.size());
			Path path = paths.get(index);
		
			target = fn.walk(Path.toJena(path));
		}

		if(target != null) {
			UnaryRelation numProps = createConcept(numericProperties);
			
			Node p = target.fwd().facets().filter(numProps).sample(true).exec().map(n -> n.asNode()).firstElement().blockingGet();
			
			if(p != null) {
			
				System.out.println("Chose numeric property " + p);
				//System.out.println("Target: " + target.fwd().facetCounts().exec().toList().blockingGet());
	
				// Sample the set of values and create a range constraint from it
				
				FacetNode v = target.fwd(p).one();
				Map<Node, Long> distribution = target.fwd().facetValueCounts().filter(Concept.parse("?s | FILTER(?s = <" + p.getURI() + ">)")).exec().toMap(FacetValueCount::getPredicate, x -> x.getFocusCount().getCount()).blockingGet();
				//List<Double> vals = v.availableValues().filter(Concept.parse("?s | FILTER(isNumeric(?s))")).sample(true).limit(2).exec().map(nv -> Double.parseDouble(nv.asNode().getLiteralLexicalForm())).toList().blockingGet();
	
				System.out.println("Values: " + distribution);
			
				result = Maps.immutableEntry(v, distribution);
			}
		}
		
		return result;
	}
	
	
	/**
	 * Picks a facet node and a range of values in accordance with the specification 
	 * 
	 * pickConstant: Select only a specific value; ignores upper / lower bound
	 * 
	 */
	//public Cell<FacetNode, Node, Node>

	
	public Entry<FacetNode, Range<NodeHolder>> pickRange(
			FacetNode facetNode,
			int maxPathLength,
			boolean pickConstant,
			boolean pickLowerBound,
			boolean pickUpperBound) {

		Entry<FacetNode, Range<NodeHolder>> result = null;

		Map<FacetNode, Map<Node, Long>> cands = selectNumericFacets(facetNode, 1, numericProperties);
		if(!cands.isEmpty()) {
			
			System.out.println("cp6 cand: " + cands);
			
			// Select candidates, thereby using the sum of the value counts as weights
			Map<FacetNode, Long> candToWeight = 
					cands.entrySet().stream()
					.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().values().stream().mapToLong(x -> x).sum()));
	
			// TODO Discard entries with a too small range
			
			// Select a random sub range
			WeightedSelector<FacetNode> selector = WeightedSelectorMutable.create(candToWeight);
			FacetNode cand = selector.sample(rand.nextDouble());
			System.out.println("Picked cand: "  + cand);
			Map<Node, Long> range = cands.get(cand);
			
			// Pick a range
			WeightedSelector<Node> rangeSelector = WeightedSelectorMutable.create(range);
			NodeHolder nvA = null;
			if(pickLowerBound || pickConstant) {
				double ia = rand.nextDouble();			
				Node a = rangeSelector.sample(ia);
				nvA = new NodeHolder(a);
			}
			
			NodeHolder nvB = null;
			if(pickUpperBound && !pickConstant) {
				double ib = rand.nextDouble();
				Node b = rangeSelector.sample(ib);
				nvB = new NodeHolder(b);			
			}
	
			if(pickLowerBound && pickUpperBound) {
				int d = nvA.compareTo(nvB);
				if(d > 0) {
					NodeHolder tmp = nvA;
					nvA = nvB;
					nvB = tmp;
				}
			}
			
			Range<NodeHolder> resultRange;
			
			if(pickConstant) {
				resultRange = Range.singleton(nvA);
			} else if(pickLowerBound) {
				if(pickUpperBound) {
					resultRange = Range.closed(nvA, nvB);
				} else {
					resultRange = Range.atLeast(nvA);
				}
			} else if(pickUpperBound) {
				resultRange = Range.atMost(nvB);
			} else {
				resultRange = null;
			}
			
			System.out.println("Range: " + resultRange);
	
			
			result = Maps.immutableEntry(cand, resultRange);
		}
		
		return result;
	}
	
   /**
    * Change of bounds of directly related numerical data\\
    * (Find all instances that additionally have numerical data lying within a certain interval behind a directly related property)
    * 
    * @param fn
    */
	public boolean applyCp6(FacetNode fn) {
		boolean result = false;

		Entry<FacetNode, Range<NodeHolder>> r = pickRange(fn, 5, false, true, true);
		
		System.out.println("Pick: " + r);
		
		if(r != null) {
			r.getKey().constraints().range(r.getValue());
			result = true;
		}
		
		// TODO If fewer than 2 values remain, indicate n/a 
		
		// 0: lower bound, 1 upper bound
		//rand.nextInt(2);
		
		
//		SetSummary summary = ConceptAnalyser.checkDatatypes(fn.fwd().facetValueRelation())
//		.connection(fn.query().connection()).exec().blockingFirst();
//		
//		System.out.println("CP6 Summary: " + summary);
		return result;
	}
	

	/**
     * Change of numerical data related via a property path of length strictly greater than one edge\\
     * (Similar to 7, but now the numerical data is indirectly related to the instances via a property path)
	 * 
	 * @param fn
	 */
	public static boolean applyCp7(FacetNode fn) {
		
		boolean result = false;
		return result;
	}

	
	/**
	 * Restrictions of numerical data where multiple dimensions are involved\\
     * (Choke points 7 and 8 under the assumption that bounds have been chosen for more than one dimension of numerical data,
     * here, we count latitude and longitude numerical values together as one dimension)
	 * 
	 * @param fn
	 */
	public static boolean applyCp8(FacetNode fn) {
		
		boolean result = false;
		return result;
	}

	/**
	 * Unbounded intervals involved in numerical data
     * (Choke points 7,8,9 when intervals are unbounded and only an upper or lower bound is chosen)
	 */
	public static boolean applyCp9(FacetNode fn) {
		
		boolean result = false;
		return result;
	}

	/**
	 * Entity-type switch changing the solution space
	 * (Change of the solution space while keeping the current filter selections)
	 * @param fn
	 */
	public static boolean applyCp11(FacetNode fn) {
		boolean result = false;
		
		FacetValueCount fwc = fn.fwd().facetValueCounts()
			.peek(dq -> dq.filter(ExprUtils.oneOf(dq.get(FacetValueCountImpl_.VALUE).fwd(RDF.type), OWL.Class.asNode(), RDFS.Class.asNode())))
			.exclude(RDFS.subClassOf)
			.exec()
			.firstElement()
			.blockingGet();

		if(fwc != null) {
			Node p = fwc.getPredicate();
			System.out.println("cp11 got predicate " + p);
			
			FacetNode newFocus = fn.fwd(p).one();
			fn.query().focus(newFocus);
			
			result = true;
		}
		
		// Note: The approach above way does not support fetching the class
		// It would have to look something like
		// ...facetValueCounts().extend((dq, cb) -> dq.get(FacetValueCountImpl_.VALUE)
		//   .fwd(RDF.type)
		//   .constraints().eq(OWL.Class.asNode()).parent() // Add the filter to the path
		//   .project() // Indicate to project the path
		
		return result;
	}

//	/**
//	 * Complicated property paths or circles
//	 * (Choke points 3 and 4 with advanced property paths involved)
//	 * 
//	 * @param fn
//	 */
//	public static boolean applyCp12(FacetNode fn) {
//		// n/a
//		boolean result = false;
//		return result;
//	}

	/**
	 * Inverse direction of an edge involved in property path based transition
	 * (Property path value and property value based transitions where the property path involves traversing edges in the inverse direction)
	 * @param fn
	 */
	public static boolean applyCp13(FacetNode fn) {
		
		boolean result = false;
		return result;
	}

	/**
	 * Numerical restriction over a property path involving the inverse direction of an edge
	 * (Additional numerical data restrictions at the end of a property path where the property path involves traversing edges in the inverse direction)
	 * @param fn
	 */
	public static boolean applyCp14(FacetNode fn) {
		
		boolean result = false;
		return result;
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
