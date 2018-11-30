package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import io.reactivex.Flowable;
import org.aksw.facete.v3.api.*;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetNodeResource;
import org.aksw.facete.v3.impl.FacetValueCountImpl_;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.changeset.util.RdfChangeTrackerWrapper;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.impl.bidirectional.ConceptPathFinderSystemBidirectional;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.*;
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
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.graph.NodeTransformExpr;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;
import org.hobbit.benchmark.faceted_browsing.v2.main.SparqlTaskResource;
import org.hobbit.benchmark.faceted_browsing.v2.main.SupplierUtils;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.RangeSpec;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.SetSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Math.log;
import static java.util.Collections.shuffle;


public class TaskGenerator {

	private static final Logger logger = LoggerFactory.getLogger(TaskGenerator.class);

	protected List<SetSummary> numericProperties;

	protected RDFConnection conn;
	protected ConceptPathFinder conceptPathFinder;
	protected Random rand;
	protected Random pseudoRandom;

//protected RdfChangeTracker state;

	protected RdfChangeTrackerWrapper changeTracker;


	protected FacetedQuery currentQuery;


	public TaskGenerator(RDFConnection conn, List<SetSummary> numericProperties, ConceptPathFinder conceptPathFinder) {
		this.conn = conn;
		this.numericProperties = numericProperties;
		this.rand = new Random(1000);
		this.conceptPathFinder = conceptPathFinder;

		Model baseModel = ModelFactory.createDefaultModel();
		Model changeModel = ModelFactory.createDefaultModel();

		//RdfChangeTrackerWrapper
		changeTracker = RdfChangeTrackerWrapperImpl.create(changeModel, baseModel);

		Model dataModel = changeTracker.getDataModel();

		// RDF Resource with state
		XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
		FacetedQueryImpl.initResource(facetedQuery);

		currentQuery = new FacetedQueryImpl(facetedQuery, null, conn);
//		try {
//			generateScenario();
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
	}

	public static Map<HLFacetConstraint, Map<Character, Node>> findExistingNumericConstraints(ConstraintFacade<? extends FacetNode> constraintFacade) {
		Map<HLFacetConstraint, Map<Character, Node>> result = new LinkedHashMap<>();
		for (HLFacetConstraint c : new ArrayList<>(constraintFacade.listHl())) {
			final Set<FacetNode> facetNodes = c.mentionedFacetNodes();
			final FacetNode fn = facetNodes.iterator().next();
			//hlFacetConstraints.remove(c);
			final Expr expr = c.expr();
			final ImmutableSet<Class<? extends Expr>> rangeComparisonExprs =
					ImmutableSet.<Class<? extends Expr>>builder()
							.add(E_GreaterThan.class)
							.add(E_GreaterThanOrEqual.class)
							.add(E_LessThan.class)
							.add(E_LessThanOrEqual.class)
							.add(E_Equals.class)
					.build();
			final Map<Character, Node> boundsMap = new LinkedHashMap<>();
			if (expr instanceof E_LogicalAnd) {
				final List<Expr> subExprs = ExprUtils.getSubExprs(expr);
				boolean rangeCE = subExprs.stream().allMatch(p -> rangeComparisonExprs.contains(p.getClass()));
				if (rangeCE) {
					subExprs.stream().forEach(e -> {
						storeNumericBoundFromExpr((ExprFunction2) e, boundsMap);
					});

				}
				subExprs.forEach(x -> System.out.println("> "+x+x.getClass()));
			} else if (rangeComparisonExprs.contains(expr.getClass())) {
				storeNumericBoundFromExpr((ExprFunction2) expr, boundsMap);
			}
			System.out.println(boundsMap);
			if (!boundsMap.isEmpty()) {
				result.put(c, boundsMap);
			}
			//System.out.println(expr.getClass());
			/*
			{
				// delete constraint
				fn.constraints().list().remove(c.state());
				c.state().removeProperties();
			}
			*/

		}
		return result;
	}

	public static void storeNumericBoundFromExpr(ExprFunction2 e, Map<Character, Node> boundsMap) {
		final List<Node> evc = e.getArgs().stream()
				.filter(p -> p.isConstant() && p.getConstant().isLiteral() && p.getConstant().getNode().getLiteralValue() instanceof Number)
				.map(p -> p.getConstant().getNode())
				.collect(Collectors.toList());

		final Node bound = evc.get(0);
		if (evc.size() == 1) {
			boundsMap.put(e.getOpName().charAt(0), bound);
		}
	}


	public Callable<SparqlTaskResource> createScenarioQuerySupplier() {

		int scenarioIdxCounter[] = {0};
		Callable<Callable<SparqlTaskResource>> scenarioSupplier = () -> {

			int scenarioIdx = scenarioIdxCounter[0]++;
			Supplier<SparqlTaskResource> core = this.generateScenario();


			Callable<SparqlTaskResource> taskSupplier = () -> {
				SparqlTaskResource s = core.get();
				if (s != null) {
					// add scenario id
					s.addLiteral(FacetedBrowsingVocab.scenarioId, scenarioIdx);

					Integer queryId = org.aksw.jena_sparql_api.utils.model.ResourceUtils.getLiteralPropertyValue(s, FacetedBrowsingVocab.queryId, Integer.class); //getString();
					String scenarioName = "scenario" + scenarioIdx;

					s = ResourceUtils.renameResource(s, "http://example.org/" + scenarioName + "-" + queryId)
							.as(SparqlTaskResource.class);
				}
				return s;

			};

//            logger.info("Generated task:\n" + toString(r.getModel(), RDFFormat.TURTLE_PRETTY));

			return taskSupplier;
		};

		Callable<SparqlTaskResource> querySupplier = SupplierUtils.flatMap(scenarioSupplier);

		return querySupplier;
	}

	public static TaskGenerator autoConfigure(RDFConnection conn) {

		List<SetSummary> numericProperties = DatasetAnalyzerRegistry.analyzeNumericProperties(conn).toList().blockingGet();

		ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();


		// Use the system to compute a data summary
		// Note, that the summary could be loaded from any place, such as a file used for caching
		Model dataSummary = system.computeDataSummary(conn).blockingGet();

		RDFDataMgr.write(System.out, dataSummary, RDFFormat.TURTLE_PRETTY);

		// Build a path finder; for this, first obtain a factory from the system
		// set its attributes and eventually build the path finder.
		ConceptPathFinder pathFinder = system.newPathFinderBuilder()
				.setDataSummary(dataSummary)
				.setDataConnection(conn)
				.setShortestPathsOnly(false)
				.build();


//		System.out.println("Properties: " + DatasetAnalyzerRegistry.analyzeNumericProperties(conn).toList().blockingGet());

		TaskGenerator result = new TaskGenerator(conn, numericProperties, pathFinder);
		return result;
	}


//	public static <T> WeightedSelector<T> createSelector(List<Entry<T, Double>> pmf, boolean drawWithReplacement) {
//		WeightedSelectorMutable<T> result = drawWithReplacement
//				? new WeigthedSelectorDrawWithReplacement<>()
//				: new WeightedSelectorMutable<>();
//				
//		result.putAll(pmf);
//		
//		return result;
//	}

