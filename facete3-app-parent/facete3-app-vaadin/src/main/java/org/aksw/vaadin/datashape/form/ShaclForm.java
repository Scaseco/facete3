package org.aksw.vaadin.datashape.form;

import java.util.Collection;
import java.util.Map.Entry;

import org.aksw.facete3.app.vaadin.components.rdf.editor.RdfTermEditor;
import org.aksw.jena_sparql_api.collection.GraphChange;
import org.aksw.jena_sparql_api.collection.ObservableGraph;
import org.aksw.jena_sparql_api.collection.ObservableGraphImpl;
import org.aksw.jena_sparql_api.collection.RdfField;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchema;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
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
import org.apache.jena.vocabulary.DCAT;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHNodeShape;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ShaclForm
    extends FormLayout
{
//    protected RDFConnection conn;


    public ShaclForm() {

        SHFactory.ensureInited();

        ObservableGraph shapeGraph = ObservableGraphImpl.decorate(RDFDataMgr.loadGraph("dcat-ap.shapes.ttl"));
        shapeGraph.addPropertyChangeListener(ev -> System.out.println("Event: " + ev));
        Model shapeModel = ModelFactory.createModelForGraph(shapeGraph);
        SHNodeShape ns = shapeModel.createResource(DCAT.Dataset.getURI()).as(SHNodeShape.class);
        // SHNodeShape nodeShape = shapeModel.createResource()

        NodeSchema baseSchema = new NodeSchemaFromNodeShape(ns);


        // Multimap<NodeSchema, Node> roots = HashMultimap.create();
        Multimap<Node, NodeSchema> roots = ArrayListMultimap.create();

        Node sourceNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");
        sourceNode = NodeFactory.createURI("http://test.org");

        roots.put(sourceNode, baseSchema);

        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");

        RDFConnection conn = RDFConnectionFactory.connect(ds);


        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
        Graph graph = GraphFactory.createDefaultGraph();
        Multimap<NodeSchema, Node> schemaToNodes = Multimaps.invertFrom(roots, HashMultimap.create());
        dataFetcher.sync(graph, schemaToNodes, conn);

        System.out.println("Fetching complete:");
        RDFDataMgr.write(System.out, ModelFactory.createModelForGraph(graph), RDFFormat.TURTLE_PRETTY);



        GraphChange graphEditorModel = new GraphChange();
        GraphUtil.addInto(graphEditorModel.getBaseGraph(), graph);



        for (Entry<Node, Collection<NodeSchema>> e : roots.asMap().entrySet()) {
            Node root = e.getKey();
            Span nodeSpan = new Span("Root: " + root);
            add(nodeSpan);

            Collection<NodeSchema> schemas = e.getValue();

            for (NodeSchema schema : schemas) {
                Span schemaSpan = new Span("Some schema");
                add(schemaSpan);

                for (PropertySchema ps : schema.getPredicateSchemas()) {
//                    getElement().appendChild(new Element("hr"));
//                    Span propertySpan = new Span(ps.getPredicate().getURI());
//                    add(propertySpan);
                    // FormItem formItem = addFormItem(span);

                    RdfField rdfField = graphEditorModel.createSetField(sourceNode, ps.getPredicate(), true);

                    Collection<Node> existingValues = rdfField.getBaseAsSet();


                    Button addValueButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE_O));
                    addFormItem(addValueButton, ps.getPredicate().getURI());


                    for (Node existingValue : existingValues) {

                        RdfTermEditor rdfTermEditor = new RdfTermEditor();
                        FormItem formItem = addFormItem(rdfTermEditor, ps.getPredicate().getURI());
                        rdfTermEditor.setValue(existingValue);
                        System.out.println("Added: " + sourceNode + " " + ps.getPredicate() + " " + existingValue);
                        setColspan(formItem, 3);
                    }

                }
            }
        }

    }
}