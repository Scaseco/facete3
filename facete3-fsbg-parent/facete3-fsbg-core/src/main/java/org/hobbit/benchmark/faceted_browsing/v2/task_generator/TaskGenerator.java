package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import static java.lang.Math.log;
import static java.util.Collections.shuffle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.aksw.commons.collections.selector.WeightedSelector;
import org.aksw.commons.collections.selector.WeightedSelectorImmutable;
import org.aksw.commons.collections.selector.WeightedSelectorMutable;
import org.aksw.commons.collections.selector.WeigthedSelectorDrawWithReplacement;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetNodeResource;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetValueCountImpl_;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.changeset.util.RdfChangeTrackerWrapper;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.RDFConnectionEx;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.impl.DataQueryImpl;
import org.aksw.jena_sparql_api.rdf.collections.NodeMapperFromRdfDatatype;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.impl.bidirectional.ConceptPathFinderSystem3;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.NodeHolder;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.views.map.MapFromBinaryRelation;
import org.aksw.jena_sparql_api.utils.views.map.MapFromMultimap;
import org.aksw.jena_sparql_api.utils.views.map.MapVocab;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;
import org.hobbit.benchmark.faceted_browsing.v2.main.RdfWorkflowSpec;
import org.hobbit.benchmark.faceted_browsing.v2.main.SparqlTaskResource;
import org.hobbit.benchmark.faceted_browsing.v2.main.SupplierUtils;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa.Nfa;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa.NfaState;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa.NfaTransition;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa.ScenarioConfig;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.SetSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;


public class TaskGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TaskGenerator.class);

    protected List<SetSummary> numericProperties;

    protected RDFConnection conn;
    protected ConceptPathFinder conceptPathFinder;
    protected Random rand;
    protected Random pseudoRandom;
    protected ScenarioConfig scenarioTemplate;


    protected Consumer<SparqlTaskResource> taskPostProcessor = null;

//protected RdfChangeTracker state;

    protected RdfChangeTrackerWrapper changeTracker;


    protected FacetedQuery currentQuery;


    protected static Duration cpTimeout = Duration.ofSeconds(30);

    public TaskGenerator(ScenarioConfig scenarioTemplate, Random random, RDFConnection conn, List<SetSummary> numericProperties, ConceptPathFinder conceptPathFinder) {
        this.scenarioTemplate = scenarioTemplate;
        this.conn = conn;
        this.numericProperties = numericProperties;
        this.rand = random;
        this.conceptPathFinder = conceptPathFinder;

        resetQueryState();
    }

    public void setTaskPostProcessor(Consumer<SparqlTaskResource> taskPostProcessor) {
        this.taskPostProcessor = taskPostProcessor;
    }

    public void resetQueryState() {
        Model baseModel = ModelFactory.createDefaultModel();
        Model changeModel = ModelFactory.createDefaultModel();

        //RdfChangeTrackerWrapper
        changeTracker = RdfChangeTrackerWrapperImpl.create(changeModel, baseModel);

        Model dataModel = changeTracker.getDataModel();

        // RDF Resource with state
        XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
        //FacetedQueryImpl.initResource(facetedQuery);

        currentQuery = FacetedQueryImpl.create(facetedQuery, conn);
//		try {
//			generateScenario();
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}

    }

    final static ImmutableSet<Class<? extends Expr>> rangeComparisonExprs =
            ImmutableSet.<Class<? extends Expr>>builder()
                    .add(E_GreaterThan.class)
                    .add(E_GreaterThanOrEqual.class)
                    .add(E_LessThan.class)
                    .add(E_LessThanOrEqual.class)
                    .add(E_Equals.class)
                    .build();

    public static Map<HLFacetConstraint<?>, Map<Character, Node>> findExistingNumericConstraints(ConstraintFacade<? extends FacetNode> constraintFacade) {
        Map<HLFacetConstraint<?>, Map<Character, Node>> result = new LinkedHashMap<>();
        for (HLFacetConstraint<?> c : new ArrayList<>(constraintFacade.listHl())) {
            final Collection<FacetNode> facetNodes = c.mentionedFacetNodes().values();
            final FacetNode fn = facetNodes.iterator().next();

            final Expr expr = c.expr();
            final Map<Character, Node> boundsMap = new LinkedHashMap<>();
            if (expr instanceof E_LogicalAnd) {
                final List<Expr> subExprs = ExprUtils.getSubExprs(expr);
                boolean rangeCE = subExprs.stream().allMatch(p -> rangeComparisonExprs.contains(p.getClass()));
                if (rangeCE) {
                    subExprs.stream().forEach(e -> {
                        storeNumericBoundFromExpr((ExprFunction2) e, boundsMap);
                    });

                }
            } else if (rangeComparisonExprs.contains(expr.getClass())) {
                storeNumericBoundFromExpr((ExprFunction2) expr, boundsMap);
            }

            if (!boundsMap.isEmpty()) {
                result.put(c, boundsMap);
            }
        }
        return result;
    }

    public static Map<HLFacetConstraint<?>, List<Node>> findExistingClassConstraints(ConstraintFacade<? extends FacetNode> constraintFacade) {
        return findExistingEqConstraintsOfType(constraintFacade, RDF.type);
    }

    public static Map<HLFacetConstraint<?>, List<Node>> findExistingEqConstraintsOfType(ConstraintFacade<? extends FacetNode> constraintFacade, Resource type) {
        Map<HLFacetConstraint<?>, List<Node>> result = new LinkedHashMap<>();

        for (HLFacetConstraint<?> c : new ArrayList<>(constraintFacade.listHl())) {
            final Collection<FacetNode> facetNodes = c.mentionedFacetNodes().values();
            final FacetNode fn = facetNodes.iterator().next();
            final Expr expr = c.expr();
            if (expr instanceof E_Equals && FacetNodeResource.reachingProperty(fn).equals(type)) {
                logger.debug("found candidate: " + expr + " // " + fn);

                final List<Node> consts = ((E_Equals) expr).getArgs().stream()
                        .filter(p -> p.isConstant() && p.getConstant().isIRI())
                        .map(p -> p.getConstant().getNode())
                        .collect(Collectors.toList());

                if (consts.size() == 1) {
                    if (!result.containsKey(c)) {
                        result.put(c, new LinkedList<>());
                    }
                    result.get(c).add(consts.get(0));
                }

            }
        }

        return result;
    }

    public static void storeNumericBoundFromExpr(ExprFunction2 e, Map<Character, Node> boundsMap) {
        final List<Node> evc = e.getArgs().stream()
                .filter(p -> p.isConstant() && p.getConstant().isLiteral() && p.getConstant().getNode().getLiteralValue() instanceof Number)
                .map(p -> p.getConstant().getNode())
                .collect(Collectors.toList());

        if(!evc.isEmpty()) {
            final Node bound = evc.get(0);
            boundsMap.put(e.getOpName().charAt(0), bound);
        }
    }


    public Callable<SparqlTaskResource> createScenarioQuerySupplier() {

        int scenarioIdxCounter[] = {0};
        Callable<Callable<SparqlTaskResource>> scenarioSupplier = () -> {

            int scenarioId = ++scenarioIdxCounter[0];
            Supplier<SparqlTaskResource> core = this.generateScenario();


            Callable<SparqlTaskResource> taskSupplier = () -> {
                SparqlTaskResource s = null;
                try {
                    s = core.get();
                } catch(Exception e) {
                    logger.warn("Scenario aborted prematurely due to exception", e);
                }
                if (s != null) {
                    // Id of 0 conflicts with the eval module...
                    //int displayId = scenarioIdx + 1;

                    // add scenario id
                    s.addLiteral(FacetedBrowsingVocab.scenarioId, scenarioId);
                }
                return s;

            };

//            logger.info("Generated task:\n" + toString(r.getModel(), RDFFormat.TURTLE_PRETTY));

            return taskSupplier;
        };

        Callable<SparqlTaskResource> querySupplier = SupplierUtils.flatMap(scenarioSupplier);

        return querySupplier;
    }

    public static TaskGenerator autoConfigure(ScenarioConfig config, Random random, RDFConnectionEx conn, boolean useCache) throws Exception {
        TaskGenerator result = autoConfigure(config, random, conn, null, useCache);
        return result;
    }


