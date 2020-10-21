package org.aksw.facete3.app.shared.concept;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;
import org.aksw.jena_sparql_api.mapper.ObjectQuery;
import org.aksw.jena_sparql_api.mapper.ObjectQueryFromQuery;
import org.aksw.jena_sparql_api.mapper.RootedQuery;
import org.aksw.jena_sparql_api.mapper.RootedQueryImpl;
import org.aksw.jena_sparql_api.rx.AggObjectGraph;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.rx.op.OperatorOrderedGroupBy;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.VarExprListUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;

interface ExprListEval {
    Node eval(ExprList el, Binding binding);
}

public class RootedQueryRx {
    private static final Logger logger = LoggerFactory.getLogger(RootedQueryRx.class);

    public static void main(String[] args) {
        Query base = QueryFactory.create("CONSTRUCT { _:root a ?o } { ?s a ?o }");

        Node rootNode = base.getConstructTemplate().getTriples().get(0).getSubject();

        RootedQuery rq = new RootedQueryImpl(rootNode, new ObjectQueryFromQuery(base));
        rq.getObjectQuery().getIdMapping().put(rootNode, new ExprList(new ExprVar(Vars.s)));

        Dataset ds = RDFDataMgr.loadDataset("https://raw.githubusercontent.com/Aklakan/aklakans-devblog/master/2020-10-20-rdflist/src/main/resources/publications.ttl");

        try (RDFConnection conn = RDFConnectionFactory.connect(ds)) {
            List<RDFNode> rdfNodes = exec(conn, rq).toList().blockingGet();

            Model allInOne = ModelFactory.createDefaultModel();
            for (RDFNode rdfNode : rdfNodes) {
                System.out.println(rdfNode);
                RDFDataMgr.write(System.out, rdfNode.getModel(), RDFFormat.TURTLE_BLOCKS);

                allInOne.add(rdfNode.getModel());
            }

            System.out.println("Single Model");
            RDFDataMgr.write(System.out, allInOne, RDFFormat.TURTLE_BLOCKS);
        }

    }

    public static Flowable<RDFNode> exec(
            SparqlQueryConnection conn,
            RootedQuery rootedQuery) {
        return exec(conn, rootedQuery, GraphFactory::createDefaultGraph);
    }

    public static Flowable<RDFNode> exec(
            SparqlQueryConnection conn,
            RootedQuery rootedQuery,
            Supplier<Graph> graphSupplier) {
        return exec(conn, rootedQuery, GraphFactory::createDefaultGraph, RootedQueryRx::evalToNode);
    }

    public static Flowable<RDFNode> exec(
            SparqlQueryConnection conn,
            RootedQuery rootedQuery,
            Supplier<Graph> graphSupplier,
            ExprListEval evalFn) {

        ObjectQuery objectQuery = rootedQuery.getObjectQuery();

        Node root = rootedQuery.getRootNode();

        Query selectQuery = objectQuery.getRelation().toQuery();
        Set<Var> requiredVars = getRequiredVars(objectQuery);

        List<Var> partitionVars;
        Function<Binding, Node> keyToNode;

        if (root.isVariable()) {
            Var rootVar = (Var)root;
            partitionVars = Collections.singletonList(rootVar);
            // pkExprs = new ExprList(new ExprVar(rootVar));
            keyToNode = b -> b.get(rootVar);
        } else if (root.isBlank()) {
            // The root node must be mapped to ids
            // TODO Currently the limitation is that the mapping must be a list of vars rather than arbitrary expressions
            ExprList el = objectQuery.getIdMapping().get(root);
            Objects.requireNonNull(el, "blank node as the root must be mapped to id-generating expressions");

            partitionVars = el.getListRaw().stream()
                    .map(ExprVars::getVarsMentioned)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());

            keyToNode = b -> evalFn.eval(el, b);
        } else {
            // Case where the root node is a constant;
            // unlikely to be useful but handled for completeness
            partitionVars = Collections.emptyList();
            keyToNode = b -> root;
        }

        Query clone = preprocessQueryForPartition(selectQuery, partitionVars, requiredVars, true);

        Aggregator<Binding, Graph> agg = createGraphAggregator(objectQuery, evalFn, graphSupplier);

