package org.aksw.vaadin.datashape.form;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map.Entry;

import org.aksw.facete3.app.vaadin.components.rdf.editor.RdfTermEditor;
import org.aksw.facete3.app.vaadin.plugin.ManagedComponentSimple;
import org.aksw.jena_sparql_api.collection.GraphChange;
import org.aksw.jena_sparql_api.collection.ObservableCollection;
import org.aksw.jena_sparql_api.collection.ObservableGraph;
import org.aksw.jena_sparql_api.collection.ObservableGraphImpl;
import org.aksw.jena_sparql_api.collection.ObservableValue;
import org.aksw.jena_sparql_api.collection.RdfField;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchema;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
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
import org.apache.jena.vocabulary.DCAT;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHNodeShape;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;

public class ShaclForm
    extends FormLayout
{
//    protected RDFConnection conn;

    protected Multimap<Node, NodeSchema> roots = ArrayListMultimap.create();
    protected NodeSchemaDataFetcher dataFetcher;

    public ShaclForm() {
        setResponsiveSteps(
                new ResponsiveStep("25em", 1),
                new ResponsiveStep("32em", 2),
                new ResponsiveStep("40em", 3));

        SHFactory.ensureInited();

        ObservableGraph shapeGraph = ObservableGraphImpl.decorate(RDFDataMgr.loadGraph("dcat-ap.shapes.ttl"));
        shapeGraph.addPropertyChangeListener(ev -> System.out.println("Event: " + ev));
        Model shapeModel = ModelFactory.createModelForGraph(shapeGraph);
        SHNodeShape ns = shapeModel.createResource(DCAT.Dataset.getURI()).as(SHNodeShape.class);
        // SHNodeShape nodeShape = shapeModel.createResource()

        NodeSchema baseSchema = new NodeSchemaFromNodeShape(ns);


        // Multimap<NodeSchema, Node> roots = HashMultimap.create();

        Node sourceNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");
//        sourceNode = NodeFactory.createURI("http://test.org");

        roots.put(sourceNode, baseSchema);


        dataFetcher = new NodeSchemaDataFetcher();
        // NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
        // Graph graph = GraphFactory.createDefaultGraph();

        

        refresh();
    }
    
    public void refresh() {
        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        RDFConnection conn = RDFConnectionFactory.connect(ds);
    	
        Graph graph = GraphFactory.createDefaultGraph();
        Multimap<NodeSchema, Node> schemaToNodes = Multimaps.invertFrom(roots, HashMultimap.create());
        dataFetcher.sync(graph, schemaToNodes, conn);

        System.out.println("Fetching complete:");
        RDFDataMgr.write(System.out, ModelFactory.createModelForGraph(graph), RDFFormat.TURTLE_PRETTY);



        GraphChange graphEditorModel = new GraphChange();
        GraphUtil.addInto(graphEditorModel.getBaseGraph(), graph);

        TextArea status = new TextArea();
        status.setWidthFull();
        FormItem statusFormItem = addFormItem(status, "Status");
        setColspan(statusFormItem, 3);

        Runnable update = () -> {
                Model additions = ModelFactory.createModelForGraph(graphEditorModel.getEffectiveAdditionGraph());
                Model deletions = ModelFactory.createModelForGraph(graphEditorModel.getEffectiveDeletionGraph());
                String str = "Added:\n" + toString(additions, RDFFormat.TURTLE_PRETTY) + "\n"
                        + "Removed:\n" + toString(deletions, RDFFormat.TURTLE_PRETTY);
                status.setValue(str);
        };
        
        graphEditorModel.getEffectiveAdditionGraph().addPropertyChangeListener(ev -> {
        	update.run();
        });
        
        graphEditorModel.getEffectiveDeletionGraph().addPropertyChangeListener(ev -> {
    		update.run();
    	});


        for (Entry<Node, Collection<NodeSchema>> e : roots.asMap().entrySet()) {
            Node root = e.getKey();
            Span nodeSpan = new Span("Root: " + root);
            add(nodeSpan);

            TextField nodeIdTextField = new TextField();
            FormItem nodeIdFormItem = addFormItem(nodeIdTextField, "IRI");
            graphEditorModel.getRenamedNodes().put(root, root);

            Button resetNodeIdButton = new Button("Reset");
            resetNodeIdButton.addClickListener(ev -> {
                graphEditorModel.getRenamedNodes().put(root, root);
            });
            nodeIdTextField.setSuffixComponent(resetNodeIdButton);

            bind(nodeIdTextField, graphEditorModel.getRenamedNodes().observeKey(root).convert(NodeMappers.uriString.asConverter()));


            Collection<NodeSchema> schemas = e.getValue();

            for (NodeSchema schema : schemas) {
                Span schemaSpan = new Span("Some schema");
                add(schemaSpan);

                for (PropertySchema ps : schema.getPredicateSchemas()) {
//                    getElement().appendChild(new Element("hr"));
//                    Span propertySpan = new Span(ps.getPredicate().getURI());
//                    add(propertySpan);
                    // FormItem formItem = addFormItem(span);

                    RdfField rdfField = graphEditorModel.createSetField(root, ps.getPredicate(), true);

                    ObservableCollection<Node> existingValues = rdfField.getBaseAsSet();
                    ObservableCollection<Node> addedValues = rdfField.getAddedAsSet();



                    Button addValueButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE_O));
                    addValueButton.addClickListener(ev -> {
                        Node newNode = NodeFactory.createBlankNode();
                        addedValues.add(newNode);
                    });
                    addFormItem(addValueButton, ps.getPredicate().getURI());

                    ListView<Node> view = ListView.create(new DataProviderFromField(rdfField),
                    		item -> {
                    			Button btn = new Button("" + item);
                    			btn.addClickListener(ev -> {
                    				rdfField.getAddedAsSet().remove(item);
                    			});
                    			return ManagedComponentSimple.wrap(btn);
                    		});
                    addFormItem(view, "List");
                    
//                    for (Node addedValue : addedValues) {
//
//                        Triple t = Triple.create(root, ps.getPredicate(), addedValue);
//                        ObservableValue<Node> value = graphEditorModel.createFieldForExistingTriple(t, 2);
//
//                        RdfTermEditor rdfTermEditor = new RdfTermEditor();
//                        FormItem formItem = addFormItem(rdfTermEditor, ps.getPredicate().getURI());
////                        rdfTermEditor.setValue(existingValue);
//                        System.out.println("Added: " + sourceNode + " " + ps.getPredicate() + " " + addedValue);
//                        setColspan(formItem, 3);
//
//                        bind(rdfTermEditor, value);
//                    }



                    for (Node existingValue : existingValues) {

                        Triple t = Triple.create(root, ps.getPredicate(), existingValue);
                        ObservableValue<Node> value = graphEditorModel.createFieldForExistingTriple(t, 2);

                        RdfTermEditor rdfTermEditor = new RdfTermEditor();
                        FormItem formItem = addFormItem(rdfTermEditor, ps.getPredicate().getURI());
//                        rdfTermEditor.setValue(existingValue);
                        System.out.println("Added: " + root + " " + ps.getPredicate() + " " + existingValue);
                        setColspan(formItem, 3);

                        // new Button(new Icon(VaadinIcon.TRASH));
                        Checkbox markAsDeleted = new Checkbox(false); 
                        addFormItem(markAsDeleted, "Delete");
                        
                        ObservableValue<Boolean> isDeleted = graphEditorModel.getDeletionGraph().asSet()
                        		.filter(c -> c.equals(t))
                        		.mapToValue(c -> !c.isEmpty(), b -> b ? null : t);
                        
                        bind(markAsDeleted, isDeleted);
                        
                        
                        markAsDeleted.addValueChangeListener(event -> {
                        	Boolean state = event.getValue();
                        	if (Boolean.TRUE.equals(state)) {
                        		graphEditorModel.getDeletionGraph().add(t);
                        	} else {
                        		graphEditorModel.getDeletionGraph().delete(t);
                        	}
                        });
                        // graphEditorModel.getDeletionGraph().tr
                        
                        bind(rdfTermEditor, value);
                    }

                }
            }
        }

    }


    // TODO Move to ModelUtils
    public static String toString(Model model, RDFFormat rdfFormat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, model, rdfFormat);
        String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
        return result;
    }

    public static <V> Runnable bind(HasValue<?, V> hasValue, ObservableValue<V> store) {
        V value = store.get();
        hasValue.setValue(value);

        Runnable deregister1 = store.addPropertyChangeListener(ev -> {
            V newValue = (V)ev.getNewValue();
            hasValue.setValue(newValue);
        });

        Registration deregister2 = hasValue.addValueChangeListener(ev -> {
            V newValue = ev.getValue();
            store.set(newValue);
        });

        return () -> {
            deregister1.run();
            deregister2.remove();
        };
    }
}