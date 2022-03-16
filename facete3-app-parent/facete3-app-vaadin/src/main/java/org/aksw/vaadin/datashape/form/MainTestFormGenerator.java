package org.aksw.vaadin.datashape.form;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.collection.observable.ObservableGraph;
import org.aksw.jena_sparql_api.collection.observable.ObservableGraphImpl;
import org.aksw.jena_sparql_api.collection.observable.RdfField;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchema;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class MainTestFormGenerator {

    static { JenaSystem.init(); }

    public static void main(String[] args) {
        SHFactory.ensureInited();

        ObservableGraph shapeGraph = ObservableGraphImpl.decorate(RDFDataMgr.loadGraph("dcat-ap.shapes.ttl"));
        shapeGraph.addPropertyChangeListener(ev -> System.out.println("Event: " + ev));
        Model shapeModel = ModelFactory.createModelForGraph(shapeGraph);
        NodeSchemaFromNodeShape schema = shapeModel.createResource(DCAT.Dataset.getURI()).as(NodeSchemaFromNodeShape.class);
        // SHNodeShape nodeShape = shapeModel.createResource()

        // NodeSchema schema = new NodeSchemaFromNodeShape(ns);
        // schema.createPropertySchema(RDFS.Nodes.label, false);
        PropertySchema ppp = schema.createPropertySchema(DCAT.distribution.asNode(), true);
        System.out.println("Target schema for distribution: " + ppp.getTargetSchemas());

//        for (PropertySchema ps : ppp.getTargetSchemas().getPredicateSchemas()) {
//            System.out.println(ps.getPredicate() + " " + ps.isForward());
//        }



        Multimap<NodeSchema, Node> roots = HashMultimap.create();
        roots.put(schema, NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04"));

        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");

//        RDFDataMgr.write(System.out, ds, RDFFormat.TRIG);


        RDFConnection conn = RDFConnectionFactory.connect(ds);

        Graph graph = null;
        for (int i = 0; i < 1; ++i) {

            NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
            graph = GraphFactory.createDefaultGraph();
//            dataFetcher.sync(graph, roots, conn);

            System.out.println("Fetching complete:");
            RDFDataMgr.write(System.out, ModelFactory.createModelForGraph(graph), RDFFormat.TURTLE_PRETTY);
        }


        Node sourceNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");

        GraphChange graphEditorModel = new GraphChange();
        // GraphUtil.addInto(graphEditorModel.getBaseGraph(), graph);
//        ObservableCollection<Node> dists = graphEditorModel.createSetForPredicate(sourceNode, DCAT.distribution.asNode(), true);
//
//        dists.addPropertyChangeListener(ev -> System.out.println("Got event: " + ev));
//
//        dists.add(NodeFactory.createBlankNode());



        RdfField rdfField = graphEditorModel.createSetField(sourceNode, DCAT.distribution.asNode(), true);

        rdfField.setDeleted(true);
        rdfField.setIntensional(true);

        // rdfField.repl

        // rdfField.clear();


        ObservableCollection<Node> oldDists = rdfField.getEffectiveAsSet();
        System.out.println("Dists: " + oldDists);
        oldDists.clear();
        System.out.println("Dists: " + oldDists);
//        System.out.println("Deleted Dists: " + graphEditorModel.getDelta().getDeletions());




        // rdfField.

        ObservableCollection<Node> newDists = rdfField.getAddedAsSet();
        newDists.add(NodeFactory.createURI("urn:newDist1"));
        System.out.println("NewDists: " + newDists);

//        for (SHPropertyShape ps : ns.getPropertyShapes()) {
//            System.out.println(ps.getPath() + " " + ps.getMinCount() + " " + ps.getMaxCount() + " " + ps.getOrder() + " " + ps.getClassOrDatatype());
//        }

        // shapeGraph.add(new Triple(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.type));


        //NodeSchemaDataFetcher.
    }
}
