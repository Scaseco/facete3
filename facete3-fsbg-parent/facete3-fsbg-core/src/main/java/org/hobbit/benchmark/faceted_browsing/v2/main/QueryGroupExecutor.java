package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.VarExprListUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.table.TableN;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.PatternVars;


/**
 *
 *
 * @author raven
 *
 */
public class QueryGroupExecutor {

    // TODO Move to VarExprList utils
    /**
     * "Smart" addition that suppresses identity mappings
     *
     * @param vel
     * @param v
     * @param e
     * @return
     */
    public static VarExprList smartAdd(VarExprList vel, Var v, Expr e) {
        if(e instanceof ExprVar) {
            ExprVar ev = (ExprVar)e;
            Var evv = ev.asVar();

            if(Objects.equals(v, evv)) {
                e = null;
            }
        }

        if(e == null) {
            vel.add(v);
        } else {
            vel.add(v, e);
        }

        return vel;
    }

    /**
     * Remove the expr side of identity mappings such as (?g AS ?g)
     *
     * @param query
     * @param v
     * @param e
     */
    public static VarExprList cleanIdentity(VarExprList result) {
        VarExprList cpy = new VarExprList();
        closure(result).forEachExpr((v, e) -> smartAdd(cpy, v, e));
        result.clear();
        VarExprListUtils.copy(cpy, result);

        return result;
    }

