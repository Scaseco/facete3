package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.collections.selector.WeightedSelector;
import org.aksw.commons.collections.selector.WeigthedSelectorDrawWithReplacement;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.impl.bidirectional.ConceptPathFinderSystem3;
import org.aksw.jenax.arq.util.expr.ExprListUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.extra.RDFConnectionEx;
import org.aksw.jenax.connection.extra.RDFConnectionFactoryEx;
import org.aksw.jenax.connection.extra.RDFConnectionMetaData;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.util.NodeCmp;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa.ScenarioConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.eccenca.access_control.triple_based.core.ElementTransformTripleRewrite;
import com.eccenca.access_control.triple_based.core.GenericLayer;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.primitives.Ints;

import io.reactivex.rxjava3.core.Flowable;

public class MainCliDiceBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(MainCliDiceBenchmark.class);


    public static void main(String[] args) throws Exception {
        // HACK/WORKAROUND for Jcommander issue
        // https://github.com/cbeust/jcommander/issues/464
        // Add a dummy element to initialize a list property
        args = ObjectArrays.concat(new String[] {"-d", "foo"}, args, String.class);

        JenaPluginUtils.registerResourceClasses(
                CommandMain.class,
                RDFConnectionMetaData.class);

        JenaPluginUtils.scan(ScenarioConfig.class);
        CommandMain cmMain = ModelFactory.createDefaultModel().createResource().as(CommandMain.class);

        JCommander jc = JCommander.newBuilder().addObject(cmMain).build();
        jc.parse(args);

        if(cmMain.getHelp()) {
            jc.usage();
            return;
        }

        String sparqEndpoint = cmMain.getSparqlEndpoint();

        DatasetDescription datasetDescription = new DatasetDescription();
        datasetDescription.addAllDefaultGraphURIs(cmMain.getDefaultGraphUris());


        try(RDFConnectionEx conn = RDFConnectionFactoryEx.connect(sparqEndpoint, datasetDescription)) {
            allocateAllowedPredicates(conn);
        }
    }



    public static Map<Node, Map<Node, Long>> index(Collection<FacetValueCount> facetValueCounts) {
        Map<Node, Map<Node, Long>> result = facetValueCounts.stream()
                .collect(Collectors.groupingBy(FacetValueCount::getPredicate,
                        Collectors.groupingBy(FacetValueCount::getValue, Collectors.summingLong(x -> x.getFocusCount().getCount()))));

        return result;
    }

    public static Map<Node, Long> indexFc(Collection<FacetCount> facetCounts) {
        Map<Node, Long> result = facetCounts.stream()
                .collect(Collectors.toMap(
                        FacetCount::getPredicate,
                        fc -> fc.getDistinctValueCount().getCount()));

        return result;
    }


    public static Map<Node, Long> index(Map<Node, Map<Node, Long>> poc) {
        Map<Node, Long> result = poc.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> e.getValue().values().stream().mapToLong(Long::longValue).sum()));

        return result;
    }

    public static <K, V, E extends Entry<K, V>> Comparator<E> entryComparator(Comparator<? super K> keyComparator, Comparator<? super V> valueComparator) {
        return (a, b) -> ComparisonChain.start()
            .compareTrueFirst(a.getKey() == null, b.getKey() == null)
            .compare(a.getKey(), b.getKey(), keyComparator)
            .compare(a.getValue(), b.getValue(), valueComparator)
            .result();
    }