        Flowable<RDFNode> result = execConstructGrouped(conn::query, agg, clone, partitionVars)
                .map(e -> {
                    Binding b = e.getKey();
                    Graph g = e.getValue();

                    Node rootNode = keyToNode.apply(b);
                    Model m = ModelFactory.createModelForGraph(g);

                    RDFNode r = ModelUtils.convertGraphNodeToRDFNode(rootNode, m);
                    return r;
                });

        return result;
    }


    public static AggObjectGraph createGraphAggregator(
            ObjectQuery objectQuery,
            ExprListEval exprListEval,
            Supplier<Graph> graphSupplier) {

        Map<Node, ExprList> idMap = objectQuery.getIdMapping();

        Map<Node, Function<Binding, Node>> nodeIdGenMap = idMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> (binding -> exprListEval.eval(e.getValue(), binding))));

        AggObjectGraph result = new AggObjectGraph(
                objectQuery.getTemplate(),
                graphSupplier,
                nodeIdGenMap);

        return result;
    }


    /**
     * One of the many ways to create always the same node (equals)
     * from the values obtained by evaluating a list of expressions w.r.t.
     * a given binding.
     *
     * @param exprs
     * @param binding
     * @return
     */
    public static Node evalToNode(ExprList exprs, Binding binding) {
        List<Node> nodes = exprs.getList().stream()
                .map(expr -> ExprUtils.eval(expr, binding))
                .map(NodeValue::asNode)
                .collect(Collectors.toList());

        String label = nodes.toString();

        Node result = NodeFactory.createBlankNode(label);
        return result;
    }


    /**
     * A flowable transformer that groups consecutive items that evaluate to the same key.
     * For every group an accumulator is created that receives the items.
     *
     * @param <ITEM> The incoming item type
     * @param <KEY> The type of the keys derived from the items
     * @param <VALUE> The type of the value accumulated from the items
     * @param itemToKey A function that yiels an item's key
     * @param aggregator An aggregator for computing a value from the set of items with the same key
     * @return
     */
    public static <ITEM, KEY, VALUE> FlowableTransformer<ITEM, Entry<KEY, VALUE>> aggregateConsecutiveItemsWithSameKey(
            Function<? super ITEM, KEY> itemToKey,
            Aggregator<? super ITEM, VALUE> aggregator) {
        return upstream -> upstream
            .lift(OperatorOrderedGroupBy.<ITEM, KEY, Accumulator<? super ITEM, VALUE>>create(
                    itemToKey,
                    groupKey -> aggregator.createAccumulator(),
                    Accumulator::accumulate))
            .map(keyAndAcc -> {
                KEY groupKey = keyAndAcc.getKey();
                Accumulator<? super ITEM, VALUE> accGraph = keyAndAcc.getValue();

                VALUE g = accGraph.getValue();
                return Maps.immutableEntry(groupKey, g);
            });
    }

    public static <T> Flowable<Entry<Binding, T>> execConstructGrouped(
            Function<Query, QueryExecution> qeSupp,
            Aggregator<? super Binding, T> aggregator,
            Query clone,
            List<Var> primaryKeyVars) {

        Function<Binding, Binding> grouper = SparqlRx.createGrouper(primaryKeyVars, false);

        Flowable<Entry<Binding, T>> result = SparqlRx
            // For future reference: If we get an empty results by using the query object, we probably have wrapped a variable with NodeValue.makeNode.
            .execSelectRaw(() -> qeSupp.apply(clone))
            .compose(aggregateConsecutiveItemsWithSameKey(grouper, aggregator));

        return result;
    }


