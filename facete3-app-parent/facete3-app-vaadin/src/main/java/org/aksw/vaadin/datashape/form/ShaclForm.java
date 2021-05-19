package org.aksw.vaadin.datashape.form;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
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
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

public class ShaclForm
    extends FormLayout
{
//    protected RDFConnection conn;

    protected Multimap<Node, NodeSchema> roots = ArrayListMultimap.create();
    protected NodeSchemaDataFetcher dataFetcher;

    int maxCols = 3; // TODO Get this using a method?
    
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

        
        Node sourceNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");

        NodeSchema userSchema = new NodeSchemaFromNodeShape(
        		ModelFactory.createDefaultModel().createResource().as(SHNodeShape.class));
        roots.put(sourceNode, userSchema);
        
        NodeSchema baseSchema = new NodeSchemaFromNodeShape(ns);
        roots.put(sourceNode, baseSchema);


        // Multimap<NodeSchema, Node> roots = HashMultimap.create();

//        sourceNode = NodeFactory.createURI("http://test.org");



        dataFetcher = new NodeSchemaDataFetcher();
        // NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
        // Graph graph = GraphFactory.createDefaultGraph();

        

        refresh();
    }
    
    public void refresh() {
        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        RDFConnection conn = RDFConnectionFactory.connect(ds);
    	
        Graph graph = GraphFactory.createDefaultGraph();
        Multimap<NodeSchema, Node> schemaToNodes = Multimaps.invertFrom(roots, ArrayListMultimap.create());
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

        
        ListDataProvider<Node> rootList = new ListDataProvider<>(roots.keys());
        
        // ListView<Node> rootListView = ListView.create(rootList, item -> renderRoot(graphEditorModel, item, this));
 
        // HasOrderedComponents<?> test = this;

        for (Entry<Node, Collection<NodeSchema>> e : roots.asMap().entrySet()) {
        	Collection<NodeSchema> schemas = roots.asMap().get(e.getKey());
        	renderRoot(graphEditorModel, e.getKey(), schemas, this);
        }

    }

    // [done] Fix langtag/datatype in RdfTermEditor
    // TODO Fix layout of RdfTermEditor
    // TODO Add feature to add new predicates
    // TODO Add feature to add schemas (sets of predicates)
    // TODO Add feature to delete resources recursively according to schema
    //      This most likely needs extra options such as only remove if orphaned; i.e. there must not be another resource that references the one to delete with a forward link
    // TOOD Add feature to show count of values for each property
    // TODO Add feature to paginate over a property's values
    // TODO Add feature to (un-)collapse resources
    // TODO Add feature to show name clashes with existing resources
    
    public void renderRoot(
    		GraphChange graphEditorModel,
    		Node root,
    		Collection<NodeSchema> schemas,
    		FormLayout target) {

    	System.out.println("Rendering " + root);
    	
//        Node root = e.getKey();
        Span nodeSpan = new Span("Root: " + root);
        target.add(nodeSpan);

        if (root.isURI()) {
	        TextField nodeIdTextField = new TextField();
	        nodeIdTextField.setValueChangeMode(ValueChangeMode.LAZY);
	        FormItem nodeIdFormItem = target.addFormItem(nodeIdTextField, "IRI");
	        graphEditorModel.getRenamedNodes().put(root, root);
	
	        Button resetNodeIdButton = new Button("Reset");
	        resetNodeIdButton.addClickListener(ev -> {
	            graphEditorModel.getRenamedNodes().put(root, root);
	        });
        
        	nodeIdTextField.setSuffixComponent(resetNodeIdButton);

        	bind(nodeIdTextField, graphEditorModel.getRenamedNodes().observeKey(root).convert(NodeMappers.uriString.asConverter()));

        	// Fill up the row
            target.setColspan(resetNodeIdButton, 2);
        } else {
        	target.setColspan(nodeSpan, maxCols);
        }
        

        /* Controls for adding new properties and schemas */
//        Button addPropertyButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE_O));
        Button addPropertyButton = new Button("ADD NEW PROPERTY");
        target.add(addPropertyButton);

        ListBindingSupport2<NodeSchema, SerializablePredicate<NodeSchema>, FormLayout> lbs = ListBindingSupport2.create(
        		target, schemas, schema -> schema.getPredicateSchemas(), (schema, s) -> {
        				Span schemaSpan = new Span("Schema: ");
        				s.add(schemaSpan, xspan -> xspan.setText("Schema: " + schema.getPredicateSchemas().size()));
        				
        				// s.add(span).withUpdate((span, schema) -> 
        				// s.addUpdate(() -> span.setText("Schema: someSchema"))
        				
        				
//ComponentControlModular<NodeSchema, FormLayout> x = s;

        				//ListBindingSupport2.create(target, schema.getPredicateSchemas(), (propertySchma, cb) -> {});

//        				Component x;
//        				x.addAttachListener(ev -> ev.);
//        				x.addAttachListener(null)
        				s.add(propertyList(schema, graphEditorModel, root, target)
        						.withAdapter(NodeSchema::getPredicateSchemas));

        				System.out.println("Created schema ui part");
//        				protected List<Component> components = new ArrayList<>();
        			});

//        ListBindingSupport2<NodeSchema, SerializablePredicate<NodeSchema>, FormLayout> lbs = new ListBindingSupport2<>(
//        		target, new ListDataProvider<NodeSchema>(schemas), schema -> schema, schema ->
//        			new ComponentControl<FormLayout>() {
//
//        				protected Span schemaSpan = new Span("Schema: ");
//        				protected List<Component> components = new ArrayList<>();
//						protected List<Function<FormLayout, Component>> newComponents = new ArrayList<>();
//        				
//						@Override
//						public void detach() {
//							// target
//						}
//
//						@Override
//						public void attach(FormLayout target) {
//						}
//
//						@Override
//						public void refresh() {
//
//														
//	//							for (PropertySchema ps : schema.getPredicateSchemas()) {
//	//   
//	//				            for (Component c : components) {
//	//				            	target.remove(c);
//	//				            }
//	//				            
//	//				            components = newComponents.stream().map(f -> f.apply(target)).collect(Collectors.toList());
////									});
////						}
//					}
//						
//					@Override
//					public void close() {
//						
//					}
//        				
//    			});
    
        int i[] = {0};
        addPropertyButton.addClickListener(ev -> {
        	NodeSchema x = schemas.iterator().next();
        	String str = "http://foo" + (++i[0]);
        	x.createPropertySchema(NodeFactory.createURI(str), true);
        	lbs.refresh();
        });
        
    }
    

    
    public ListBindingSupport2<PropertySchema, SerializablePredicate<PropertySchema>, FormLayout> propertyList(NodeSchema schema, GraphChange graphEditorModel, Node root, FormLayout target) {
    	
		ListBindingSupport2<PropertySchema, SerializablePredicate<PropertySchema>, FormLayout> lbs2 =
				ListBindingSupport2.create(
						target, schema.getPredicateSchemas(),
						(ps, newComponent) -> {
//							    getElement().appendChild(new Element("hr"));
//							    Span propertySpan = new Span(ps.getPredicate().getURI());
//							    add(propertySpan);
			    // FormItem formItem = addFormItem(span);

//							ComponentControlModular<PropertySchema, FormLayout> x = newComponent;
							
			    RdfField rdfField = graphEditorModel.createSetField(root, ps.getPredicate(), true);

			    ObservableCollection<Node> existingValues = rdfField.getBaseAsSet();
			    ObservableCollection<Node> addedValues = rdfField.getAddedAsSet();


			    HorizontalLayout propertySpan = new HorizontalLayout();
			    target.setColspan(propertySpan, 3);
			    propertySpan.add(ps.getPredicate().getURI());

			    Button addValueButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE_O));
			    propertySpan.add(addValueButton);
			    // target.addFormItem(addValueButton, ps.getPredicate().getURI());


			    ListView<Node> view = ListView.create(new DataProviderFromField(rdfField),
			    		item -> {
			    			VerticalLayout tmp = new VerticalLayout();
			    			
			    			RdfTermEditor ed = new RdfTermEditor();       			
			    			ObservableValue<Node> remapped = graphEditorModel.getRenamedNodes().observeKey(item);
			    			
			    			bind(ed, remapped);
			    			tmp.add(ed);

			    			Button btn = new Button("" + item);
			    			btn.addClickListener(ev -> {
			    				rdfField.getAddedAsSet().remove(item);
			    			});
			    			
			    			tmp.add(btn);
			    			return ManagedComponentSimple.wrap(tmp);
			    		});
			    
			    view.getStyle().set("border-left", "thin solid");
			    view.getStyle().set("padding-left", "10px");
			    
			    // FormItem fi = target.addFormItem(view, "List");
			    
//							    target.add(propertySpan);
//							    target.add(view);
//							    target.setColspan(view, maxCols);
			    newComponent.add(propertySpan);
			    newComponent.add(view, (tgt, v)-> tgt.setColspan(view, maxCols));
			    
			    
			    newComponent.add(addValueButton.addClickListener(ev -> {
			        // Node newNode = NodeFactory.createBlankNode();
			    	Node newNode = graphEditorModel.freshNode();
			        addedValues.add(newNode);
			    }));

			    
//							    for (Node addedValue : addedValues) {
			//
//							        Triple t = Triple.create(root, ps.getPredicate(), addedValue);
//							        ObservableValue<Node> value = graphEditorModel.createFieldForExistingTriple(t, 2);
			//
//							        RdfTermEditor rdfTermEditor = new RdfTermEditor();
//							        FormItem formItem = addFormItem(rdfTermEditor, ps.getPredicate().getURI());
////							        rdfTermEditor.setValue(existingValue);
//							        System.out.println("Added: " + sourceNode + " " + ps.getPredicate() + " " + addedValue);
//							        setColspan(formItem, 3);
			//
//							        bind(rdfTermEditor, value);
//							    }


			    NodeSchema targetSchema = ps.getTargetSchema();

			    // for (Node existingValue : existingValues) {
			    ListView<Node> existingView = ListView.create(new ListDataProvider<Node>(existingValues),
			    		existingValue -> {

			    			VerticalLayout newC = new VerticalLayout();

			        Triple t = Triple.create(root, ps.getPredicate(), existingValue);
			        ObservableValue<Node> value = graphEditorModel.createFieldForExistingTriple(t, 2);

			        RdfTermEditor rdfTermEditor = new RdfTermEditor();
			        // FormItem formItem = target.addFormItem(rdfTermEditor, ps.getPredicate().getURI());
//							        rdfTermEditor.setValue(existingValue);
			        System.out.println("Added: " + root + " " + ps.getPredicate() + " " + existingValue);

			        // new Button(new Icon(VaadinIcon.TRASH));
			        Checkbox markAsDeleted = new Checkbox(false); 
			        //target.addFormItem(markAsDeleted, "Delete");
			        
			        ObservableValue<Boolean> isDeleted = graphEditorModel.getDeletionGraph().asSet()
			        		.filter(c -> c.equals(t))
			        		.mapToValue(c -> !c.isEmpty(), b -> b ? null : t);
			        
			        newComponent.add(bind(markAsDeleted, isDeleted));
			        
			        
			        newComponent.add(markAsDeleted.addValueChangeListener(event -> {
			        	Boolean state = event.getValue();
			        	if (Boolean.TRUE.equals(state)) {
			        		graphEditorModel.getDeletionGraph().add(t);
			        	} else {
			        		graphEditorModel.getDeletionGraph().delete(t);
			        	}
			        }));
			        
			        // graphEditorModel.getDeletionGraph().tr
			        
			        newComponent.add(bind(rdfTermEditor, value));
			        
			        Collection<NodeSchema> s = targetSchema == null ? Collections.emptyList() : Collections.singletonList(targetSchema);
			        
			        if (!s.isEmpty()) {
			        	// renderRoot(graphEditorModel, existingValue, s, target);
			        }

//			        newComponent.add(rdfTermEditor, (tgt, rte) -> tgt.setColspan(rte, maxCols - 1));
//			        newComponent.add(markAsDeleted, (tgt, mad) -> tgt.add(mad, 1));
			        newC.add(rdfTermEditor);
			        newC.add(markAsDeleted);
			        
			        
			        
			        // newComponent.add
			        Node predicate = ps.getPredicate();
			        boolean isForward = ps.isForward();

			        Multimap<Node, NodeSchema> childState = ArrayListMultimap.create();
			        NodeSchema userSchema = new NodeSchemaFromNodeShape(
			        		ModelFactory.createDefaultModel().createResource().as(SHNodeShape.class));

			        childState.put(existingValue, userSchema);

			        if (targetSchema != null) {
			        	childState.put(existingValue, targetSchema);
			        }
			        
			        
			        FormLayout childLayout = new FormLayout();
			        newC.add(childLayout);
			        
			        Button childToggleBtn = new Button("Show child");
			        childToggleBtn.addClickListener(ev -> {
			        	renderRoot(graphEditorModel, existingValue, s, childLayout);
			        });
//			        
			        newC.add(childToggleBtn);
			        return new ManagedComponentSimple(newC);
			    });
			    
			    existingView.getStyle().set("border-left", "thin solid");
			    existingView.getStyle().set("padding-left", "10px");

			    newComponent.add(existingView, (tgt, v)-> tgt.setColspan(v, maxCols));

			}
								
		);
		
		return lbs2;
    }
    

    // TODO Move to ModelUtils
    public static String toString(Model model, RDFFormat rdfFormat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, model, rdfFormat);
        String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
        return result;
    }

    public static <V> Registration bind(HasValue<?, V> hasValue, ObservableValue<V> store) {
        V value = store.get();
        hasValue.setValue(value);

        Runnable deregister1 = store.addPropertyChangeListener(ev -> {
            V newValue = (V)ev.getNewValue();
            hasValue.setValue(newValue);
        });

        
        // Extra variable because of https://stackoverflow.com/questions/55532055/java-casting-java-11-throws-lambdaconversionexception-while-1-8-does-not
        ValueChangeListener<ValueChangeEvent<V>> listener = ev -> {
            V newValue = ev.getValue();
            store.set(newValue);
        };
        Registration deregister2 = hasValue.addValueChangeListener(listener);

        return () -> {
            deregister1.run();
            deregister2.remove();
        };
    }
}