	public static FacetNode generatePath(FacetNode fn, PathSpecSimple pathSpec, Supplier<Double> rand) {
		WeightedSelector<Direction> dirSelector = PathSpecSimple.createSelector(pathSpec);
		Predicate<List<P_Path0>> pathValidator = PathSpecSimple.createValidator(pathSpec);

		FacetNode result = generatePathRec(fn, pathSpec, dirSelector, pathValidator, rand, 0);
		return result;
	}

	public static FacetNode generatePathRec(
			FacetNode fn,
			PathSpecSimple pathSpec,
			WeightedSelector<Direction> baseDirSelector,
			Predicate<List<P_Path0>> pathValidator,
			Supplier<Double> rand, int depth) {
		FacetNode result = null;

		boolean doValidate = false;

		// If the path is too short, try to make more steps
		if (depth < pathSpec.getMaxLength()) {
			// If we have exceeded the minimum path length but
			// have met a dead end before reaching the preferred length, we accept it anyway

			// Allow backtracking on the direction: If choosing a direction does not lead to a result
			// use the other direction instead
			WeightedSelector<Direction> dirSelector = baseDirSelector.clone();

			double r = rand.get();
			Direction dir = dirSelector.sample(r);

			boolean dirRetry = false;
			do {


				List<FacetCount> facetCounts = fn.step(dir).facetCounts().exec().toList().blockingGet();

				WeightedSelector<FacetCount> selector = WeigthedSelectorDrawWithReplacement.create(facetCounts, fc -> fc.getDistinctValueCount().getCount());

				// Retry until the base selector is empty or the max retry count is reached
				int numFacetRetries = 10;
				//WeightedSelector<FacetCount> selector = baseSelector.clone();		
				for (int i = 0; i < numFacetRetries && !selector.isEmpty(); ++i) {
					FacetCount fc = selector.sample(rand.get());
					if (fc != null) {
						Node p = fc.getPredicate();

						FacetNode next = fn.fwd(p).one();

						result = generatePathRec(next, pathSpec, dirSelector, pathValidator, rand, depth + 1);

						// If we have a result and reached the preferred length
						// return it
						if (result != null) {// && depth >= pathSpec.getMaxLength()) {
							break;
						}
					}
				}

				// If we have exhausted the retries but have a path longer than min length
				// yield it
				if (result == null && depth > pathSpec.getMinLength()) {
					result = fn;
					doValidate = true;
				}

				// If the direction did not lead to a result, try the other one
				if (result == null) {
					dir = Direction.BACKWARD.equals(dir) ? Direction.FORWARD : Direction.BACKWARD;
					dirRetry = true;
				} else {
					break;
				}

			} while (!dirRetry);
		} else {
			result = fn;
			doValidate = true;
		}

		// If we generated (in contrast to just passing on) a non-null result, validate it
		if (doValidate) {
			List<P_Path0> steps = BgpNode.toSparqlSteps(result.as(FacetNodeResource.class).state());

			boolean accepted = pathValidator.test(steps);
			if (!accepted) {
				result = null;
			}
		}

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


	public Callable<Boolean> wrapWithCommitChanges(Callable<Boolean> supplier) {
		return () -> {
			boolean r;
			try {
				r = supplier.call();
				if (r) {
					changeTracker.commitChanges();
				} else {
					changeTracker.discardChanges();
				}
			} catch (Exception e) {
				changeTracker.discardChanges();
				throw new RuntimeException(e);
			}

//			System.out.println("BEFORE CHANGES");
//			RDFDataMgr.write(System.out, changeTracker.getDataModel(), RDFFormat.TURTLE_PRETTY);


//			System.out.println("AFTER CHANGES");
//			RDFDataMgr.write(System.out, changeTracker.getDataModel(), RDFFormat.TURTLE_PRETTY);

			return r;
		};
	}
//
//	


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

	public Supplier<SparqlTaskResource> generateScenario() {
		// Maps a chokepoint id to a function that given a faceted query
		// yields a supplier. Invoking the supplier applies the action and yields a runnable for undo.
		// if an action is not applicable, the supplier is null
		Map<String, Callable<Boolean>> cpToAction = new HashMap<>();

		changeTracker.commitChangesWithoutTracking();

		// How to wrap the actions such that changes go into the change Model?

//		addAction(cpToAction, "cp1", TaskGenerator::applyCp1);

		cpToAction.put("cp1", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp1)));
		cpToAction.put("cp2", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp2)));
		cpToAction.put("cp3", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp3)));
		cpToAction.put("cp4", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp4)));
//		cpToAction.put("cp5", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp5)));
		cpToAction.put("cp6", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp6)));
		cpToAction.put("cp7", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp7)));
//		cpToAction.put("cp8", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp8)));
//		cpToAction.put("cp9", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp9)));
//		
		cpToAction.put("cp10", this::applyCp10);
//
		cpToAction.put("cp11", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp11)));
//		cpToAction.put("cp12", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp12)));
		cpToAction.put("cp13", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp13)));
		cpToAction.put("cp14", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp14)));


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

		// Remove references to unavailable actions
		for (String k : new ArrayList<>(concreteWeights.keySet())) {
			if (!cpToAction.containsKey(k)) {
				logger.warn("Ignoring reference to action " + k + " as no implementation was registered");
				concreteWeights.remove(k);
			}
		}


		//WeightedSelectorMutable<String> s = WeightedSelectorMutable.create(concreteWeights);

		WeightedSelector<String> actionSelector = WeigthedSelectorDrawWithReplacement.create(concreteWeights);

		Range<Double> range = config.getPropertyResourceValue(Vocab.scenarioLength).as(RangeSpec.class).toRange(Double.class);
		int scenarioLength = (int) RangeUtils.pickDouble(range, rand); // TODO Obtain value from config
		//scenarioLength = 100;
		System.out.println("Scenario length: " + scenarioLength);