//    public static Query appendToProject(Query query, List<Var> vars) {
//    	query.addProjectVars(vars);
//    }
//
//    public static Query appendToProject(Query query, VarExprList vel) {
//
//    }


    /**
     * Return the sets of variables used in the template and the id mapping.
     *
     *
     * @param objectQuery
     * @return
     */
    public static Set<Var> getRequiredVars(ObjectQuery objectQuery) {
        Template template = objectQuery.getTemplate();

        Set<Var> result = new LinkedHashSet<>();

        Map<Node, ExprList> idMapping = objectQuery.getIdMapping();
        for (ExprList exprs : idMapping.values()) {
            ExprVars.varsMentioned(result, exprs);
        }

        result.addAll(QuadPatternUtils.getVarsMentioned(template.getQuads()));

        return result;
    }

    /**
     * Prepend a given sequence of sort conditions to those
     * already in the query (if there are already any).
     * Duplicate sort conditions are removed in the process
     *
     * @param query
     * @param sortConditions The sort conditions. If null or empty this method becomes a no-op.
     * @return The input query
     */
    public static Query prependToOrderBy(Query query, List<SortCondition> sortConditions) {
        if (sortConditions != null && !sortConditions.isEmpty()) {
            Stream<SortCondition> newConditions;

            if (query.hasOrderBy()) {
                // We need to make a copy using Sets.newLinkedHashSet because we are going to change query.getOrderBy()
                newConditions = Sets.newLinkedHashSet(Iterables.concat(sortConditions, query.getOrderBy())).stream();
                query.getOrderBy().clear();
            } else {
                newConditions = sortConditions.stream();
            }

            newConditions.forEach(query::addOrderBy);
        }

        return query;
    }

    /** Create sort conditions with the given directions from an iterable of {@link Expr}s */
    public static List<SortCondition> createSortConditionsFromExprs(Iterable<Expr> exprs, int dir) {
        List<SortCondition> result = exprs == null
                ? null
                : Streams.stream(exprs)
                    .map(expr -> new SortCondition(expr, dir))
                    .collect(Collectors.toList());
        return result;
    }

    public static List<SortCondition> createSortConditionsFromVars(Iterable<Var> vars, int dir) {
        List<SortCondition> result = vars == null
                ? null
                : Streams.stream(vars)
                    .map(var -> new SortCondition(new ExprVar(var), dir))
                    .collect(Collectors.toList());
        return result;

    }



//    public static Query removeDuplicateSortConditions(Query query) {
//        if (query.hasOrderBy()) {
//            List<SortCondition> orderBy = query.getOrderBy();
//
//            Set<SortCondition> newOrderBy = new LinkedHashSet<>(orderBy);
//            orderBy.clear();
//            orderBy.addAll(newOrderBy);
//        }
//
//        return query;
//    }


    /**
     * Return a SELECT query from the given query where
     * - it is ensured that all primaryKeyVars are part of the projection (if they aren't already)
     * - distinct is applied in preparation to instantiation of construct templates (where duplicates can be ignored)
     * - if sortRowsByPartitionVar is true then result bindings are sorted by the primary key vars
     *   so that bindings that belong together are consecutive
     * - In case of a construct template without variables variable free is handled
     *
     *
     * @param q
     * @param primaryKeyVars
     * @param sortRowsByPartitionVar
     * @return
     */
    public static Query preprocessQueryForPartition(
            Query selectQuery,
            List<Var> primaryKeyVars,
            Set<Var> requiredVars,
            boolean sortRowsByPartitionVars) {

        Query clone = selectQuery.cloneQuery();
        clone.setQueryResultStar(false);

        VarExprList project = clone.getProject();

        VarExprListUtils.addAbsentVars(project, primaryKeyVars);
        VarExprListUtils.addAbsentVars(project, requiredVars);

        // Handle the corner case where no variables are requested
        if (project.isEmpty()) {
            // If the template is variable free then project the first variable of the query pattern
            // If the query pattern is variable free then just use the result star
            Set<Var> patternVars = SetUtils.asSet(PatternVars.vars(clone.getQueryPattern()));
            if(patternVars.isEmpty()) {
                clone.setQueryResultStar(true);
            } else {
                Var v = patternVars.iterator().next();
                clone.setQueryResultStar(false);
                clone.getProject().add(v);
            }
        }

        // clone.setDistinct(true);

        if (sortRowsByPartitionVars) {
            List<SortCondition> newSortConditions = createSortConditionsFromVars(primaryKeyVars, Query.ORDER_DEFAULT);
            prependToOrderBy(clone, newSortConditions);
        }

        logger.debug("Converted query to: " + clone);
        return clone;
    }
}