    public static void main(String[] args) {
        Model m = RDFDataMgr.loadModel("path-data.ttl");
        try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(m))) {

            //Query baseQuery = QueryFactory.create("SELECT ?g1 ?g2 (COUNT(DISTINCT ?g3) AS ?g4) { ?g1 ?g2 ?g3 } GROUP BY ?g1 ?g2");
            Query baseQuery = QueryFactory.create("SELECT ?p (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?p");

            Query groupQuery = createQueryGroup(baseQuery);
            System.out.println(groupQuery);

            Table groups;
            try(QueryExecution qe = conn.query(groupQuery)) {
                groups = TableFactory.create(new QueryIteratorResultSet(qe.execSelect()));
            }

            Query splitQuery = splitByGroupBy(baseQuery, groups, 1);
            System.out.println(splitQuery);


            System.out.println(groups);

            try(QueryExecution qe = conn.query(splitQuery)) {
                System.out.println(ResultSetFormatter.asText(qe.execSelect()));
            }
        }
    }


    // TODO Move to QueryUtils
    public static Set<Var> varsMentioned(Query query) {
        Set<Var> result = new HashSet<>();
        result.addAll(PatternVars.vars(query.getQueryPattern()));
        result.addAll(VarUtils.toList(query.getResultVars()));
        result.addAll(Optional.ofNullable(query).map(Query::getValuesVariables).orElse(Collections.emptyList()));
        return result;
    }


    // TODO Move to QueryUtils
    /**
     *
     * Create a union from multiple SELECT queries.
     * Combines their projections; does not rename any variables
     *
     * @param queries
     * @return
     */
    public static Query union(Iterable<Query> queries) {
        Query result = new Query();
        result.setQuerySelectType();
        Set<String> resultVars = new LinkedHashSet<>();
        List<Element> resultElements = new ArrayList<>();

        for(Query q : queries) {
            resultVars.addAll(q.getResultVars());

            Element e = !(q.hasAggregators() && q.hasGroupBy() && q.hasValues() && q.hasHaving() && q.hasOffset() && q.hasLimit())
                    ? q.getQueryPattern()
                    : new ElementSubQuery(q);
            resultElements.add(e);
        }
        resultVars.forEach(result::addResultVar);
        Element e = ElementUtils.unionIfNeeded(resultElements);
        result.setQueryPattern(e);

        return result;
    }


    /**
     * Add mappings of the form (v, new ExprVar(v)) for each variable v not associated with an expression
     *
     * @param vel
     * @return
     */
    public static VarExprList closure(VarExprList vel) {
        // Prevent concurrent modification
        List<Var> vs = new ArrayList<>(vel.getVars());
        for(Var v : vs) {
            Expr e = vel.getExpr(v);
            if(e == null) {
                vel.add(v, new ExprVar(v));
            }
        }

        return vel;
    }

    /**
     * Get 'simple' group vars - i.e. instances of ExprVar
     * @param query
     * @return
     */
    public static Optional<List<Var>> getSimpleGroupVars(Query query) {
        // TODO Do not throw the exception
        List<Var> tmp = closure(query.getGroupBy()).getExprs().values().stream()
            .peek(x -> { if(!(x instanceof ExprVar)) throw new RuntimeException("Only ExprVars allowed in group by"); })
            .map(x -> ((ExprVar)x).asVar())
            .collect(Collectors.toList());

        return Optional.of(tmp);
    }

    public static VarExprList createWithIdentityMapping(Iterable<Var> vars) {
        VarExprList result = new VarExprList();
        vars.forEach(v -> result.add(v, new ExprVar(v)));
        return result;
    }


    /**
     * Given a query with group by and aggregators,
     * create a new query that only yields the group keys
     *
     * The idea is to be able to perform subsequent limited counts for these group keys.
     * However, the expectation is, that retrieving the distinct group keys
     * implies a scan over exactly the same set of tuples that otherwise be aggregated, so
     * nothing is gained by separating this.
     *
     * @param query
     * @return
     */
    public static Query createQueryGroup(Query query) {
        Query result = new Query();
        result.setQuerySelectType();

        VarExprList vel = createWithIdentityMapping(getSimpleGroupVars(query).get());

        result.setDistinct(true);
        vel.forEachExpr((v, e) -> smartAdd(result.getProject(), v, e));
        vel.getExprs().values().forEach(result::addGroupBy);
        result.setQueryPattern(query.getQueryPattern());

        return result;
    }

    public static Table toTable(Var v, Collection<Node> nodes) {
        TableN result = new TableN(Collections.singletonList(v));
        for(Node node : nodes) {
            result.addBinding(BindingFactory.binding(v, node));
        }

        return result;
    }

    public static Query splitByGroupBy(Query query, Collection<Node> nodes, long limit) {
        List<Var> groupVars = getSimpleGroupVars(query).get();

        if(groupVars.size() != 1) {
            throw new RuntimeException("Exactly 1 group var required");
        }

        Var groupVar = groupVars.get(0);
        Table table = toTable(groupVar, nodes);

        Query result = splitByGroupBy(query, table, limit);

        return result;
    }

    /**
     * SELECT ?g ... { ... } GROUP BY ?g ->
     *
     * SELECT ?g ?c { { SELECT } UNION { ... } }
     *
     *
     * @param query
     * @param refValues
     * @return
     */
    public static Query splitByGroupBy(Query query, Table table, long limit) {

        // Allocate a fresh var for the count
        Var countVar = VarGeneratorBlacklist.create(varsMentioned(query)).next();

        VarExprList vel = closure(query.getGroupBy());

        Iterator<Binding> it = table.rows();
        List<Element> elts = new ArrayList<>();

        Element e = query.getQueryPattern();
        while(it.hasNext()) {
            Binding b = it.next();

            Query q = createQueryInstance(e, vel, b);
            q.setLimit(limit);
            elts.add(new ElementSubQuery(q));
        }

        Element qp = ElementUtils.unionIfNeeded(elts);

        Query result = new Query();
        result.setQuerySelectType();
        result.setQueryPattern(qp);

        vel.forEachExpr((v, ee) -> smartAdd(result.getProject(), v, ee));
        vel.getExprs().forEach(result::addGroupBy);

        Expr agg = result.allocAggregate(new AggCount());
        result.getProject().add(countVar, agg);

        return result;
    }


    public static ExprList bindingToFilters(Binding b) {
        ExprList result = new ExprList();
        Iterator<Var> it = b.vars();
        while(it.hasNext()) {
            Var v = it.next();
            Node node = b.get(v);

            result.add(new E_Equals(new ExprVar(v), NodeValue.makeNode(node)));
        }

        return result;
    }

    public static Query createQueryInstance(Element elt, VarExprList groupExprs, Binding binding) {

        Element newQueryPattern = ElementUtils.groupIfNeeded(elt, new ElementFilter(ExprUtils.andifyBalanced(bindingToFilters(binding))));

        Query result = new Query();
        result.setQuerySelectType();
        result.setQueryPattern(newQueryPattern);

        //el.forEach(result::addGroupBy);
        //groupExprs.getExprs().forEach(result::addGroupBy);
        groupExprs.forEachExpr((v, e) -> smartAdd(result.getProject(), v, e));

        return result;
    }
}



