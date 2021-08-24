package org.aksw.jena_sparql_api.entity.graph.metamodel;

import org.aksw.jena_sparql_api.collection.ObservableGraph;
import org.aksw.jena_sparql_api.collection.ObservableGraphImpl;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCAT;
import org.topbraid.shacl.model.SHNodeShape;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class MainPlaygroundResourceMetamodel {
    public static void main(String[] args) {
        JenaPluginUtils.registerResourceClasses(
                DatasetMetamodel.class,
                ResourceMetamodel.class,
                PredicateStats.class,
                GraphPredicateStats.class);

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


    public static void analyzeResources(Multimap<NodeSchema, Node> schemaToNodes) {

        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        RDFConnection conn = RDFConnectionFactory.connect(ds);

        ObservableGraph shapeGraph = ObservableGraphImpl.decorate(RDFDataMgr.loadGraph("dcat-ap.shapes.ttl"));
        shapeGraph.addPropertyChangeListener(ev -> System.out.println("Event: " + ev));
        Model shapeModel = ModelFactory.createModelForGraph(shapeGraph);
        SHNodeShape ns = shapeModel.createResource(DCAT.Dataset.getURI()).as(SHNodeShape.class);

        Multimap<Node, NodeSchema> roots = ArrayListMultimap.create();

        Node sourceNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");

        NodeSchema userSchema = new NodeSchemaFromNodeShape(
                ModelFactory.createDefaultModel().createResource().as(SHNodeShape.class));
        roots.put(sourceNode, userSchema);

        NodeSchema baseSchema = new NodeSchemaFromNodeShape(ns);
        roots.put(sourceNode, baseSchema);


        // Try to fetch for every resource all triples w.r.t. to the schema
        // If that fails then first request the counts for every predicate
        // - only prefetch information if the count is sufficiently low


        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();

        dataFetcher.sync(shapeGraph, schemaToNodes, conn);







    }

}