//	public static TreeMultimap<Long, Entry<Node, Node>> newRdfTreeMultimap() {
//		return TreeMultimap.create(Ordering.natural(), entryComparator(NodeUtils::compareRDFTerms, NodeUtils::compareRDFTerms));
//	}

    public static class Chooser<E> {

        protected Random rand;
        protected int numPicks;
        protected Comparator<? super E> comparator;


        public Chooser(Comparator<? super E> comparator, Random random, int numPicks) {
            this.rand = random;
            this.numPicks = numPicks;
            this.comparator = comparator;
        }

//		public static <K, V> Entry<K, V> nearestKey(NavigableMap<K, V> map, K proto, Function<? super K, Long> distance) {
//			K a = map.ceilingKey(proto);
//			K b = map.floorKey(proto);
//
//			if(a == null)
//		}

        public static <K, V> Entry<K, V> nearestEnty(NavigableMap<K, V> map, K proto, BiFunction<? super K, ? super K, Long> distance) {
            Entry<K, V> a = map.floorEntry(proto);
            Entry<K, V> b = map.ceilingEntry(proto);

            long da = a == null ? Long.MAX_VALUE : distance.apply(proto, a.getKey());
            long db = b == null ? Long.MAX_VALUE : distance.apply(b.getKey(), proto);

            Entry<K, V> result = da <= db ? a : b;
            return result;
        }

        public Set<E> choose(Map<E, Long> pc) {


            // Pick n properties
            double scale = 0.1;
//			int numPicks = 10;

//			Random rand = new Random(1000);

            // Each iteration adjust the weights, so that properties with totalSize / n have greatest weight
            // First,

            // With no property contributing more than m triples
            long maxContribSize = (long)(100000 * scale);
            long minContribSize = (long)(maxContribSize * 0.1);

            // Amounting to at most triples
            long totalSize = (long)(1000000 * scale);

            // A result is acceptable if its total sum is plus/mi tolerance off
            double totalTolerance = 0.1;



//			long minTotalSize = totalSize;

            long minTotalSize = (long)(totalSize * (1.0 - totalTolerance));
            long maxTotalSize = (long)(totalSize * (1.0 + totalTolerance));

            //Map<E, Long> pc = null;

            Supplier<TreeMultimap<Long, E>> mmSupplier = () -> TreeMultimap.create(Ordering.natural(), comparator);

            TreeMultimap<Long, E> ipc = Multimaps.invertFrom(Multimaps.forMap(pc), mmSupplier.get());

            // Get the actual most and least frequent property values
            long from = ipc.asMap().firstKey();
            long to = ipc.asMap().lastKey();


            // Adjust the range of the map to the desired range
            from = Math.max(from, minContribSize);
            to = Math.min(to, maxContribSize);


            long totalChosenSize = 0;
            Set<E> chosen = new LinkedHashSet<>();


            for(int i = 0; i < (numPicks * 2); ++i) {
                int chosenSize = chosen.size();


                // if we are at the last pick but have not reached the acceptance threshold,
                // remove the smallest pick and pick again
                if(chosenSize == numPicks) {
                        E removalItem = null;
                        if(totalChosenSize < minTotalSize) {
                            removalItem = chosen.stream().sorted((a, b) -> Ints.saturatedCast(pc.get(a) - pc.get(b))).findFirst().orElse(null);
                        } else if(totalChosenSize > maxTotalSize) {
                            removalItem = chosen.stream().sorted((a, b) -> Ints.saturatedCast(pc.get(b) - pc.get(a))).findFirst().orElse(null);
                        }

                        if(removalItem != null) {
                            long reduction = pc.get(removalItem);
                            totalChosenSize -= reduction;
                            chosen.remove(removalItem);
                            --chosenSize;
                        } else {
                            break;
                        }
//						else {
//							throw new RuntimeException("Should not happen");
//						}


                }

                int remainingIterations = numPicks - chosenSize;

                long remainingSize = totalSize - totalChosenSize / remainingIterations;


                double desiredMean = remainingSize / (double)remainingIterations;
                //double stdev = Math.abs(remainingSize * 0.1); //totalSize / numPicks;//(double)remainingIterations;


                // Set the mean to the actual value closest to it
                Entry<Long, Collection<E>> tmpMean = Chooser.nearestEnty(ipc.asMap(), (long)desiredMean, (a, b) -> a.longValue() - b.longValue());
                double mean = tmpMean != null ? (double)tmpMean.getKey() : desiredMean;

                double stdev = 0.1 * mean;

                //System.out.println("Mean: " + mean + " - stdev: " + stdev);

//				Function<Double, Double> pmf = new Gaussian(mean, stdev)::value;
                Function<Double, Double> pmf = v -> {
                    //System.out.println("mean=" + mean + " value=" + v);
                    double r = new Gaussian(1.0, mean, stdev).value(v);
//						System.out.println("mean=" + mean + " value=" + v + " -> " + r);
                    return r;
                };


                long span = to - from;

                //double bucketSpan = span / (double)numPicks;
                // Cut off the part of the map with maxTargetSize
                NavigableMap<Long, Collection<E>> tmp = ipc.asMap();
                tmp = tmp.tailMap(from, true);
                tmp = tmp.headMap(to, true);
//				tmp.headMap(maxTotalSize, true);


                ipc = mmSupplier.get(); //newRdfTreeMultimap();
                for(Entry<Long, Collection<E>> e : tmp.entrySet()) {
                    ipc.putAll(e.getKey(), e.getValue());
                }

                Map<E, Double> nodeToWeight = ipc.entries().stream()
                        .collect(Collectors.toMap(Entry::getValue, e -> pmf.apply(e.getKey().doubleValue())));


                WeightedSelector<E> selector = WeigthedSelectorDrawWithReplacement.create(nodeToWeight);


//				logger.info("Adjusted map to frequencies " + from + " - " + to + " with " + ipc.size() + "entries");
//
//				Map<Long, List<Entry<Long, Node>>> buckets = ipc.entries().stream()
//						.collect(Collectors.groupingBy(
//								e -> Long.valueOf((long)(e.getKey() * bucketSpan)), Collectors.toList()));
//
//
//				ipc.asMap().entrySet().forEach(x -> System.out.println(x.getValue().size() < 10 ? "" + x : "" + x.getKey() + ": " + x.getValue().size()));
//

                double value = rand.nextDouble();
                Entry<E, ? extends Number> e = selector.sampleEntry(value);
                E item = e.getKey();
                long sizeContrib = pc.get(item); //e.getValue().longValue();

                System.out.println("Selected " + e + " - " + sizeContrib);

//				if(sizeContrib > totalSize) {
//					continue;
//				}

                totalChosenSize += sizeContrib;
                chosen.add(item);
            }


            System.out.println("Chose: " + totalChosenSize);


            return chosen;
        }
    }

    public static Query toQuery(Collection<Entry<Node, Node>> es) {
        Expr expr = toExpr(Vars.p, Vars.o, es);

        Query result = new Query();
        result.setQuerySelectType();
        result.addProjectVars(Arrays.asList(Vars.s, Vars.p, Vars.o));

        result.setQueryPattern(
                ElementUtils.groupIfNeeded(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o),
                new ElementFilter(expr)));

        return result;
    }

    public static Expr toExpr(Var p, Var o, Collection<Entry<Node, Node>> es) {
        Map<Node, Set<Node>> grouped = es.stream().collect(
            Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toSet())));


        // if a set contains a null value, remove all non-null values from it
        for(Entry<Node, Set<Node>> e : grouped.entrySet()) {
            Set<Node> set = e.getValue();
            if(set.contains(null)) {
                e.setValue(null);
//				set.clear();
//				set.add(null);
            }
        }

        ExprVar pe = new ExprVar(p);
        ExprVar oe = new ExprVar(o);

        List<Expr> ors = new ArrayList<>();
        for(Entry<Node, Set<Node>> e : grouped.entrySet()) {
            Node k = e.getKey();
            Set<Node> v = e.getValue();

            List<Expr> ands = new ArrayList<>();

            if(k != null) {
                ands.add(new E_Equals(pe, NodeValue.makeNode(k)));
            }

            if(v != null) {
                ands.add(new E_OneOf(oe, ExprListUtils.nodesToExprs(v)));
            }

            if(!ands.isEmpty()) {
                Expr tmp = ExprUtils.andifyBalanced(ands);
                ors.add(tmp);
            }
        }

        Expr result = ExprUtils.orifyBalanced(ors);
        return result;
    }


    public static void allocateAllowedPredicates(RDFConnectionEx conn) throws Exception {

//		double f = 1000.0;
//		Function<Double, Double> pmf = new Gaussian(1, 5000, 500)::value;
//		System.out.println(pmf.apply(0.0 * f));
//		System.out.println(pmf.apply(2.5 * f));
//		System.out.println(pmf.apply(5.0 * f));
//		System.out.println(pmf.apply(7.5 * f));
//		System.out.println(pmf.apply(10.0 * f));
//
//		System.out.println(pmf.apply(5050.0));
//
//		if(true) { return; }

        // Split properties into numBuckets by frequency
        //int numBuckets = 5;




        FacetedQuery fq = FacetedQueryImpl.create(conn);

        Entry<Node, Query> facetFocusCountQuery = fq.focus().fwd().facetFocusCounts(false).toConstructQuery();
        System.out.println("Facetcount query: " + facetFocusCountQuery);
        List<FacetCount> facetFocusCounts = new RdfWorkflowSpec()
                .execFlowable(conn, facetFocusCountQuery)
                .cache(true)
                .getModel()
                .map(r -> r.as(FacetCount.class))
                .toList().blockingGet();


        logger.info("Computing facet counts");

        Entry<Node, Query> fcq = fq.focus().fwd().facetCounts().toConstructQuery();
        logger.debug("Facetcount query: " + fcq);
        Flowable<FacetCount> facetCounts = new RdfWorkflowSpec()
                .execFlowable(conn,fcq)
                .cache(true)
                .getModel()
                .map(r -> r.as(FacetCount.class));

        //facetCounts.toList().blockingGet();


        Entry<Node, Query> facetValueCountQuery = fq.focus().fwd().facetValueCounts().only(RDF.type).toConstructQuery();

//		System.out.println(qq);
        logger.debug("Done computing facet counts");
        // Actually we want to cache the result as a stream...
        // ReactiveSparqlUtils.

        logger.info("Computing facet value counts");

        List<FacetValueCount> facetValueCounts = new RdfWorkflowSpec()
            .execFlowable(conn, facetValueCountQuery)
            .cache(true)
            .getModel()
            .map(r -> r.as(FacetValueCount.class))
            .toList()
            .blockingGet();

        logger.debug("Done computing facet value counts");

        // predicate to objects to counts


        // TODO This blockingGet in map is ugly - get rid of it!
    //	Map<Node, Map<Node, Long>> poc = facetValueCounts
    //			.groupBy(FacetValueCount::getPredicate)
    //			.map(g -> Maps.immutableEntry(g.getKey(), g.toMap(FacetValueCount::getValue, x -> x.getFocusCount().getCount()).blockingGet()))
    //			.toMap(Entry::getKey, Entry::getValue)
    //			.blockingGet();


        //Map<Node, Map<Node, Long>> poc = index(facetValueCounts.toList().blockingGet());
        //Map<Node, Long> pc = index(poc);

        Map<Node, Long> pcx = indexFc(facetFocusCounts);


        // Map normal predicates p to pairs with (p, null)
        Map<Entry<Node, Node>, Long> pFreqs = pcx.entrySet().stream()
                .collect(Collectors.toMap(e -> Maps.immutableEntry(e.getKey(), null), Entry::getValue));

        Map<Entry<Node, Node>, Long> poFreqs = facetValueCounts.stream()
                .collect(Collectors.toMap(
                        e -> Maps.immutableEntry(e.getPredicate(), e.getValue()),
                        e -> e.getFocusCount().getCount()));

        Map<Entry<Node, Node>, Long> freqencies = new LinkedHashMap<>();
        freqencies.putAll(pFreqs);
        freqencies.putAll(poFreqs);

        // Should we divide the ordered set? This would give buckets of same size, but possibly
        // vastly different sizes within a basket
        // So we should really paritien by count
        //% numBucket


//		List<WeightedSelector<Node>> buckets = IntStream.range(0, numBuckets)
//				.mapToObj(() -> new W

//		for(Entry<Long, Node> e : ipc.entries()) {
//
//		}


        // Split the properties into bins based on their frequency
        int numPicks = 10;
        Chooser<Entry<Node, Node>> chooser = new Chooser<>(entryComparator(NodeCmp::compareRDFTerms, NodeCmp::compareRDFTerms), new Random(0), numPicks);
        Set<Entry<Node, Node>> chosen = chooser.choose(freqencies);


        for(Entry<Node, Node> item : chosen) {
            System.out.println(item + ": " + freqencies.get(item));
        }


        Query view = toQuery(chosen);
//		GenericLayer layer = GenericLayer.create(RelationUtils.fromQuery("SELECT ?s ?p ?o { ?s ?p ?o FILTER(!STRSTARTS(STR(?p), 'http://dbpedia.org/property/') )}"));
        GenericLayer layer = GenericLayer.create(FragmentUtils.fromQuery(view));


        Query query = QueryFactory.create("SELECT (COUNT(*) AS ?c) { ?x ?y ?z }");
//		"SELECT DISTINCT ?p1 ?p2 ?p3 ?p4 (COUNT(DISTINCT ?s) AS ?cs) (COUNT(DISTINCT ?o1) AS ?c1) (COUNT(DISTINCT ?o2) AS ?c2) (COUNT(DISTINCT ?o3) AS ?c3) (COUNT(DISTINCT ?o4) AS ?c4){\n" +
//		"  ?s a <http://dbpedia.org/ontology/MusicalArtist> .\n" +
//		"  ?s  ?p1 ?o1 .\n" +
//		"  ?o1 ?p2 ?o2 .\n" +
//		"  ?o2 ?p3 ?o3 .\n" +
//		"  ?o3 ?p4 ?o4 .\n" +
//		"} GROUP BY ?p1 ?p2 ?p3 ?p4");

        Query q = ElementTransformTripleRewrite.transform(query, layer, true);
        q = QueryUtils.rewrite(q, AlgebraUtils.createDefaultRewriter()::rewrite);

        System.out.println(q);
        Integer count = ServiceUtils.fetchInteger(conn.query(q), Vars.c);
        System.out.println("Counted: " + count);

    //	for(Entry<Long, Collectio> e : ipc.entrySet()) {
    //		System.out.println(e);
    //	}


                //.toMap(GroupedFlowable::getKey);

            //.toMultimap(r -> r.getPredicate(), r -> ((Number)r.getValue().getLiteralValue()).longValue());


//		facetValueCounts.blockingForEach(x -> System.out.println(x));
        // Now query by the model by the template pattern

        // So certainly we can start with some set of resources - maybe configurable via nfa
        // And then?
        // We could
        // - use the schema path finder to find all paths of length 3

        logger.info("Done analyzing numeric properties");
//		ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();
        ConceptPathFinderSystem system = new ConceptPathFinderSystem3();

//		Model dataSummary = new RdfWorkflowSpec()
//			.deriveDatasetWithFunction(conn, "path-finding-summary", () -> system.computeDataSummary(conn).blockingGet())
//			.cache(true)
//			.getModel();
//			logger.info("Created path finding data summary");
//
//		ConceptPathFinder pathFinder = system.newPathFinderBuilder()
//			.setDataConnection(conn)
//			.setDataSummary(dataSummary)
//			.setShortestPathsOnly(true)
//			.setSimplePathsOnly(true)
//			.build();
//		logger.info("Searching for paths");
//
//		List<SimplePath> paths = pathFinder.createSearch(Concept.parse("?s | ?s a <http://dbpedia.org/ontology/MusicalArtist>"), Concept.parse("?s | ?s ?p ?o"))
//			.setMaxLength(3l)
//			.exec()
//			.toList()
//			.blockingGet();
//		System.out.println(paths.size());
//
        // So what is the goal of this test?
        // I think the first test does not need to consider connectivity
        // Its just about taking a set of predicates / predicate values such that a certain
        // dataset size is obtained
        //   Note: We could also exclude items
        // Operations:
        //   Chose / Add / Remove property
        //   Chose / Add / Remove property+value

        // Or maybe we just exclude values

        // Start:
        // Decide whether to dice by a predicate or (predicate, object) pair


        // Well, but the next step is then to run faceted queries on the selected dice
        // so connectivity then *is* an issue...


    //	System.out.println();
    //
    //	Map<CountInfo, Collection<Node>> map = fq.focus().fwd().facetFocusCounts(false).exec()
    //		.toMultimap(FacetCount::getDistinctValueCount, FacetCount::getPredicate, () -> new TreeMap<>(Ordering.natural().reversed()))
    //		.blockingGet();

    //	map.entrySet().forEach(x -> System.out.println(x.getValue().size() < 10 ? "" + x : "" + x.getKey() + ": " + x.getValue().size()));


    //	Query query = QueryFactory.create("SELECT * { ?s a ?t . ?t ?y  ?z . FILTER(?y = <http://www.example.org/foo> && ?z = <http://www.example.org/bar>)}");
//		Query query = QueryFactory.create(
//				"SELECT DISTINCT ?p1 ?p2 ?p3 ?p4 (COUNT(DISTINCT ?s) AS ?cs) (COUNT(DISTINCT ?o1) AS ?c1) (COUNT(DISTINCT ?o2) AS ?c2) (COUNT(DISTINCT ?o3) AS ?c3) (COUNT(DISTINCT ?o4) AS ?c4){\n" +
//				"  ?s a <http://dbpedia.org/ontology/MusicalArtist> .\n" +
//				"  ?s  ?p1 ?o1 .\n" +
//				"  ?o1 ?p2 ?o2 .\n" +
//				"  ?o2 ?p3 ?o3 .\n" +
//				"  ?o3 ?p4 ?o4 .\n" +
//				"} GROUP BY ?p1 ?p2 ?p3 ?p4");

//		q = DataQueryImpl.rewrite(q, DataQueryImpl.createDefaultRewriter()::rewrite);

        //System.out.println(q);
        //return
    }

}