//		FacetedQuery fq = FacetedQueryImpl.create(conn);
		//fq.connection(conn);

//		List<String> chosenActions = new ArrayList<>();
//		Stream<SparqlTaskResource> result = IntStream.range(0, scenarioLength)
//				//.mapToObj(i -> )
//				.peek(i -> nextAction(cpToAction, actionSelector))
//				.mapToObj(i -> generateQuery())
//				;
//		
//		for(int i = 0; i < scenarioLength; ++i) {
//			nextAction(cpToAction, actionSelector);
//		}
//		
//		System.out.println("Chosen actions: " + chosenActions);
//		
//		return result;

		int queryIdx[] = {0};
		Supplier<SparqlTaskResource> result = () -> {
			SparqlTaskResource r = null;

			int i = queryIdx[0]++;
			if (i < scenarioLength) {

				String cpName = nextAction(cpToAction, actionSelector);


				// HACK to parse out the integer id of a cp
				// Needed for compatibility with the old evaluation module
				// TODO Get rid of making assumptions about cp ids				
				if (cpName != null) {
					String cpSuffix = cpName.substring(2);
					int cpId = Integer.parseInt(cpSuffix);

					r = generateQuery();
					r
							.addLiteral(FacetedBrowsingVocab.queryId, i) //Integer.toString(i))
							.addLiteral(FacetedBrowsingVocab.chokepointId, cpId);
				}

				//RDFDataMgr.write(System.out, task.getModel(), RDFFormat.TURTLE_PRETTY);
			}
			return r;
		};

		return result;
	}


	public SparqlTaskResource generateQuery() {
		Entry<Node, Query> e = currentQuery.focus().availableValues().toConstructQuery();
		Query q = e.getValue();

		SparqlTaskResource result = ModelFactory.createDefaultModel()
				.createResource()
				.as(SparqlTaskResource.class)
				.setSparqlStmtString(Objects.toString(q));

		return result;
	}

	public String nextAction(Map<String, Callable<Boolean>> cpToAction, WeightedSelector<String> actionSelector) {
		String result = null; // the chosen action

		// Reset the available actions after each iteration
		WeightedSelector<String> s = actionSelector.clone();

		// Simplest recovery strategy: If an action could not be applied
		// repeat the process and hope that due to randomness we can advance
		int maxRandomRetries = 3;
		for (int j = 0; j < maxRandomRetries && !s.isEmpty(); ++j) {
			//while(!s.isEmpty()) {
			double w = rand.nextDouble();
			String step = s.sample(w);
			logger.info("Next randomly selected action: " + step);

			Callable<Boolean> actionFactory = cpToAction.get(step);
			if (actionFactory == null) {
				// TODO Prevent encountering this case using prior check
				logger.warn(step + " not associated with an implementation");
			}

			if (actionFactory != null) {

				boolean success;
				try {
					success = actionFactory.call();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (success) {
//					// Commit any changes introduced by the action
//					changeTracker.commitChanges();
					logger.info("Successfully applied " + step + "");

//					System.out.println("CAN UNDO: " + changeTracker.canUndo());
					//chosenActions.add(step);
					result = step;
					break;
				} else {
					// TODO deal with that case ; pick another action instead or even backtrack
					logger.info("Skipping " + step + "; application failed");

					// Discard any changes introduced by the action
//					changeTracker.discardChanges();
//					System.out.println("CAN UNDO: " + changeTracker.canUndo());
//					changeTracker.undo();
//					changeTracker.clearRedo();
					continue;
				}
			} else {
				logger.info("Skipping " + step + "; no implementation provided");
				continue;
			}
		}

//		if(result == null) {
//			logger.error("Early abort of benchmark due to no applicable action found");
//			// TODO Probably raise an exception
////		} else {
//
//			//System.out.println("GENERATED QUERY:" + currentQuery.root().availableValues().exec().toList().blockingGet());
//			System.out.println("GENERATED QUERY: " + currentQuery.root().availableValues().toConstructQuery());
//			// TODO Check whether the step is applicable - if not, retry with that step removed. Bail out if no applicable step.
//			
//			// Now generate the faceted browsing query from the state
//
//		}
//

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
	 * Undoing former restrictions to previous state
	 * (Go back to instances of a previous step)
	 *
	 * @return true on success
	 */
	public boolean applyCp10() {
		boolean result;

		if ((result = changeTracker.canUndo())) {
			changeTracker.undo();
		}

		return result;
	}


	/**
	 * Cp1: Select a facet + value and add it as constraint
	 * <p>
	 * [done]
	 */
	public boolean applyCp1(FacetNode fn) {

		// Initially assume failure
		boolean result = false;

		//.filter("!isBlank(?x)").
		Direction dir = Direction.FORWARD;

		// Exclude all facet-values for which there are constraints
		// This is a constraint over a binary relation
		FacetValueCount fc = fn
				.step(dir)
				.nonConstrainedFacetValueCounts()
				.randomOrder()
				.pseudoRandom(pseudoRandom)
				.limit(1)
				.exec()
				.firstElement()
				.timeout(10, TimeUnit.SECONDS)
				.blockingGet();

		if (fc != null) {
			// Find a facet value for which the filter does not yet exist


			//fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
			Node p = fc.getPredicate();
			Node o = fc.getValue();

			//fn.step(p, isBwd).one().constraints().eq(o);
			fn.step(p, dir).one().constraints().range(Range.singleton(new NodeHolder(o)));

			// Pick one of the facet values

			logger.info("Applying cp1: " + fn.root().availableValues().exec().toList().blockingGet());

			//fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
			result = true;
		}

		return result;
	}


	/**
	 * Find all instances which additionally realize this property path with any property value
	 * <p>
	 * [todo verify]
	 */
	public boolean applyCp2(FacetNode fn) {
		boolean result = false;

		final ConceptPathFinder conceptPathFinder = getConceptPathFinder();
		//new Concept()
		final Concept targetConcept = new Concept(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s);
		final PathSearch<SimplePath> pathSearch = conceptPathFinder.createSearch(fn.remainingValues().baseRelation().toUnaryRelation(), targetConcept);

		pathSearch.setMaxPathLength(3);
		final List<SimplePath> paths =
				pathSearch.exec().filter(sp  -> sp.getSteps().stream().allMatch(p ->
						p.isForward()
				)  && sp.getSteps().size() >= 1 ).toList().blockingGet();
		Collections.shuffle(paths,
				rand);

//		Node node = fn.fwd().facets().pseudoRandom(pseudoRandom)
//				.randomOrder()
//				.limit(1).exec().firstElement().map(x -> x.asNode()).blockingGet();
		if (!paths.isEmpty()) {
			fn.walk(SimplePath.toPropertyPath(paths.get(0))).constraints().exists();
			//fn.fwd(node).one().constraints().exists();

			// Pick one of the facet values
			logger.info("Applying cp2) " + fn.root().availableValues().exec().toList().blockingGet());

			result = true;
		}

		return result;
	}


	/**
	 * (Find all instances which additionally have a certain value at the end of a property path)
	 * This is CP1 with a property path instead of a property.
	 *
	 * @param fn
	 */
	public boolean applyCp3(FacetNode fn) {
		boolean result = false;

		final Direction dir = Direction.FORWARD;

		final ConceptPathFinder conceptPathFinder = getConceptPathFinder();

		final Concept targetConcept = new Concept(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s);
		final PathSearch<SimplePath> pathSearch = conceptPathFinder.createSearch(fn.remainingValues().baseRelation().toUnaryRelation(), targetConcept);

		pathSearch.setMaxPathLength(3);
		final List<SimplePath> paths =
				pathSearch.exec().filter(sp  -> sp.getSteps().stream().allMatch(p ->
						p.isForward()
				)  && sp.getSteps().size() >= 1 ).toList().blockingGet();
		Collections.shuffle(paths,
				rand);

		if (!paths.isEmpty() && applyEqConstraintOnPath(fn, paths.get(0), pseudoRandom)) {
			result = true;
		}

		return result;
		//System.out.println("cp2 item: " + fn.fwd().facets().randomOrder()
		//				.pseudoRandom(pseudoRandom)
		//				.limit(1).exec().firstElement().map(RDFNode::asNode).blockingGet().getClass());

// TODO We need a session state object to hold information about virtual predicates....
// The old question that needs answering is: On which level(s) to allow virtual predicates - level are:
		// Global: These predicates will be injected into evey query - this may needlessly negatively affect performance 
		// FacetDirNode: At this level, the predicate will only be part when retrieving facets of the given FacetDirNode
		// Containment based (more general than type-based) - use the query containment system to inject facets

//		Node node = fn.fwd().facets().randomOrder()
//				.pseudoRandom(pseudoRandom)
//				.limit(1).exec().firstElement().map(x -> x.asNode()).blockingGet();
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
	public boolean applyCp4(FacetNode fn) {
		boolean result = false;

		// TODO Exclude values that denote meta vocabulary, such as 'owl:Class', 'rdf:Property' etc
		//FacetNode typeFacetNode = fn.fwd(RDF.type).one();
		/*final List<FacetValueCount> facetValueCounts = */
		final Map<Node, Double> nodeDoubleMap = fn.fwd()
				.facetValueCounts()
				.only(RDF.type)
				.exec()
				.toMap(xk -> xk.getValue(), xv -> 1 + log(xv.getFocusCount().getCount()), LinkedHashMap::new)
				.blockingGet();

		//})
				//.exec().toList().blockingGet();
		if (!nodeDoubleMap.isEmpty()) {
			System.out.println(nodeDoubleMap);
			final WeightedSelector<Node> selector = WeightedSelectorImmutable.create(nodeDoubleMap);
			final Node clazz = selector.sample(rand.nextDouble());
			System.out.println(clazz);
			fn.fwd(RDF.type).one().constraints().eq(clazz);

			// Pick one of the facet values
			final List<RDFNode> facets = fn.fwd().facets().exclude(RDF.type).randomOrder().pseudoRandom(pseudoRandom).exec().toList().blockingGet();
			if (!facets.isEmpty()) {
				logger.info("Applying cp4) " + facets.get(0));
				fn.fwd(facets.get(0).asNode()).one().constraints().exists();

				result = true;
			}
		}

		return result;
/*
		final List<?> objects = typeFacetNode.remainingValues().exclude(OWL.Class, RDFS.Class).pseudoRandom(pseudoRandom).randomOrder()
				//.limit(1)
				.exec().toList().blockingGet();
		System.out.println(objects);
		Node node = typeFacetNode.remainingValues().exclude(OWL.Class, RDFS.Class).pseudoRandom(pseudoRandom).randomOrder()
				.limit(1).exec().firstElement().map(x -> x.asNode()).blockingGet();
		if (node != null) {
			typeFacetNode.constraints().eq(node);


		}
*/
	}


	/**
	 * Transition of a selected property value class to one of its subclasses
	 * (For a selected class that a property value should belong to, select a subclass)
	 */
	public boolean applyCp5(FacetNode fn) {
		// Applicability check: There must be at least constraint on the type facet
		List<Node> typeConstraints = fn.root().fwd(RDF.type).one().constraints().stream()
				.map(FacetConstraint::expr)
				.filter(e -> e instanceof E_Equals)
				// HACK To get rid of blank nodes in exprs
				.map(e -> ExprTransformer.transform(new NodeTransformExpr(node -> !node.isBlank() ? node : Var.alloc("hack")), e))
//				.peek(x -> System.out.println("Peek: " + x))
//				.map(e -> new E_Equals(new ExprVar("hack"), ))
				.map(ExprUtils::tryGetVarConst)
				.filter(e -> e != null)
				.map(Entry::getValue)
				.collect(Collectors.toList());

		//boolean isApplicable = !typeConstraints.isEmpty();

		// Pick a random type for which there is a subclass
		Collections.shuffle(typeConstraints, rand);


		//new HierarchyCoreOnDemand()
		//HierarchyCoreOnDemand.createConceptForRoots(typeConstraints.)


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

	public static List<SimplePath> findPathsToResourcesWithNumericProperties(
			ConceptPathFinder conceptPathFinder,
			FacetNode fn,
			Path pathPattern,
			int minPathLength, int pathLength, List<SetSummary> numericProperties) {

		//SparqlQueryConnection conn = fn.query().connection();

		// The source concept denotes the set of resources matching the facet constraints
		UnaryRelation valuesConcept = fn.remainingValues().baseRelation().toUnaryRelation();

		// The target concept denotes the set of resources carrying numeric properties
		UnaryRelation numericValuesConcept = new Concept(
				ElementUtils.createElementGroup(
						ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o),
						createConcept(numericProperties).getElement()),
				Vars.s);

		// TODO We need to wire up pathPattern with the path finder
//		List<SimplePath> paths = ConceptPathFinder.findPaths(
//				new QueryExecutionFactorySparqlQueryConnection(conn),
//				valuesConcept,
//				numericValuesConcept,
//				100,
//				100);

		List<SimplePath> paths = conceptPathFinder.createSearch(valuesConcept, numericValuesConcept)
				.setMaxPathLength(pathLength)
				.exec()
				.filter(sp -> sp.getSteps().size() >= minPathLength)
				.toList().blockingGet();
		//paths.stream().f

		return paths;
	}

	/**
	 * This method yields ALL reachable numeric facets together with ALL value distributions.
	 *
	 * @param fn
	 * @param minPathLength
	 * @param pathLength
	 * @param numericProperties
	 * @return
	 */
	public static Map<FacetNode, Map<Node, Long>> selectNumericFacets(
			ConceptPathFinder conceptPathFinder,
			Random pseudoRandom,
			FacetNode fn,
			int minPathLength, int pathLength,
			Path pathPattern,
			/* TODO Add an argument for the path generation model */
			List<SetSummary> numericProperties) {
		Map<FacetNode, Map<Node, Long>> result = new LinkedHashMap<>();


		List<SimplePath> paths;
		if (minPathLength < 0) {
			paths = Collections.singletonList(new SimplePath());
		} else {
			paths = findPathsToResourcesWithNumericProperties(
					conceptPathFinder,
					fn,
					pathPattern,
					minPathLength, pathLength,
					numericProperties);
		}
		logger.info("Found " + paths.size() + " paths leading to numeric facets: " + paths);

		for (SimplePath path : paths) {
			FacetNode target = fn.walk(SimplePath.toPropertyPath(path));

			if (target != null) {
				UnaryRelation numProps = createConcept(numericProperties);

				List<Node> ps;
				if (minPathLength == -1){
					final Node node = FacetNodeResource.reachingProperty(target).asNode();
					if (numericProperties.stream().anyMatch(p -> p.asNode().equals(node))) {
						ps = Collections.singletonList(node);
						target = target.parent();
					} else {
						ps = Collections.emptyList();
					}
				} else {
					ps = target
							.fwd().facets()
							.filter(numProps)
							.pseudoRandom(pseudoRandom)
							.exec().map(n -> n.asNode()).toList().blockingGet();
				}

				for (Node p : ps) {

					System.out.println("Chose numeric property " + p);
					//System.out.println("Target: " + target.fwd().facetCounts().exec().toList().blockingGet());

					// Sample the set of values and create a range constraint from it

					FacetNode v = target.fwd(p).one();
					Map<Node, Long> distribution = target.fwd().nonConstrainedFacetValueCounts()//.facetValueCounts()
							.filter(Concept.parse("?s | FILTER(?s = <" + p.getURI() + ">)"))
							.pseudoRandom(pseudoRandom)
							.exec()
							.toMap(FacetValueCount::getValue, x -> x.getFocusCount().getCount(), LinkedHashMap::new)
							.blockingGet();
					//List<Double> vals = v.availableValues().filter(Concept.parse("?s | FILTER(isNumeric(?s))")).randomOrder()
					//				.pseudoRandom(pseudoRandom)
					//				.limit(2).exec().map(nv -> Double.parseDouble(nv.asNode().getLiteralLexicalForm())).toList().blockingGet();

					System.out.println("Values: " + distribution);

//					result = Maps.immutableEntry(v, distribution);

					// Only yield non-empty distributions
					if (!distribution.isEmpty()) {
						result.put(v, distribution);
					}
				}
			}

		}
		return result;
	}

	public static Entry<FacetNode, Map<Node, Long>> selectNumericFacet(
			ConceptPathFinder conceptPathFinder,
			FacetNode fn,
			int pathLength,
			int minPathLength, Path pathPattern,
			Random rand,
			Random pseudoRandom,
			List<SetSummary> numericProperties) {
		Entry<FacetNode, Map<Node, Long>> result = null;

		List<SimplePath> paths;
		if (minPathLength < 0) {
			paths = Collections.singletonList(new SimplePath());
		} else {
			paths = findPathsToResourcesWithNumericProperties(
					conceptPathFinder,
					fn,
					pathPattern,
					minPathLength, pathLength,
					numericProperties);
		}

		FacetNode target = null;
		if (!paths.isEmpty()) {
			int index = rand.nextInt(paths.size());
			SimplePath path = paths.get(index);

			target = fn.walk(SimplePath.toPropertyPath(path));
		}

		if (target != null) {
			UnaryRelation numProps = createConcept(numericProperties);

			Node p;
			if (minPathLength == -1){
				final Node node = FacetNodeResource.reachingProperty(target).asNode();
				if (numericProperties.stream().anyMatch(q -> q.asNode().equals(node))) {
					p = node;
					target = target.parent();
				} else {
					p = null;
				}
			} else {
				p = target
						.fwd().facets()
						.filter(numProps)
						.randomOrder()
						.pseudoRandom(pseudoRandom)
						.exec().map(n -> n.asNode()).firstElement().blockingGet();
			}

			if (p != null) {

				System.out.println("Chose numeric property " + p);
				//System.out.println("Target: " + target.fwd().facetCounts().exec().toList().blockingGet());

				// Sample the set of values and create a range constraint from it

				FacetNode v = target.fwd(p).one();
				Map<Node, Long> distribution = target.fwd()
						.facetValueCounts()
						.filter(Concept.parse("?s | FILTER(?s = <" + p.getURI() + ">)"))
						.pseudoRandom(pseudoRandom)
						.exec()
						.toMap(FacetValueCount::getPredicate, x -> x.getFocusCount().getCount(), LinkedHashMap::new).blockingGet();
				//List<Double> vals = v.availableValues().filter(Concept.parse("?s | FILTER(isNumeric(?s))")).randomOrder()
				//				.pseudoRandom(pseudoRandom)
				//				.limit(2).exec().map(nv -> Double.parseDouble(nv.asNode().getLiteralLexicalForm())).toList().blockingGet();

				System.out.println("Values: " + distribution);

				result = Maps.immutableEntry(v, distribution);
			}
		}

		return result;
	}

	static int nodeDepth(FacetNode node) {
		int result = 0;
		while ((node = node.parent()) != null) {
			result += 1;
		}
		return result;
	}

	/**
	 * Picks a facet node and a range of values in accordance with the specification
	 * <p>
	 * pickConstant: Select only a specific value; ignores upper / lower bound
	 */
	//public Cell<FacetNode, Node, Node>
	public static Entry<FacetNode, Range<NodeHolder>> pickRange(
			Random rand,
			Random pseudoRandom,
			List<SetSummary> numericProperties,
			ConceptPathFinder conceptPathFinder,

			FacetNode facetNode,
			Path pathPattern, int minPathLength, int maxPathLength,
			boolean pickConstant,
			boolean pickLowerBound,
			boolean pickUpperBound) {

		Entry<FacetNode, Range<NodeHolder>> result = null;

		Map<FacetNode, Map<Node, Long>> cands = selectNumericFacets(
				conceptPathFinder,
				pseudoRandom,
				facetNode,
				minPathLength, maxPathLength,
				pathPattern,
				numericProperties);
		if (!cands.isEmpty()) {

			System.out.println("cp6 cand: " + cands);

			// Select candidates, thereby using the sum of the value counts as weights divided by the path length
			Map<FacetNode, Long> candToWeight =
					cands.entrySet().stream()
							.collect(Collectors.toMap(
									Entry::getKey,
									e -> e
											.getValue()
											.values().stream().mapToLong(x -> x).sum()
									/
									nodeDepth(e.getKey()),
									(k1, k2) -> k1,
									LinkedHashMap::new));

			// TODO Discard entries with a too small range

			// Select a random sub range
			WeightedSelector<FacetNode> selector = WeightedSelectorMutable.create(candToWeight);
			FacetNode cand = selector.sample(rand.nextDouble());
			System.out.println("Picked cand: " + cand);
			Map<Node, Long> range = cands.get(cand);

			// Pick a range
			WeightedSelector<Node> rangeSelector = WeightedSelectorMutable.create(range);
			NodeHolder nvA = null;
			if (pickLowerBound || pickConstant) {
				double ia = rand.nextDouble();
				Node a = rangeSelector.sample(ia);
				nvA = new NodeHolder(a);
			}

			NodeHolder nvB = null;
			if (pickUpperBound && !pickConstant) {
				double ib = rand.nextDouble();
				Node b = rangeSelector.sample(ib);
				nvB = new NodeHolder(b);
			}

			if (pickLowerBound && pickUpperBound) {
				int d = nvA.compareTo(nvB);
				if (d > 0) {
					NodeHolder tmp = nvA;
					nvA = nvB;
					nvB = tmp;
				}
			}

			Range<NodeHolder> resultRange;

			if (pickConstant) {
				resultRange = Range.singleton(nvA);
			} else if (pickLowerBound) {
				if (pickUpperBound) {
					resultRange = Range.closed(nvA, nvB);
				} else {
					resultRange = Range.atLeast(nvA);
				}
			} else if (pickUpperBound) {
				resultRange = Range.atMost(nvB);
			} else {
				resultRange = null;
			}

			System.out.println("Range: " + resultRange);


			result = Maps.immutableEntry(cand, resultRange);
		}

		return result;
	}


	public boolean applyNumericCp(FacetNode fn, Path pathPattern, int minPathLength, int maxPathLength, boolean pickConstant, boolean pickLowerBound, boolean pickUpperBound, boolean allowExisting) {
		boolean result = false;

		Entry<FacetNode, Range<NodeHolder>> r = pickRange(
				rand,
				pseudoRandom,
				numericProperties,
				conceptPathFinder,
				fn,
				pathPattern, minPathLength, maxPathLength,
				pickConstant,
				pickLowerBound,
				pickUpperBound);

		System.out.println("Pick: " + r);

		if (r != null) {
			if (!r.getKey().root().constraints().listHl().stream().anyMatch(p -> p.mentionedFacetNodes().contains(r.getKey()))
				|| allowExisting ) {
				r.getKey().constraints().range(r.getValue());
				result = true;
			}
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
		boolean result;
		org.apache.jena.sparql.path.Path pathPattern = null; // TODO: not implemented: // PathParser.parse("(eg:p|^eg:p)*", PrefixMapping.Extended);

		Map<HLFacetConstraint, Map<Character, Node>> numericConstraints =
				TaskGenerator.findExistingNumericConstraints(fn.root().constraints());
		if (!numericConstraints.isEmpty()) {
			final Collection<HLFacetConstraint> hlFacetConstraints = fn.root().constraints().listHl();
			result = modifyNumericConstraintRandom(hlFacetConstraints, numericConstraints, false, true, true);
		} else {
			result = applyNumericCp(fn, pathPattern, 0, 0, false, true, true, true);
		}
		return result;
	}


	/**
	 * Change of numerical data related via a property path of length strictly greater than one edge\\
	 * (Similar to 7, but now the numerical data is indirectly related to the instances via a property path)
	 *
	 * @param fn
	 */
	public boolean applyCp7(FacetNode fn) {
		org.apache.jena.sparql.path.Path pathPattern = null; // TODO: not implemented // PathParser.parse("(eg:p|^eg:p){2,}", PrefixMapping.Extended);
		boolean result;

		Map<HLFacetConstraint, Map<Character, Node>> numericConstraints =
				TaskGenerator.findExistingNumericConstraints(fn.root().constraints());
		if (!numericConstraints.isEmpty()) {
			final Collection<HLFacetConstraint> hlFacetConstraints = fn.root().constraints().listHl();
			result = modifyNumericConstraintRandom(hlFacetConstraints, numericConstraints, false, true, true);
		} else {
			result = applyNumericCp(fn, pathPattern, 1, 3, false, true, true, true);
		}
		return result;
	}


// NOTE FIXME TODO I don't really get cp8; it looks like repeatedy application of most other cps - so we don't really need it
//	/**
//	 * Restrictions of numerical data where multiple dimensions are involved\\
//     * (Choke points 7 and 8 under the assumption that bounds have been chosen for more than one dimension of numerical data,
//     * here, we count latitude and longitude numerical values together as one dimension)
//	 * 
//	 * @param fn
//	 */
	public boolean applyCp8(FacetNode fn) {
//		
		boolean result = false;
		Map<HLFacetConstraint, Map<Character, Node>> numericConstraints =
				TaskGenerator.findExistingNumericConstraints(fn.root().constraints());
		if (numericConstraints.size() >= 2 && rand.nextInt(10) > 2) {
			final Collection<HLFacetConstraint> hlFacetConstraints = fn.root().constraints().listHl();
			result = modifyNumericConstraintRandom(hlFacetConstraints, numericConstraints, false, true, true);
		} else {
			result = applyNumericCp(fn, null, 0, 3, false, true, true, false);
		}
		return result;
	}

	/**
	 * Unbounded intervals involved in numerical data
	 * (Choke points 7,8,9 when intervals are unbounded and only an upper or lower bound is chosen)
	 */
	public boolean applyCp9(FacetNode fn) {
		boolean pickLowerBound = rand.nextBoolean();
		boolean pickUpperBound = !pickLowerBound;

		boolean result = false;
		Map<HLFacetConstraint, Map<Character, Node>> numericConstraints =
				TaskGenerator.findExistingNumericConstraints(fn.root().constraints());
		if (numericConstraints.size() >= 1 && rand.nextInt(10) >= 2) {
			final Collection<HLFacetConstraint> hlFacetConstraints = fn.root().constraints().listHl();
			result = modifyNumericConstraintRandom(hlFacetConstraints, numericConstraints, false, pickLowerBound, pickUpperBound);

		} else {
			result = applyNumericCp(fn, null, 0, 3, false, pickLowerBound, pickUpperBound, false);
		}
		return result;
	}


	/**
	 * Entity-type switch changing the solution space
	 * (Change of the solution space while keeping the current filter selections)
	 *
	 * @param fn
	 */
	public static boolean applyCp11(FacetNode fn) {
		boolean result;

		// Pick a facet value, where the value has a type (and don't take the schematic subClassOf relation)
		FacetValueCount fwc = fn.fwd().facetValueCounts()
				//.peek(dq -> dq.filter(ExprUtils.oneOf(dq.get(FacetValueCountImpl_.VALUE).fwd(RDF.type), OWL.Class.asNode(), RDFS.Class.asNode())))
				.peek(dq -> dq.filter(new E_Bound(dq.get(FacetValueCountImpl_.VALUE).fwd(RDF.type).asExpr())))
				.exclude(RDFS.subClassOf, RDF.type)
				.exec()
				.firstElement()
				.blockingGet();

		if (fwc != null) {
			Node p = fwc.getPredicate();
			System.out.println("cp11 got predicate " + p);

			FacetNode newFocus = fn.fwd(p).one();
			fn.query().focus(newFocus);

			result = true;
		} else {
			result = false;
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
	 *
	 * @param fn
	 */
	public boolean applyCp13(FacetNode fn) {
		boolean result = false;

		// Choose a random desired path length
		int desiredPathLength = rand.nextInt(2) + 1;

		final ConceptPathFinder conceptPathFinder = getConceptPathFinder();

		final Concept targetConcept = new Concept(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s);
		final PathSearch<SimplePath> pathSearch = conceptPathFinder.createSearch(fn.remainingValues().baseRelation().toUnaryRelation(), targetConcept);

		pathSearch.setMaxPathLength(desiredPathLength);
		final List<SimplePath> paths =
				pathSearch.exec().filter(sp  -> sp.getSteps().stream().anyMatch(p ->
						!p.isForward()
				)  && sp.getSteps().size() >= 1 ).toList().blockingGet();
		Collections.shuffle(paths,
				rand);

		if (!paths.isEmpty() && applyEqConstraintOnPath(fn, paths.get(0), pseudoRandom)) {
			result = true;
		}

		return result;

		/*
		PathSpecSimple pathSpec = PathSpecSimple.create(1, desiredPathLength, 1, 0.5, 0.5);
		FacetNode targetFn = generatePath(fn, pathSpec, rand::nextDouble);

		if (targetFn == null) {
			logger.info("cp13: No suitable path could be generated");
		} else {

			// TODO Hide this parent part in a .nonConstrainedFacetValueCounts() on the FacetNode object itself
			BgpMultiNode parent = targetFn.as(FacetNodeResource.class).state().parent();


			Direction dir = parent.getDirection(); // boolean isReverse = !parent.isForward();

			FacetValueCount fc = targetFn
					.parent()
					.step(dir)
					.nonConstrainedFacetValueCounts()
					.only(parent.reachingProperty())
					//.filter(new E_Equals(new ExprVar(Vars.p), NodeValue.makeNode(parent.reachingProperty().asNode())))
					.randomOrder()
					.pseudoRandom(pseudoRandom)
					.limit(1)
					.exec()
					.firstElement()
					.timeout(10, TimeUnit.SECONDS)
					.blockingGet();

			// FacetValueCount fc = fn.fwd().facetValueCounts().randomOrder()
			//				.pseudoRandom(pseudoRandom)
			//				.limit(1).exec().firstElement().blockingGet();
			if (fc != null) {
				// Find a facet value for which the filter does not yet exist


				//fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
				Node p = fc.getPredicate();
				Node o = fc.getValue();

				//fn.step(p, isBwd).one().constraints().eq(o);
				targetFn.constraints().range(Range.singleton(new NodeHolder(o)));

				// Pick one of the facet values

				logger.info("Applying cp13: " + fn.root().availableValues().exec().toList().blockingGet());

				//fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
				result = true;
			}

			// Now choose a value and set it as constraint
		}
		return result;
*/
	}

	public static boolean applyEqConstraintOnPath(FacetNode fn, SimplePath path, Random pseudoRandom) {
		boolean result = false;
		final FacetNode walk = fn.walk(path);
		final List<RDFNode> objects = walk
				.remainingValues()
				.randomOrder().pseudoRandom(pseudoRandom)
				.exec()
				.toList()
				.blockingGet();
		//System.out.println(objects);
		if (!objects.isEmpty()) {
			walk.constraints().eq(objects.get(0));
			result = true;
		}
		return result;
	}

	/**
	 * Numerical restriction over a property path involving the inverse direction of an edge
	 * (Additional numerical data restrictions at the end of a property path where the property path involves traversing edges in the inverse direction)
	 *
	 * @param fn
	 */
	public boolean applyCp14(FacetNode fn) {

		boolean result = false;


		org.apache.jena.sparql.path.Path pathPattern = null ; // not implemented yet. // PathParser.parse("((eg:p|!eg:p)|(^eg:p|!^eg:p))*", PrefixMapping.Extended);

		Entry<FacetNode, Range<NodeHolder>> r = pickRange(rand, pseudoRandom, numericProperties,
				conceptPathFinder, fn, pathPattern, 1, 3, false, true, true);

		System.out.println("Pick: " + r);

		if (r != null) {
			r.getKey().constraints().range(r.getValue());
			result = true;
		}

		return result;
	}


//		System.out.println("Facets and counts: " + fn.fwd().facetValueCounts().exec().toList().blockingGet());


	//List<? extends RDFNode> available = fn.availableValues().randomOrder()
	//				.pseudoRandom(pseudoRandom)
	//				.limit(1).exec().toList().blockingGet();

	//System.out.println("CP1 Available: " + available);

	//RDFNode value = Iterables.getFirst(available, null);
//		if(value != null) {
//			//fn.fwd
//		}

	//fq.root().out(property).constraints().eq(value).end().availableValues().exec()

	// Pseudo number generator for things that need to randomized from java anyway
	public TaskGenerator setRandom(Random random) {
		this.rand = random;
		return this;
	}
	
	public Random getRandom() {
		return this.rand;
	}

	// Pseudo random number generator for things that should actually be randomized by external reasons (such as database random)
	public TaskGenerator setPseudoRandom(Random pseudoRandom) {
		this.pseudoRandom = pseudoRandom;
		return this;
	}

	public Random getPseudoRandom() {
		return pseudoRandom;
	}

	//public void simulateNavigation() {
		// perform an entity type switch
		// -> 
	//}

	public ConceptPathFinder getConceptPathFinder() {
		return conceptPathFinder;
	}

	public RdfChangeTrackerWrapper getChangeTracker() {
		return changeTracker;
	}

	public FacetedQuery getCurrentQuery() {
		return currentQuery;
	}

	public List<SetSummary> getNumericProperties() {
		return numericProperties;
	}

	public boolean modifyNumericConstraintMakeUnbound(Collection<HLFacetConstraint> hlFacetConstraints, Entry<HLFacetConstraint, Map<Character, Node>> constraintMode, boolean unboundLower) {
		boolean result = false
				;
		final HLFacetConstraint constraint = constraintMode.getKey();
		final Map<Character, Node> constraintModeValue = constraintMode.getValue();

		hlFacetConstraints.remove(constraint);

		final FacetNode facetNode = constraint.mentionedFacetNodes().iterator().next();

		final Node oldLower = constraintModeValue.getOrDefault('>', constraintModeValue.getOrDefault('=', null));
		final Node oldUpper = constraintModeValue.getOrDefault('<', constraintModeValue.getOrDefault('=', null));
		if (unboundLower) {
			if (oldUpper == null) {
				result = false;
			} else {
				result = true;
				facetNode.constraints().range(Range.atMost(new NodeHolder(oldUpper)));
			}
		} else {
			if (oldLower == null) {
				result = false;
			} else {
				result = true;
				facetNode.constraints().range(Range.atLeast(new NodeHolder(oldLower)));
			}
		}
		if (result) {
			//System.out.println(">>>>"+facetNodeRangeEntry);
			constraint.state().removeProperties();
		} else {
			hlFacetConstraints.add(constraint);
		}

		return result;
	}

	public boolean modifyNumericConstraintRandomValue(Collection<HLFacetConstraint> hlFacetConstraints, Entry<HLFacetConstraint, Map<Character, Node>> constraintMode, boolean pickConstant, boolean pickLowerBound, boolean pickUpperBound) {
		boolean result = false;
		final HLFacetConstraint constraint = constraintMode.getKey();
		final Map<Character, Node> constraintModeValue = constraintMode.getValue();

		hlFacetConstraints.remove(constraint);

		final FacetNode facetNode = constraint.mentionedFacetNodes().iterator().next();

		final Entry<FacetNode, Range<NodeHolder>> facetNodeRangeEntry = pickRange(getRandom(), getPseudoRandom(), getNumericProperties(), getConceptPathFinder(),
				facetNode, null, -1, 0, false, true, true);
		if (facetNodeRangeEntry != null) {
			final Range<NodeHolder> range = facetNodeRangeEntry.getValue();
			Node newLower = null;
			Node newUpper = null;
			final Node xLower = range.lowerEndpoint().getNode();
			final Node xUpper = range.upperEndpoint().getNode();
			final Node oldLower = constraintModeValue.getOrDefault('>', constraintModeValue.getOrDefault('=', null));
			final Node oldUpper = constraintModeValue.getOrDefault('<', constraintModeValue.getOrDefault('=', null));
			if (oldLower == null || NodeValue.compare(NodeValue.makeNode(xLower), NodeValue.makeNode(oldLower)) == Expr.CMP_GREATER) {
				newLower = xLower;
				newUpper = oldUpper;
			} else if (oldUpper == null || NodeValue.compare(NodeValue.makeNode(xUpper), NodeValue.makeNode(oldUpper)) == Expr.CMP_LESS) {
				newUpper = xUpper;
				newLower = oldLower;
			} else if (getRandom().nextBoolean()){
				newLower = xLower;
				newUpper = oldUpper;
			} else {
				newLower = oldLower;
				newUpper = xUpper;
			}
			if (NodeValue.compare(NodeValue.makeNode(newLower), NodeValue.makeNode(newUpper)) == Expr.CMP_GREATER) {
				Node tmp = newLower;
				newLower = newUpper;
				newUpper = tmp;
			}
			if (pickConstant || NodeValue.compare(NodeValue.makeNode(newLower), NodeValue.makeNode(newUpper)) == Expr.CMP_EQUAL) {
				facetNode.constraints().eq(newLower);
				result = true;
			} else if (pickUpperBound && pickLowerBound){
				facetNode.constraints().range(Range.closed(new NodeHolder(newLower), new NodeHolder(newUpper)));
				result = true;
			} else if (pickLowerBound) {
				facetNode.constraints().range(Range.atLeast(new NodeHolder(newLower)));
				result = true;
			} else if (pickUpperBound) {
				facetNode.constraints().range(Range.atMost(new NodeHolder(newUpper)));
				result = true;
			} else {
				result = false;
			}
		}
		if (result) {
			//System.out.println(">>>>"+facetNodeRangeEntry);
			constraint.state().removeProperties();
		} else {
			hlFacetConstraints.add(constraint);
		}
		return result;
	}

	public boolean modifyNumericConstraintRandom(Collection<HLFacetConstraint> hlFacetConstraints, Map<HLFacetConstraint, Map<Character, Node>> numericConstraints, boolean pickConstant, boolean pickLowerBound, boolean pickUpperBound) {
		final List<Entry<HLFacetConstraint, Map<Character, Node>>> entryList = new ArrayList<>(numericConstraints.entrySet());
		shuffle(entryList);
		if (entryList.isEmpty()) {
			return false;
		}
		final Entry<HLFacetConstraint, Map<Character, Node>> constraintMode = entryList.get(0);


		return modifyNumericConstraintRandomValue(hlFacetConstraints, constraintMode, pickConstant, pickLowerBound, pickUpperBound);
	}
}