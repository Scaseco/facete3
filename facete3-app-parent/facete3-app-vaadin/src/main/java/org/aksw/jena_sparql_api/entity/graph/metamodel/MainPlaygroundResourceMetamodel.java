package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.collection.ObservableGraph;
import org.aksw.jena_sparql_api.collection.ObservableGraphImpl;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchema;
import org.aksw.jena_sparql_api.schema.PropertySchemaFromPropertyShape;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.DCAT;
import org.topbraid.shacl.model.SHFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class MainPlaygroundResourceMetamodel {
    public static void main(String[] args) {
        JenaSystem.init();
        SHFactory.ensureInited();

        JenaPluginUtils.registerResourceClasses(
                NodeSchemaFromNodeShape.class,
                PropertySchemaFromPropertyShape.class,
                DatasetMetamodel.class,
                ResourceMetamodel.class,
                PredicateStats.class,
                GraphPredicateStats.class);


        testAnalyzeResources();
    }

    public static void testCreateModel() {
        DatasetMetamodel dsm = ModelFactory.createDefaultModel().createResource().as(DatasetMetamodel.class);
        ResourceMetamodel m = dsm.getOrCreateResourceMetamodel("urn:my-resource-1");

        m.getKnownIngoingPredicateIris().add("http://foo.bar/baz");

        PredicateStats ps = m.getOrCreateOutgoingPredicateStats("urn:my-predicate-1");
        GraphPredicateStats gps1 = ps.getOrCreateStats("urn:my-graph-1");
        gps1.setDistinctValueCount(123l);
        gps1.setDistinctValueCountMinimum(true);

        GraphPredicateStats gps2 = ps.getOrCreateStats("urn:my-graph-2");
        gps2.setDistinctValueCount(123l);

        m.getOrCreateOutgoingPredicateStats("urn:my-predicate-1")
            .getOrCreateStats("urn:my-graph-1")
            .setDistinctValueCount(666l);


        RDFDataMgr.write(System.out, m.getModel(), RDFFormat.TURTLE_PRETTY);
    }

    public static void testAnalyzeResources() {
         DatasetMetamodel dsm = ModelFactory.createDefaultModel().createResource().as(DatasetMetamodel.class);


        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        RDFConnection conn = RDFConnectionFactory.connect(ds);

        ObservableGraph shapeGraph = ObservableGraphImpl.decorate(RDFDataMgr.loadGraph("dcat-ap.shapes.ttl"));
        shapeGraph.addPropertyChangeListener(ev -> System.out.println("Event: " + ev));
        Model shapeModel = ModelFactory.createModelForGraph(shapeGraph);
        NodeSchemaFromNodeShape ns = shapeModel.createResource(DCAT.Dataset.getURI()).as(NodeSchemaFromNodeShape.class);

        Multimap<Node, NodeSchema> roots = ArrayListMultimap.create();

        Node sourceNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");

        NodeSchema userSchema = ModelFactory.createDefaultModel().createResource().as(NodeSchemaFromNodeShape.class);
        roots.put(sourceNode, userSchema);


        // NodeSchema baseSchema = new NodeSchemaFromNodeShape(ns);
        roots.put(sourceNode, ns);

        Multimap<NodeSchema, Node> schemaToNodes = HashMultimap.create();
        Multimaps.invertFrom(roots, schemaToNodes);

        analyzeResources(dsm, schemaToNodes, shapeModel, conn);

        RDFDataMgr.write(System.out, dsm.getModel(), RDFFormat.TURTLE_PRETTY);
    }

    public static void analyzeResources(
            DatasetMetamodel dsm,
            Multimap<NodeSchema, Node> schemaToNodes,
            Model shapeModel,
            RDFConnection conn) {


        // Try to fetch for every resource all triples w.r.t. to the schema
        // If that fails then first request the counts for every predicate
        // - only prefetch information if the count is sufficiently low


        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();

        Graph dataGraph = GraphFactory.createDefaultGraph();
        dataFetcher.sync(dataGraph, schemaToNodes, conn);

        fillMetamodel(dsm, schemaToNodes, dataGraph);



    }

    public static void fillMetamodel(DatasetMetamodel dsm, Multimap<NodeSchema, Node> schemaToNodes, Graph dataGraph) {
        for (Entry<NodeSchema, Collection<Node>> e : schemaToNodes.asMap().entrySet()) {
            NodeSchema nodeSchema = e.getKey();
            for (Node node : e.getValue()) {
                fillModel(dsm, dataGraph, nodeSchema, node);
            }
        }
    }

    public static void fillModel(DatasetMetamodel dsm, Graph dataGraph,
            NodeSchema nodeSchema, Node node) {
        ResourceMetamodel rmm = dsm.getOrCreateResourceMetamodel(node);
        // rmm.getOutgoingPredicateStats().get(Node.ANY).getGraphToPredicateStats().get(Node.ANY).get
        for (PropertySchema propertySchema : nodeSchema.getPredicateSchemas()) {
            boolean isForward = propertySchema.isForward();

            NodeSchema targetSchema = propertySchema.getTargetSchema();

            Iterator<Triple> it = propertySchema.streamMatchingTriples(node, dataGraph).iterator();
            while (it.hasNext()) {
                Triple triple = it.next();
                Node p = triple.getPredicate();
//                        Node source = TripleUtils.getSource(triple, isForward);

                rmm.getKnownPredicates(isForward).add(p);

                // We don't have stats here

//                        GraphPredicateStats stats = rmm
//                                .getOrCreatePredicateStats(node, isFoward)
//                                .getOrCreateStats(Quad.defaultGraphNodeGenerated);
                Node targetNode = TripleUtils.getTarget(triple, isForward);

                if (targetSchema != null) {
                    fillModel(dsm, dataGraph, targetSchema, targetNode);
                }
            }

        }
    }

}
