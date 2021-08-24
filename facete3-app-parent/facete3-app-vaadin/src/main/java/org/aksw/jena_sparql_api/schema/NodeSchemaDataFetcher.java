package org.aksw.jena_sparql_api.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A class to retrieve the triples that correspond to a set of RDF resources w.r.t.
 * given schemas.
 *
 * @author raven
 *
 */
public class NodeSchemaDataFetcher {

    private static final Logger logger = LoggerFactory.getLogger(NodeSchemaDataFetcher.class);

    /**
     * Build the query that retrieves the information that matches the schemas for the given nodes.
     *
     * This method does not perform any optimizations:
     * If the same node occurs under multiple schemas with overlapping properties then
     * needless amounts of data are accessed.
     * In principle a preprocessing step could be applied that optimizes the 'schemaAndNodes' argument.
     * TODO Whenever we provide such a preprocessing implementation link to it from here
     *
     * @param schemaAndNodes
     * @return
     */
    public static Query toQuery(Multimap<NodeSchema, Node> schemaAndNodes) {

        Set<Query> unionMembers = new LinkedHashSet<>();
        for (Entry<NodeSchema, Collection<Node>> e : schemaAndNodes.asMap().entrySet()) {
            NodeSchema schema = e.getKey();
            Collection<Node> nodes = e.getValue();
            Query unionMember = immediateSchemaToSparql(schema);
            QueryUtils.injectFilter(unionMember, ExprUtils.oneOf(Vars.s, nodes));

            unionMembers.add(unionMember);
        }

        Query result = QueryUtils.unionConstruct(unionMembers);
        return result;
    }


    public void sync(Graph target,
            // NodeGraphSchema schema,
            Multimap<NodeSchema, Node> schemaAndNodes,
            SparqlQueryConnection conn) {

        Multimap<NodeSchema, Node> done = HashMultimap.create();

        Multimap<NodeSchema, Node> next = schemaAndNodes;
        while (!next.isEmpty()) {
            Multimap<NodeSchema, Node> tmp = step(target, next, conn, done);
            next = tmp;
        }


    }

    public Multimap<NodeSchema, Node> step(Graph target,
            // NodeGraphSchema schema,
            Multimap<NodeSchema, Node> schemaAndNodes,
            SparqlQueryConnection conn,
            Multimap<NodeSchema, Node> done) {

        // The map for what to fetch in the next breadth
        Multimap<NodeSchema, Node> next = HashMultimap.create();

        Graph graph = GraphFactory.createDefaultGraph();

        Query unionQuery = toQuery(schemaAndNodes);
        logger.debug("Union Query: " + unionQuery);

        SparqlRx.execConstructTriples(conn, unionQuery).forEach(graph::add);

        for (Entry<NodeSchema, Collection<Node>> e : schemaAndNodes.asMap().entrySet()) {
            NodeSchema schema = e.getKey();
            Collection<Node> nodes = e.getValue();

            for (Node node : nodes) {

//                Graph tmp = GraphFactory.createDefaultGraph();
                schema.copyMatchingTriples(node, target, graph);

//                RDFDataMgr.write(System.out, ModelFactory.createModelForGraph(tmp), RDFFormat.TURTLE_PRETTY);

                for (PropertySchema predicateSchema : schema.getPredicateSchemas()) {
                    // Get the set of matching values so that we can perform a nested lookup with them
                    Set<Node> subGraphNodes = new LinkedHashSet<Node>();
                    predicateSchema.copyMatchingValues(node, subGraphNodes, target);

//                    System.out.println("Next nodes for " + predicateSchema.getPredicate() + ": " + subGraphNodes);

                    NodeSchema targetSchema = predicateSchema.getTargetSchema();
                    if (targetSchema != null) {
                        for (Node targetNode : subGraphNodes) {
                            if (!done.containsEntry(targetSchema, targetNode)) {
                                done.put(targetSchema, targetNode);
                                next.put(targetSchema, targetNode);
                            }
                        }
                    }
                }
            }
        }

        return next;
    }


    public static Query immediateSchemaToSparql(NodeSchema schema) {
        Set<Expr> fwdDisjunction = new LinkedHashSet<>();
        Set<Expr> bwdDisjunction = new LinkedHashSet<>();
        for (PropertySchema predicateSchema : schema.getPredicateSchemas()) {
            boolean isFwd = predicateSchema.isForward();

            Node p = predicateSchema.getPredicate();
            Expr expr = new E_Equals(new ExprVar(Vars.p), NodeValue.makeNode(p));
//            ExprList el = new ExprList(expr);

            if (isFwd) {
                fwdDisjunction.add(expr);
            } else {
                bwdDisjunction.add(expr);
            }
        }

        for (DirectedFilteredTriplePattern dftp : schema.getGenericPatterns()) {
            // Align the variable naming
            // Map<Var, Var> var

            // TernaryRelation tr = new TernaryRelationImpl(new ElementFilter(), dftp.getTriplePattern())
        }

        // inverse predicate / object variables
        Var ip = Var.alloc("ip");
        Var io = Var.alloc("io");

        Triple fwdTriplePattern = Triple.create(Vars.s, Vars.p, Vars.o);
        Triple bwdTriplePattern = Triple.create(io, ip, Vars.s);

        List<Triple> tps = new ArrayList<>(2);
        List<Element> elts = new ArrayList<>(2);
        if (!fwdDisjunction.isEmpty()) {
            tps.add(fwdTriplePattern);
            elts.add(
                ElementUtils.groupIfNeeded(
                    ElementUtils.createElement(fwdTriplePattern),
                    new ElementFilter(ExprUtils.orifyBalanced(fwdDisjunction))
                ));
        }

        if (!bwdDisjunction.isEmpty()) {
            tps.add(bwdTriplePattern);
            elts.add(
                    ElementUtils.groupIfNeeded(
                        ElementUtils.createElement(bwdTriplePattern),
                        new ElementFilter(ExprUtils.orifyBalanced(bwdDisjunction))
                    ));
        }


        Query stdQuery = new Query();
        stdQuery.setQueryConstructType();
        stdQuery.setConstructTemplate(new Template(BasicPattern.wrap(tps)));
        stdQuery.setQueryPattern(ElementUtils.unionIfNeeded(elts));

        // AttributeGraphFragment result = new AttributeGraphFragment().addMandatoryJoin(Vars.s, stdQuery);
//        GraphPartitionJoin result = new GraphPartitionJoin(EntityGraphFragment.fromQuery(Vars.s, stdQuery));
//System.out.println(stdQuery);

        // return result;
        return stdQuery;
        //ListServiceEntityQuery ls = new ListServiceEntityQuery(conn, agf);
    }


    public static void main(String [] args) {
        NodeSchema schema = new NodeSchemaImpl();
        PropertySchema pgs = schema.createPropertySchema(RDF.type.asNode(), true);
        PropertySchema pgs2 = schema.createPropertySchema(DCTerms.identifier.asNode(), true);
        PropertySchema pgs3 = schema.createPropertySchema(DCAT.distribution.asNode(), true);

        pgs3.getTargetSchema().createPropertySchema(DCAT.downloadURL.asNode(), true);

        Multimap<NodeSchema, Node> roots = HashMultimap.create();
        roots.put(schema, NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04"));

        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");

//        RDFDataMgr.write(System.out, ds, RDFFormat.TRIG);


        RDFConnection conn = RDFConnectionFactory.connect(ds);

        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
        Graph graph = GraphFactory.createDefaultGraph();
        dataFetcher.sync(graph, roots, conn);

    }

}