//	public static TaskGeneratorConfig {
//		Resource getDataSummary();
//		Resource getNumericPropertites();
//	}
//
//
//	public static void autoConfigure(TaskGeneratorConfig baseConfig) {
//
//	}


    public static ScenarioConfig extractScenarioConfig(String uri) {
        Model configModel = RDFDataMgr.loadModel(uri);

        ScenarioConfig result = extractScenarioConfig(configModel);
        return result;
    }

    public static ScenarioConfig extractScenarioConfig(Model configModel) {
        RDFDataMgrEx.execSparql(configModel, "nfa-materialize.sparql");

        Set<Resource> configs = configModel.listResourcesWithProperty(RDF.type, Vocab.ScenarioConfig).toSet();

        Resource configRes = Optional.ofNullable(configs.size() == 1 ? configs.iterator().next() : null)
                .orElseThrow(() -> new RuntimeException("Exactly 1 config required"));

        ScenarioConfig result = configRes.as(ScenarioConfig.class);

        return result;
    }


    /**
     * The auto configuration procedure generates the necessary benchmark artifacts,
     * namely:
     *
     * - properties + classification (whether they are numeric)
     * - path finding summary
     *
     * @param conn
     * @param dataSummary
     * @return
     */
    public static TaskGenerator autoConfigure(ScenarioConfig config, Random random, RDFConnectionEx conn, Model dataSummary, boolean useCache) throws Exception {

        if(config == null) {
            config = extractScenarioConfig("config-all.ttl");
        }

        logger.info("Starting analyzing numeric properties...");
        Model model = new RdfWorkflowSpec()
                .deriveDatasetWithSparql(conn, "analyze-numeric-properties.sparql")
                .cache(useCache)
                .getModel();

        String numericRangeQueryStr = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT  ?p { ?p rdfs:range [ rdfs:subClassOf* xsd:numeric ] }";
        List<SetSummary> numericProperties = SparqlRx.execSelect(() -> QueryExecutionFactory.create(numericRangeQueryStr, model))
            .map(b -> b.getResource("p").as(SetSummary.class))
            .toList()
            .blockingGet();

//		Resource xsdNumeric = ResourceFactory.createResource(XSD.NS + "numeric");
//		List<SetSummary> numericProperties = model.listSubjects()
//
//				.filterKeep(r -> Optional.ofNullable(r.getPropertyResourceValue(RDFS.range))
//						.map(x -> x.hasProperty(RDFS.subClassOf, xsdNumeric)).orElse(false))
//				//.filterKeep() // TODO Filter by numerc property
//				.mapWith(s -> s.as(SetSummary.class))
//				.toList();

//		List<SetSummary> numericProperties = DatasetAnalyzerRegistry.analyzeNumericProperties(conn).toList().blockingGet();
        logger.info("Done analyzing numeric properties");
//		ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();
        ConceptPathFinderSystem system = new ConceptPathFinderSystem3();


        // Use the system to compute a data summary
        // Note, that the summary could be loaded from any place, such as a file used for caching
        if(dataSummary == null) {
//			logger.info("No path finding data summary specified, creating it on demand");
//			dataSummary = system.computeDataSummary(conn).blockingGet();
//			logger.info("Path finding data summary computed");

            logger.info("Creating path finding data summary");

             dataSummary = new RdfWorkflowSpec()
                .deriveDatasetWithFunction(conn, "path-finding-summary", () -> system.computeDataSummary(conn).blockingGet())
                .cache(useCache)
                .getModel();
                logger.info("Created path finding data summary");

//			if (logger.isDebugEnabled()) {
//				final StringWriter sw = new StringWriter();
//				RDFDataMgr.write(sw, dataSummary, RDFFormat.TURTLE_PRETTY);
//				logger.debug("Data Summary: {}", sw.toString());
//			}
        } else {
            logger.info("Using specified path finding data summary");
        }

        logger.info("Loaded " + dataSummary.size() + " triples");
        // Select only the most common relations


//		dataSummary = QueryExecutionFactory.create("CONSTRUCT { ?s ?p ?o . ?s <http://www.w3.org/2002/07/owl#inverseOf> ?x . ?x ?y ?z} WHERE { { SELECT DISTINCT ?s { ?s <http://www.example.org/count> ?c . } ORDER BY DESC(?c) LIMIT 1000 } ?s ?p ?o . OPTIONAL { ?s <http://www.w3.org/2002/07/owl#inverseOf> ?x . ?x ?y ?z } }", dataSummary).execConstruct();
//		//Model subModel = QueryExecutionFactory.create("CONSTRUCT { }", model);
//		logger.info("Reduced to " + dataSummary.size() + " triples");


        // Build a path finder; for this, first obtain a factory from the system
        // set its attributes and eventually build the path finder.
        ConceptPathFinder pathFinder = system.newPathFinderBuilder()
                .setDataSummary(dataSummary)
                .setDataConnection(conn)
                .setShortestPathsOnly(false)
                // Skip path with immediate forward / backward traversals (or vice versa) on the same node
                .addPathValidator(TaskGenerator::rejectZigZagPath)
                .addPathValidator(TaskGenerator::rejectConsecutiveReverseProperty)
                .build();

//		System.out.println("Properties: " + DatasetAnalyzerRegistry.analyzeNumericProperties(conn).toList().blockingGet());
        //Model configModel = RDFDataMgr.loadModel(config);

        TaskGenerator result = new TaskGenerator(config, random, conn, numericProperties, pathFinder);
        return result;
    }

    public static boolean rejectZigZagPath(SimplePath path, P_Path0 contrib) {
        P_Path0 ls = path.lastStep();

        boolean result = ls == null
                ? true
                : !(contrib.getNode().equals(ls.getNode()) && ls.isForward() != contrib.isForward());
//		System.out.println("" + path + " + " + contrib + " = " + result);
        return result;
    }

    public static boolean rejectConsecutiveReverseProperty(SimplePath path, P_Path0 contrib) {
        P_Path0 ls = path.lastStep();

        boolean result = ls == null
                ? true
                : !(contrib.getNode().equals(ls.getNode()) && !ls.isForward() && !contrib.isForward());
        //System.out.println("" + path + " + " + contrib + " = " + result);
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
                                new Triple(Vars.e, MapVocab.key.asNode(), Vars.k),
                                new Triple(Vars.e, MapVocab.value.asNode(), Vars.v))),
                Vars.k, Vars.v));

        Map<RDFNode, RDFNode> result = MapFromMultimap.createView(multimap);
        return result;
    }

    public static void substituteRangesWithRandomValues(Random rand, Model model) {
        // List all resources with min / max attributes

        Set<Resource> candidates =
                model.listResourcesWithProperty(Vocab.min).andThen(model.listResourcesWithProperty(Vocab.max)).toSet();

        for(Resource cand : candidates) {
            Set<Statement> stmts = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.listReverseProperties(cand, null).toSet();

            for(Statement stmt : stmts) {
                Resource s = stmt.getSubject();
                Property p = stmt.getPredicate();
                Resource o = stmt.getObject().asResource();
                Number min = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.getLiteralPropertyValue(o, Vocab.min, Number.class);
                Number max = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.getLiteralPropertyValue(o, Vocab.max, Number.class);


                String dtypeStr = Optional.ofNullable(o.getProperty(ResourceFactory.createProperty("http://www.example.org/type"))).map(oo -> oo.getObject().asResource().getURI()).orElse(null);


                if(min == null && max == null) {
                    // Nothing todo / skip
                } else if(min != null && max != null) {
                    Range<Double> range = Range.closedOpen(min.doubleValue(), max.doubleValue());
    //				Range<Double> r = x.getValue().intersection(Range.closedOpen(0.0, 1.0));
                    double point = range.lowerEndpoint() + rand.nextDouble() * (range.upperEndpoint() - range.lowerEndpoint());

                    RDFNode value;
                    if(dtypeStr != null) {
                        RDFDatatype t = TypeMapper.getInstance().getTypeByName(dtypeStr);
                        Object raw = NodeMapperFromRdfDatatype.toJavaCore(NodeValue.makeDouble(point).asNode(), t);

                    ///String lex = t.unparse(raw);
                         value = model.createTypedLiteral(raw, t);
                    } else {
                        value = model.createTypedLiteral(point);
                    }
                    //Resourceutils.setlit
                    //Node v = t.parse("" + point);


                    s.removeAll(p);
                    o.removeProperties();
                    s.addProperty(p, value);

                    logger.info("Substituted range[min: " + min + ", max: " + max +") -> " + value);// + "(" + + ")");

                } else {
                    throw new RuntimeException("Ranges must be restricted on both ends; " + o + "min: " + min + ", max: " + max);
                }
            }

        }
    }

    public Supplier<SparqlTaskResource> generateScenario() {
        // Maps a chokepoint id to a function that given a faceted query
        // yields a supplier. Invoking the supplier applies the action and yields a runnable for undo.
        // if an action is not applicable, the supplier is null
        Map<String, Callable<Boolean>> cpToAction = new HashMap<>();


        resetQueryState();
        changeTracker.commitChangesWithoutTracking();

        // How to wrap the actions such that changes go into the change Model?

//		addAction(cpToAction, "cp1", TaskGenerator::applyCp1);

        cpToAction.put("cp1", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp1)));
        cpToAction.put("cp2", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp2)));
        cpToAction.put("cp3", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp3)));
        cpToAction.put("cp4", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp4)));
        cpToAction.put("cp5", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp5)));
        cpToAction.put("cp6", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp6)));
        cpToAction.put("cp7", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp7)));
        cpToAction.put("cp8", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp8)));
        cpToAction.put("cp9", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp9)));
//
        cpToAction.put("cp10", this::applyCp10);
//
        cpToAction.put("cp11", wrapWithCommitChanges(bindActionToFocusNode(TaskGenerator::applyCp11)));
        cpToAction.put("cp12", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp12)));
        cpToAction.put("cp13", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp13)));
        cpToAction.put("cp14", wrapWithCommitChanges(bindActionToFocusNode(this::applyCp14)));

        Nfa nfa = scenarioTemplate.getNfa();
        Collection<NfaTransition> transitions = nfa.getTransitions();

        // Report references to unavailable actions from the template
        for(NfaTransition transition : transitions) {
            String key = getTransitionKey(transition);
            if (!cpToAction.containsKey(key)) {
                logger.warn("Ignoring reference to action " + key + " as no implementation was registered");

            }
        }

        // One task can have multiple queries
        int taskIdInScenario[] = {0};


        // Ideally for every task the query id would start at 0,
        // but the eval module currently only supports (scenarioId, queryId)
        int transitionId[] = {0};

        // Create a concrete instance of the scenario configuration template
        ScenarioConfig config =
                scenarioTemplate.inModel(ResourceUtils.reachableClosure(scenarioTemplate))
                .as(ScenarioConfig.class);

        NfaState currentState[] = {config.getNfa().getStartState()};


        substituteRangesWithRandomValues(rand, config.getModel());
        Integer scenarioLength = config.getScenarioLength();
        Objects.requireNonNull(scenarioLength);


        int maxRetries = 3;

        Supplier<Collection<SparqlTaskResource>> tmp = () -> {
            Collection<SparqlTaskResource> r = null;

            int i = ++taskIdInScenario[0];

            if (i < scenarioLength) {

                int attemptCount = 0;
                int retryCount = 3;

                transitionId[0]++;

                while(attemptCount < retryCount) {

                    NfaTransition transition = nextState(cpToAction, currentState[0]);
                    if(transition != null) {

                        String cpName = getTransitionKey(transition);

                        // HACK to parse out the integer id of a cp
                        // Needed for compatibility with the old evaluation module
                        // FIXME Get rid of making assumptions about cp ids
                        if (cpName != null) {
                            String cpSuffix = cpName.substring(2);
                            int cpId = Integer.parseInt(cpSuffix);


                            r = generateQueries(currentQuery.focus());

                            // Add annotations
                            for(SparqlTaskResource s : r) {
                                s
                                    .addLiteral(FacetedBrowsingVocab.transitionId, transitionId[0]) //Integer.toString(i))
                                    .addLiteral(FacetedBrowsingVocab.transitionType, cpId);
                            }

                            try {
                                for(SparqlTaskResource s : r) {
                                    taskPostProcessor.accept(s);
                                }
                            } catch(Exception e) {
                                logger.warn("Task post processing failed ", e);
                                ++attemptCount;

                                if(attemptCount > retryCount) {
                                    throw new RuntimeException("Scenario failed");
                                }

                                continue;
                            }

                            currentState[0] = transition.getTarget();
                            break;
                        }
                    }
                }

                //RDFDataMgr.write(System.out, task.getModel(), RDFFormat.TURTLE_PRETTY);
            }
            return r;
        };


        Supplier<SparqlTaskResource> result =
                SupplierUtils.toSupplier(SupplierUtils.flatMapIterable(tmp::get));//SupplierUtils.flatMap(SupplierUtils.from()

        return result;
    }


    public Collection<SparqlTaskResource> generateQueries(FacetNode focus) {

        List<SparqlTaskResource> result = Arrays.asList(
            generateQuery(currentQuery.focus().availableValues().ordered().limit(1000)), // TODO Probably sort and take a limit
            generateQuery(currentQuery.focus().fwd().facetCounts().ordered().limit(1000)),
            // This order by is somewhat hacky - there is no guarantee its always ?o ...
            generateQuery(currentQuery.focus().fwd().facetValueCounts().ordered().addOrderBy(Vars.o, Query.ORDER_ASCENDING).limit(1000)));

        for(int i = 0; i < result.size(); ++i) {
            result.get(i)
                .addLiteral(FacetedBrowsingVocab.queryId, i + 1)
                .addLiteral(FacetedBrowsingVocab.queryType, i + 1);
        }

        return result;
    }

    public SparqlTaskResource generateQuery(DataQuery<?> dq) {
        Entry<Node, Query> e = dq.toConstructQuery();
        Query q = e.getValue();

        // Convert to select query
        q = OpAsQuery.asQuery(Algebra.compile(q));
        //q.setResultVars();

        SparqlTaskResource result = ModelFactory.createDefaultModel()
                .createResource()
                .as(SparqlTaskResource.class)
                .setSparqlStmtString(Objects.toString(q));

        return result;
    }

    // Nfa nfa,
    public NfaTransition nextState(Map<String, Callable<Boolean>> cpToAction, NfaState current) {
        Collection<NfaTransition> transitions = current.getOutgoingTransitions();

        Map<NfaTransition, Double> weights = transitions.stream()
                .filter(t -> cpToAction.containsKey(getTransitionKey(t)))
                .collect(Collectors.toMap(t -> t, NfaTransition::getWeight));

        WeightedSelector<NfaTransition> actionSelector = WeigthedSelectorDrawWithReplacement.create(weights);

        NfaTransition result = nextAction(cpToAction, actionSelector);

        return result;
    }

    public static String getTransitionKey(NfaTransition transition) {
        String result = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.getLiteralPropertyValue(transition, MapVocab.key, String.class);
        return result;
    }

    /**
     * Returns the applied transition
     *
     * @param cpToAction
     * @param actionSelector
     * @return
     */
    public NfaTransition nextAction(Map<String, Callable<Boolean>> cpToAction, WeightedSelector<NfaTransition> actionSelector) {
        NfaTransition result = null; // the chosen action

        // Reset the available actions after each iteration
        WeightedSelector<NfaTransition> s = actionSelector.clone();

        // Simplest recovery strategy: If an action could not be applied
        // repeat the process and hope that due to randomness we can advance
        int maxRandomRetries = 3;
        for (int j = 0; j < maxRandomRetries && !s.isEmpty(); ++j) {
            int numRetriesRemaining = maxRandomRetries - 1 - j;

            //while(!s.isEmpty()) {
            double w = rand.nextDouble();
            NfaTransition transition = s.sample(w);
            String step = getTransitionKey(transition);
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
                    if(j < maxRandomRetries) {
                        logger.info("Retrying " + numRetriesRemaining + " more times after error applying " + step, e);
                        continue;
                    } else {
                        throw new RuntimeException(e);
                    }
                }

                if (success) {
//					// Commit any changes introduced by the action
//					changeTracker.commitChanges();

                    logger.info("Successfully applied " + step + (Boolean.TRUE.equals(transition.preventUndo()) ? " and preventing undo by committing state " : ""));

                    if(Boolean.TRUE.equals(transition.preventUndo())) {
                        changeTracker.commitChanges();
                    }

//					System.out.println("CAN UNDO: " + changeTracker.canUndo());
                    //chosenActions.add(step);
                    result = transition;
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
                .timeout(cpTimeout.getSeconds(), TimeUnit.SECONDS)
                .blockingGet();

        if (fc != null) {
            // Find a facet value for which the filter does not yet exist


            //fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
            Node p = fc.getPredicate();
            Node o = fc.getValue();

            //fn.step(p, isBwd).one().constraints().eq(o);
            fn.step(p, dir).one().constraints().nodeRange(Range.singleton(new NodeHolder(o))).activate();

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

        boolean useNewPathFinder = true;
        pathSearch.setMaxPathLength(3);

        if(useNewPathFinder) {
            SimplePath path = pathSearch
                    .filter(sp  -> sp.getSteps().stream().allMatch(p -> p.isForward())  && sp.getSteps().size() >= 1 )
                    .shuffle(rand)
                    .exec()
                    .firstElement()
                    .blockingGet();
            if (path != null) {
                fn.walk(SimplePath.toPropertyPath(path)).constraints().exists().activate();
                //fn.fwd(node).one().constraints().exists();

                // Pick one of the facet values
                logger.info("Applying cp2) ");// + fn.root().availableValues().exec().toList().blockingGet());

                result = true;
            }

        } else {

            final List<SimplePath> paths =
                    pathSearch.exec().filter(sp  -> sp.getSteps().stream().allMatch(p ->
                            p.isForward()
                    )  && sp.getSteps().size() >= 1 ).toList().blockingGet();
            shuffle(paths, rand);
            if (!paths.isEmpty()) {
                fn.walk(SimplePath.toPropertyPath(paths.get(0))).constraints().exists().activate();
                //fn.fwd(node).one().constraints().exists();

                // Pick one of the facet values
                logger.info("Applying cp2) " + fn.root().availableValues().exec().toList().blockingGet());

                result = true;
            }
        }

//		Node node = fn.fwd().facets().pseudoRandom(pseudoRandom)
//				.randomOrder()
//				.limit(1).exec().firstElement().map(x -> x.asNode()).blockingGet();

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

        boolean useNewPathFinder = true;

        if(useNewPathFinder) {
            SimplePath path = pathSearch
                .filter(sp  -> sp.getSteps().stream().allMatch(p -> p.isForward())  && sp.getSteps().size() >= 1 )
                .shuffle(rand)
                .exec()
                .firstElement()
                .blockingGet();

            if (path != null && applyEqConstraintOnPathRandom(fn, path, pseudoRandom)) {
                result = true;
            }

        } else {

            pathSearch.setMaxPathLength(3);
            final List<SimplePath> paths =
                    pathSearch.exec().filter(sp  -> sp.getSteps().stream().allMatch(p ->
                            p.isForward()
                    )  && sp.getSteps().size() >= 1 ).toList().blockingGet();
            shuffle(paths, rand);

            if (!paths.isEmpty() && applyEqConstraintOnPathRandom(fn, paths.get(0), pseudoRandom)) {
                result = true;
            }
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
                .timeout(cpTimeout.getSeconds(), TimeUnit.SECONDS)
                .blockingGet();

        //})
                //.exec().toList().blockingGet();
        if (!nodeDoubleMap.isEmpty()) {
            final WeightedSelector<Node> selector = WeightedSelectorImmutable.create(nodeDoubleMap);
            final Node clazz = selector.sample(rand.nextDouble());
            fn.fwd(RDF.type).one().constraints().eq(clazz).activate();

            // Pick one of the facet values
            final List<RDFNode> facets = fn.fwd().facets().exclude(RDF.type).randomOrder().pseudoRandom(pseudoRandom).exec().toList().blockingGet();
            if (!facets.isEmpty()) {
                logger.info("Applying cp4) " + facets.get(0));
                fn.fwd(facets.get(0).asNode()).one().constraints().exists().activate();

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
        boolean result = false;

        // TODO What is the best way to deal with hierarchical data?
        // Probably we need some wrapper object with the two straight forward implementations:
        // fetch relations on demand, and fetch the whole hierarchy once and answer queries from cache

        result = modifyClassConstraintSubClassRandom(fn);
        if (!result) {
            result = applyClassEqConstraintRandom(fn, 2);
        }


        return result;
/*
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
        shuffle(typeConstraints, rand);


        //new HierarchyCoreOnDemand()
        //HierarchyCoreOnDemand.createConceptForRoots(typeConstraints.)

*/
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
        logger.info("Found " + paths.size());// + " paths leading to numeric facets: " + paths);

        for (SimplePath path : paths) {
            FacetNode target = fn.walk(SimplePath.toPropertyPath(path));

            // Dump contstraints
            // System.out.println("DEBUG POINT FOCUS: " + target.query().focus());
            // for(FacetConstraint fc : target.query().constraints()) {
            //	HLFacetConstraint<?> hlfc = new HLFacetConstraintImpl<>(null, target, fc);
            //	System.out.println("DEBUG POINT CONSTRAINT: " + hlfc);
            // }


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
                            //.pseudoRandom(pseudoRandom)
                            .exec()
                            .map(RDFNode::asNode)
                            .toSortedList(NodeUtils::compareRDFTerms)
                            .timeout(cpTimeout.getSeconds(), TimeUnit.SECONDS)
                            .blockingGet();
                }

                for (Node p : ps) {

                    logger.debug("Chose numeric property " + p);
                    //System.out.println("Target: " + target.fwd().facetCounts().exec().toList().blockingGet());

                    // Sample the set of values and create a range constraint from it

                    FacetNode v = target.fwd(p).one();
                    Map<Node, Long> distribution = target.fwd().nonConstrainedFacetValueCounts()//.facetValueCounts()
                            .only(p)
                            //.filter(Concept.parse("?s | FILTER(?s = <" + p.getURI() + ">)"))
                            //.pseudoRandom(pseudoRandom)
                            .exec()
                            .toMap(FacetValueCount::getValue, x -> x.getFocusCount().getCount(), LinkedHashMap::new)
                            .blockingGet();
                    //List<Double> vals = v.availableValues().filter(Concept.parse("?s | FILTER(isNumeric(?s))")).randomOrder()
                    //				.pseudoRandom(pseudoRandom)
                    //				.limit(2).exec().map(nv -> Double.parseDouble(nv.asNode().getLiteralLexicalForm())).toList().blockingGet();

                    // Printing out the whole distribution is usually too large!
                    // FIXME In the future we need to implement a strategy to summarize the distribution
                    logger.debug("# Values: " + distribution.size());

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
                        .exec().map(n -> n.asNode())
                        .timeout(cpTimeout.getSeconds(), TimeUnit.SECONDS)
                        .firstElement()
                        .blockingGet();
            }

            if (p != null) {

                logger.debug("Chose numeric property " + p);
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

                logger.debug("Values: " + distribution.size());

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

            logger.debug("# range candidates: " + cands.size());

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
            logger.debug("Picked candidate: " + cand);
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

            logger.debug("Range: " + resultRange);


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

        logger.debug("Pick: " + r);

        if (r != null) {
            if (!r.getKey().root().constraints().listHl().stream().anyMatch(p -> p.mentionedFacetNodes().values().contains(r.getKey()))
                || allowExisting ) {
                r.getKey().constraints().nodeRange(r.getValue()).activate();
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

        Map<HLFacetConstraint<?>, Map<Character, Node>> numericConstraints =
                TaskGenerator.findExistingNumericConstraints(fn.root().constraints());
        if (!numericConstraints.isEmpty()) {
            final Collection<? extends HLFacetConstraint<?>> hlFacetConstraints = fn.root().constraints().listHl();
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

        Map<HLFacetConstraint<?>, Map<Character, Node>> numericConstraints =
                TaskGenerator.findExistingNumericConstraints(fn.root().constraints());
        if (!numericConstraints.isEmpty()) {
            final Collection<? extends HLFacetConstraint<?>> hlFacetConstraints = fn.root().constraints().listHl();
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
        Map<? extends HLFacetConstraint<?>, Map<Character, Node>> numericConstraints =
                TaskGenerator.findExistingNumericConstraints(fn.root().constraints());
        if (numericConstraints.size() >= 2 && rand.nextInt(10) > 2) {
            final Collection<? extends HLFacetConstraint<?>> hlFacetConstraints = fn.root().constraints().listHl();
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
        Map<HLFacetConstraint<?>, Map<Character, Node>> numericConstraints =
                TaskGenerator.findExistingNumericConstraints(fn.root().constraints());
        if (numericConstraints.size() >= 1 && rand.nextInt(10) >= 2) {
            final Collection<? extends HLFacetConstraint<?>> hlFacetConstraints = fn.root().constraints().listHl();
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
                .timeout(cpTimeout.getSeconds(), TimeUnit.SECONDS)
                .blockingGet();

        if (fwc != null) {
            Node p = fwc.getPredicate();
            logger.debug("cp11 got predicate " + p);

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

    /**
     * Complicated property paths or circles
     * (Choke points 3 and 4 with advanced property paths involved)
     * Applies a class restriction onto a property path
     *
     * @param fn
     */
    public boolean applyCp12(FacetNode fn) {
        // n/a
        boolean result = false;
        result = applyClassEqConstraintRandom(fn, 3);
        return result;
    }

    public boolean applyClassEqConstraintRandom(FacetNode fn, int maxPathLength) {
        return applyPropertyEqConstraint(fn, RDF.type, maxPathLength);
    }

    public boolean applyPropertyEqConstraint(FacetNode fn, Property property, int maxPathLength) {
        boolean result = false;
        final ConceptPathFinder conceptPathFinder = getConceptPathFinder();
        final Concept targetConcept = new Concept(ElementUtils.createElementTriple(Vars.s, property.asNode(), Vars.o), Vars.s);
        final DataQuery<RDFNode> rdfNodeDataQuery = fn.remainingValues();

        final UnaryRelation sourceConcept = rdfNodeDataQuery.baseRelation().toUnaryRelation();
        final PathSearch<SimplePath> pathSearch = conceptPathFinder.createSearch(
                sourceConcept, targetConcept);

        pathSearch.setMaxPathLength(maxPathLength);

        boolean useNewPathFinder = true;

        if(useNewPathFinder) {

            SimplePath simplePath = pathSearch
                    .shuffle(rand)
                    .exec()
                    .firstElement()
                    .blockingGet();

            if(simplePath != null) {
                result = applyPropertyEqConstraintOnPathRandom(fn, simplePath, property, pseudoRandom);
            }

        } else {
            final List<SimplePath> simplePathList = pathSearch.exec().toList().blockingGet();
            shuffle(simplePathList, rand);
            if (!simplePathList.isEmpty()) {
                final SimplePath simplePath = simplePathList.get(0);

                result = applyPropertyEqConstraintOnPathRandom(fn, simplePath, property, pseudoRandom);
            }
        }

        return result;
    }

    public static boolean applyPropertyEqConstraintOnPathRandom(FacetNode fn, SimplePath simplePath, Property property, Random pseudoRandom) {
        boolean result = false;
        final FacetNode walk = fn.walk(simplePath);
        final FacetNode typeNode = walk.fwd(property).one();
        final Maybe<RDFNode> someclazz = typeNode.remainingValues().exclude(OWL.NS + "NamedIndividual")
                .randomOrder().pseudoRandom(pseudoRandom).exec().firstElement();
        final RDFNode clazzNode = someclazz
                .timeout(cpTimeout.getSeconds(), TimeUnit.SECONDS)
                .blockingGet();
        if (clazzNode != null) {
            typeNode.constraints().eq(clazzNode).activate();
            result = true;
        }
        return result;
    }

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

        boolean useNewPathFinder = true;

        if(useNewPathFinder) {
            SimplePath path = pathSearch
                    .filter(sp  -> sp.getSteps().stream().anyMatch(p -> !p.isForward())  && sp.getSteps().size() >= 1 )
                    .shuffle(rand)
                    .exec()
                    .firstElement()
                    .blockingGet();

            if (path != null) {
                result = applyEqConstraintOnPathRandom(fn, path, pseudoRandom);
            }
        } else {
            List<SimplePath> paths =
                    pathSearch.exec().filter(sp  -> sp.getSteps().stream().anyMatch(p ->
                            !p.isForward()
                    )  && sp.getSteps().size() >= 1 ).toList().blockingGet();
            shuffle(paths, rand);

            if (!paths.isEmpty() && applyEqConstraintOnPathRandom(fn, paths.get(0), pseudoRandom)) {
                result = true;
            }
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

    public static boolean applyEqConstraintOnPathRandom(FacetNode fn, SimplePath path, Random pseudoRandom) {
        boolean result = false;
        final FacetNode walk = fn.walk(path);
        final List<RDFNode> objects = walk
                .remainingValues()
                .randomOrder().pseudoRandom(pseudoRandom)
                .exec()
                .toList()
                .timeout(cpTimeout.getSeconds(), TimeUnit.SECONDS)
                .blockingGet();
        //System.out.println(objects);
        if (!objects.isEmpty()) {
            walk.constraints().eq(objects.get(0)).activate();
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

        logger.debug("Pick: " + r);

        if (r != null) {
            r.getKey().constraints().nodeRange(r.getValue()).activate();
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
    public TaskGenerator setRandom(@Nonnull Random random) {
        this.rand = Objects.requireNonNull(random);
        return this;
    }

    public Random getRandom() {
        return this.rand;
    }

    // Pseudo random number generator for things that should actually be randomized by external reasons (such as database random)
    public TaskGenerator setPseudoRandom(Random pseudoRandom) {
        if (pseudoRandom != null) {
            // if the pseudoRandom is enabled, the random should also be pseudo-random
            final long l = pseudoRandom.nextLong();
            if (l == -6519408338692630574L) {
                pseudoRandom = new Random(1234L);
                this.rand = new Random(1000L);
            } else {
                this.rand = new Random(l);
            }
        } else {
            this.rand = new Random();
        }
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
        final HLFacetConstraint<?> constraint = constraintMode.getKey();
        final Map<Character, Node> constraintModeValue = constraintMode.getValue();

        hlFacetConstraints.remove(constraint);

        final FacetNode facetNode = constraint.mentionedFacetNodes().values().iterator().next();

        final Node oldLower = constraintModeValue.getOrDefault('>', constraintModeValue.getOrDefault('=', null));
        final Node oldUpper = constraintModeValue.getOrDefault('<', constraintModeValue.getOrDefault('=', null));
        if (unboundLower) {
            if (oldUpper == null) {
                result = false;
            } else {
                result = true;
                facetNode.constraints().nodeRange(Range.atMost(new NodeHolder(oldUpper))).activate();
            }
        } else {
            if (oldLower == null) {
                result = false;
            } else {
                result = true;
                facetNode.constraints().nodeRange(Range.atLeast(new NodeHolder(oldLower))).activate();
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

    public boolean modifyNumericConstraintRandomValue(Collection<? extends HLFacetConstraint<?>> hlFacetConstraints, Entry<? extends HLFacetConstraint<?>, Map<Character, Node>> constraintMode, boolean pickConstant, boolean pickLowerBound, boolean pickUpperBound) {
        boolean result = false;
        final HLFacetConstraint<?> constraint = constraintMode.getKey();
        final Map<Character, Node> constraintModeValue = constraintMode.getValue();

        //hlFacetConstraints.remove(constraint);
        constraint.deactivate();

        final FacetNode facetNode = constraint.mentionedFacetNodes().values().iterator().next();

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

//			if(oldLower == null && oldUpper == null) {
//				System.out.println("newLower: " + newLower);
//				System.out.println("newUpper: " + newUpper);
//				System.out.println("DEBUG POINT here");
//				//throw new RuntimeException("Should not happen");
//			}

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

            if(newLower != null && newUpper != null) {

                if (NodeValue.compare(NodeValue.makeNode(newLower), NodeValue.makeNode(newUpper)) == Expr.CMP_GREATER) {
                    Node tmp = newLower;
                    newLower = newUpper;
                    newUpper = tmp;
                }
                if (pickConstant || NodeValue.compare(NodeValue.makeNode(newLower), NodeValue.makeNode(newUpper)) == Expr.CMP_EQUAL) {
                    facetNode.constraints().eq(newLower).activate();
                    result = true;
                } else if (pickUpperBound && pickLowerBound){
                    facetNode.constraints().nodeRange(Range.closed(new NodeHolder(newLower), new NodeHolder(newUpper))).activate();
                    result = true;
                } else if (pickLowerBound) {
                    facetNode.constraints().nodeRange(Range.atLeast(new NodeHolder(newLower))).activate();
                    result = true;
                } else if (pickUpperBound) {
                    facetNode.constraints().nodeRange(Range.atMost(new NodeHolder(newUpper))).activate();
                    result = true;
                } else {
                    result = false;
                }
            }
        }
        if (result) {
            //System.out.println(">>>>"+facetNodeRangeEntry);
            constraint.state().removeProperties();
        } else {
            //hlFacetConstraints.add(constraint);
            constraint.activate();
        }
        return result;
    }

    public boolean modifyNumericConstraintRandom(Collection<? extends HLFacetConstraint<?>> hlFacetConstraints, Map<? extends HLFacetConstraint<?>, Map<Character, Node>> numericConstraints, boolean pickConstant, boolean pickLowerBound, boolean pickUpperBound) {
        final List<Entry<? extends HLFacetConstraint<?>, Map<Character, Node>>> entryList = new ArrayList<>(numericConstraints.entrySet());
        shuffle(entryList, rand);
        if (entryList.isEmpty()) {
            return false;
        }
        final Entry<? extends HLFacetConstraint<?>, Map<Character, Node>> constraintMode = entryList.get(0);


        return modifyNumericConstraintRandomValue(hlFacetConstraints, constraintMode, pickConstant, pickLowerBound, pickUpperBound);
    }

    public boolean modifyClassConstraintRandomSubClassValue(Collection<? extends HLFacetConstraint<?>> hlFacetConstraints, List<Node> constraintClasses, HLFacetConstraint<?> hlFacetConstraint) {
        final Path narrowingRelation = PathParser.parse("!eg:x|eg:x", PrefixMapping.Extended);
        ///Path narrowingRelation = new P_Link(RDFS.subClassOf.asNode());
        boolean result = false;
        final Concept broaderClasses = ConceptUtils.createConcept(constraintClasses);
        final FacetNode fn = hlFacetConstraint.mentionedFacetNodes().values().iterator().next();
        logger.debug("fn="+fn);
        {
            //hlFacetConstraints.remove(hlFacetConstraint);
            hlFacetConstraint.deactivate();
        }
        UnaryRelation availableClasses = fn.availableValues().baseRelation().toUnaryRelation();

        final UnaryRelation subClassesRelation = HierarchyCoreOnDemand.createConceptForDirectlyRelatedItems(broaderClasses, narrowingRelation, availableClasses, false);

        DataQuery<Resource> dq = new DataQueryImpl<>(conn, subClassesRelation, null, Resource.class);

        final List<Resource> subClasses = dq.exec().toList().timeout(cpTimeout.getSeconds(), TimeUnit.SECONDS).blockingGet();


        logger.debug("Subclasses: " + subClasses.size());
        final List<NodeHolder> subClassNodes = subClasses.stream().map(c -> new NodeHolder(c.asNode())).collect(Collectors.toList());
        final List<FacetValueCount> fn2_av = fn.parent().fwd().facetValueCounts()
                .only(RDF.type)
                .exec()
                .filter(p -> subClassNodes.contains(new NodeHolder(p.getValue())))
                .toList()
                .blockingGet();
        //logger.debug("Facet Value Counts: {}", fn2_av);
        if (!fn2_av.isEmpty()) {
            final WeightedSelector<Node> subClassSelector = WeightedSelectorImmutable
                    .create(fn2_av, ge -> ge.getValue(), gw -> 1 + log(gw.getFocusCount().getCount()));
            final Node sampleSubClass = subClassSelector.sample(getRandom().nextDouble());
            fn.constraints().eq(sampleSubClass).activate();
            result = true;
        }
        if (!result) {
            hlFacetConstraint.activate();
            //hlFacetConstraints.add(hlFacetConstraint);
            //Collection<?> tmp = fn.root().constraints().listHl();

            // add back the constraint
            //fn.root().constraints().listHl().add(hlFacetConstraint);
        }
        return result;
    }

    public boolean modifyClassConstraintSubClassRandom(FacetNode fn) {
        boolean result = false;
        final Map<HLFacetConstraint<?>, List<Node>> existingClassConstraints = findExistingClassConstraints(fn.constraints());
        final List<Entry<HLFacetConstraint<?>, List<Node>>> classConstraintList = new ArrayList<>(existingClassConstraints.entrySet());
        shuffle(classConstraintList, getRandom());

        logger.debug("classConstraintList="+classConstraintList);
        if (!classConstraintList.isEmpty()) {
            final Entry<HLFacetConstraint<?>, List<Node>> constraintListEntry = classConstraintList.get(0);
            final List<Node> constraintClass = constraintListEntry.getValue();

            final HLFacetConstraint<?> hlFacetConstraint = constraintListEntry.getKey();
            final Collection<? extends HLFacetConstraint<?>> hlFacetConstraints = fn.constraints().listHl();
            result = modifyClassConstraintRandomSubClassValue(hlFacetConstraints, constraintClass, hlFacetConstraint);
        }
        return result;
    }
}





//
//List<Statement> stmts = org.aksw.jena_sparql_api.utils.model.ResourceUtils.listReverseProperties(transition).toList();
//transition.removeProperties();
//configModel.remove(stmts);

//RDFDataMgrEx.execSparql(configModel, "nfa-materialize.sparql");
//
//Set<Resource> configs = configModel.listResourcesWithProperty(RDF.type, Vocab.ScenarioConfig).toSet();
//
//Resource configRes = Optional.ofNullable(configs.size() == 1 ? configs.iterator().next() : null)
//		.orElseThrow(() -> new RuntimeException("Exactly 1 config required"));
//
//
//ScenarioConfig scenarioTemplate = configRes.as(ScenarioConfig.class);

//Model copyModel = ModelFactory.createDefaultModel();
//copyModel.add(scenarioTemplate.getModel());
//
//ScenarioConfig copy = scenarioTemplate.inModel(copyModel).as(ScenarioConfig.class);

//Map<RDFNode, RDFNode> map = viewResourceAsMap(config.getPropertyResourceValue(Vocab.weights));
//
//Map<String, RDFNode> mmm = new MapFromKeyConverter<>(map, new ConverterFromNodeMapperAndModel<>(weightModel, RDFNode.class, new ConverterFromNodeMapper<>(NodeMapperFactory.string)));
////new MapFromValueConverter<>(mmm, converter);
//
//Map<String, Range<Double>> xxx = Maps.transformValues(mmm, n -> n.as(RangeSpec.class).toRange(Double.class));//new MapFromValueConverter<>(mmm, new ConverterFromNode)
////Map<String >
////RangeUtils.
//
//// Derive a concrete map
//Map<String, Double> concreteWeights = xxx.entrySet().stream()
//		.map(x -> {
//			Range<Double> r = x.getValue().intersection(Range.closedOpen(0.0, 1.0));
//			double rr = r.lowerEndpoint() + rand.nextDouble() * (r.upperEndpoint() - r.lowerEndpoint());
//			return Maps.immutableEntry(x.getKey(), rr);
//		})
//		.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
//
//logger.debug("Concrete weights " + concreteWeights);

//WeightedSelectorMutable<String> s = WeightedSelectorMutable.create(concreteWeights);
//
//WeightedSelector<String> actionSelector = WeigthedSelectorDrawWithReplacement.create(concreteWeights);
//
//Range<Double> range = config.getPropertyResourceValue(Vocab.scenarioLength).as(RangeSpec.class).toRange(Double.class);
//int scenarioLength = (int) RangeUtils.pickDouble(range, rand); // TODO Obtain value from config
////scenarioLength = 100;
//logger.debug("Scenario length: " + scenarioLength);

//FacetedQuery fq = FacetedQueryImpl.create(conn);
//fq.connection(conn);

//List<String> chosenActions = new ArrayList<>();
//Stream<SparqlTaskResource> result = IntStream.range(0, scenarioLength)
//		//.mapToObj(i -> )
//		.peek(i -> nextAction(cpToAction, actionSelector))
//		.mapToObj(i -> generateQuery())
//		;
//
//for(int i = 0; i < scenarioLength; ++i) {
//	nextAction(cpToAction, actionSelector);
//}
//
//System.out.println("Chosen actions: " + chosenActions);
//
//return result;