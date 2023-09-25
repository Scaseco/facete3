package org.aksw.facete3.app.vaadin.components.rdf.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.Registration;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.jena_sparql_api.update.GraphListenerBatchBase;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.vaadin.common.component.managed.ManagedComponent;
import com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.DatasetChanges;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphMonitor;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.update.UpdateRequest;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;


/*
 * Issue: How to separate the life-cycle of changes from the components that introduced them?
 * For example, if a form uses pagination over a large amount of an rdf resource's properties
 * than changing the page may delete the components of the prior page.
 *
 * Maybe rdf intensional deletions are bound to the triples that were inserted?
 * Hm, actually this is a schema specification, so it would have to reside on the NodeGraph
 * - i.e. on the node graph one component states that property foo is single valued.
 * A multivalued form field could then simply ask the schema whether multiple values are permitted.
 * If not, then the 'add' button may not be shown.
 *
 * But still, the cardinality restriction is an object that gets imposed by the form component.
 * And how can this restriction 'survive' the deletion of a form component and how can it be 'reclaimed'
 * if the component gets recreated?
 *
 * A simple solution might be to simply not delete components but only to detach them.
 * Nonetheless, there needs to be a 'ComponentManager' that holds the set of components.
 * In that regard, the component manager can also manage the set of bindings (binders?) -
 * i.e. upon recreation of a component from a factory it could re-use an existing binding objects
 *
 *
 * We probably need a 'ChangeCollection' on which binders register their changes.
 *
 * bindingFactory
 *   .setComponent(component)
 *   .strategyReplacePropertyValues(RDFS.label)
 *   .lifeCycle(changeContainer) // ???
 *
 *
 */


//interface ChangeContext {
//    /**
//     * Get all nodes references in the editor view together with their schemas.
//     * The main purpose is to enable bulk retrieval of all triples according to the schemas
//     * w.r.t. a given connection.
//     *
//     * @return
//     */
//    Multimap<Node, NodeSchema> getNodeToSchema();
//
//    Map<Node, Node> getEffectiveRenames();
//
//    Runnable addListener();
//}

//interface ResourceCondition
//{
//    TripleConstraint bindToSource(NodeGraph nodeGraph);
//}

/**
 * Reference to a property's values.
 * Implies a cardinality restriction on that property - i.e. single or multi-valued.
 *
 *
 */
interface PropertyValueRef {
    //
    Object bind(Node source);
}




//class NodeSchema {
//    protected SlottedBuilder<Expr, Expr> predicateFilterBuilder = new SlottedBuilderImpl<>(
//            exprs -> org.aksw.jena_sparql_api.utils.ExprUtils.orifyBalanced(exprs));
//
//
//}

/**
 * A graph fragment restricted to a certain node in subject or object position
 *
 */
//class NodeGraph
//    extends GraphBase
//{
//    protected Node source;
//    // protected Set<TripleConstraint> constraints;
//    protected NodeSchema schema;
//
//    protected Map<P_Path0, NodeGraph> subNodeGraphs;
//
////    NodeGraph getGraph(Node predicate, boolean isForward) {
////        return null;
////    }
//
//    @Override
//    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
//        //cacheGraph.find
//
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//}



//class GraphMaterializedView
//    extends GraphBase
//{
//    protected Graph graph;
//
//
//}






/**
 * A resoure that acts as an object provides a method to remove all
 * triples related to that object
 *
 * @author raven
 *
 */
interface ResourceObject
    extends Resource
{
    /**
     * Destroy the object effectively removing all triples that comprise that object's state
     *
     * @return The number of changed (deleted) triples; (-1 if this is not tracked)
     */
    int destroy();
}


interface GraphChangeActionAddTriple {
    Triple getTriple();
    UpdateRequest getDeleteStatement();
}

@ResourceView
interface GraphChangeActionResourceObjectAddTriple
    extends GraphChangeActionAddTriple, ResourceObject
{
    // Triple$
    String getUpdateRequest();

    @Override
    default int destroy() {
        return -1;
    }
}




class ObservableResource
    extends ResourceImpl
{
    /**
     * Register a listener that fires events whenever a triple
     * which has this resource in subject position is added or removed
     * to the underlying model.
     *
     */
    Runnable addChangeListener() {
        return null;
//    	Statement stmt;
//    	Resource s;
//    	s.getProperty(RDF.type).ge
//    	stmt.changeLiteralObject(o)
        // Register a listener to the model
    }
}



/**
 *
 *
 * @author raven
 *
 */
class RdfCollectionBinding {
    protected Map<Resource, HasValue<?, ?>> resourceToComponent;

    void onCollectionChange(List<? extends Resource> oldValues, List<? extends Resource> newValues) {
        // Compare
    }


}

class RdfBinding {
    HasValue<ValueChangeEvent<?>, ?> hasValue;



    void bindToResource(Resource r) {

    }
}

interface ResourceEditorFactory {

    ResourceEditor create(Node node, ModelState modelState);


//    ResourceEditorImpl appendToComponent(Component target) {
//        return null;
//    }
}


class PropertyEditor {

}

//class ResourceSchema {
//    List<PropertyEditor> availablePropertyEditors;
//}


class EditorModel {
    Model model;

}

interface ResourceSet {
    void createNewItem();
    void linkToItem(Node target);
}


class ModelState {
//    Model model;
    Graph graph;
    Set<Resource> newResources;

    public Graph getGraph() {
        return graph;
    }
//    public Model getModel() {
//        return model;
//    }

    public Set<Resource> getNewResources() {
        return newResources;
    }

}

class ResourceSetImpl
    implements ResourceSet
{
    protected ModelState modelState;

    //protected Graph graph;
    protected Node source;
    protected Graph shadowGraph;

//    protected Node subject;
    protected P_Path0 predicate;


    protected void bindEventListeners() {

        Triple match = TripleUtils.createMatch(source, predicate);
        Function<Triple, Node> targetFn = t -> TripleUtils.getTarget(t, predicate.isForward());


        GraphListener listener = new GraphListenerBatchBase() {

            @Override
            protected void deleteEvent(Graph g, Iterator<Triple> it) {
                Set<Node> changes = Streams.stream(it)
                    .filter(match::matches)
                    .map(targetFn)
                    .collect(Collectors.toSet());
            }

            @Override
            protected void addEvent(Graph g, Iterator<Triple> it) {
                Set<Node> changes = Streams.stream(it)
                        .filter(match::matches)
                        .map(targetFn)
                        .collect(Collectors.toSet());
            }
        };


//        graph.getEventManager().register(listener);
    }


    @Override
    public void createNewItem() {
//        Resource res = modelState.getModel().createResource();
//        modelState.getNewResources().add(res);

        Node newTarget = NodeFactory.createBlankNode();
        Triple newTriple = TripleUtils.create(source, predicate, newTarget);
        modelState.graph.add(newTriple);


    }

    @Override
    public void linkToItem(Node target) {
    }


}


class NodeShape {
    // getArityOfProperty(Property p)
    //
}

interface ComponentFactory {
    boolean isApplicableFor();
}

class PropertyMultiEditor {

    protected Resource s;
    protected Resource p;

    protected Collection<?> oldState;
    protected Map<?, ManagedComponent> valueToComponent;
    protected Function<?, ManagedComponent> componentFactory;


    void bind(Resource r) {
        Model x = ModelFactory.createDefaultModel();
        x.register(new StatementListener() {
            @Override
            public void removedStatement(Statement s) {
            }

            @Override
            public void addedStatement(Statement s) {
            }
        });
    }

}

// Function<Node, >
//interface ResourceDataProvider {
//}

interface ResourceEditor {

}


interface CollectionEditor {

}


class CollectionEditorBase
    implements CollectionEditor
{
    protected ModelState sharedState;
    protected ObservableCollection<Node> collection;
    protected Collection<Node> snapshot;

    protected Registration unregister;

    protected ResourceEditorFactory itemEditorFactory;


    protected HasComponents targetComponent;
    protected Map<Node, ResourceEditor> nodeToEditor;

    public CollectionEditorBase(ObservableCollection<Node> collection) {
        super();
        this.collection = collection;

        snapshot = new ArrayList<>(collection);

        unregister = collection.addPropertyChangeListener(event -> {
            Collection<Node> deletions = null;

            for (Node node : deletions) {
                ResourceEditor editor = nodeToEditor.get(node);
                // editor.close();
            }

            Collection<Node> additions = null;
            for (Node node : additions) {
                ResourceEditor re = itemEditorFactory.create(node, sharedState);
            }
        });
    }
}


//class CollectionEditorTabs
//{
//
//}


//class ResourceEditorState {
//
//}

public class ResourceEditorImpl
    implements ResourceEditor
{
    protected DatasetGraph datasetGraph;
    protected LookupService<Node, Graph> nodeToGraph;
    protected Node rootNode;

    /** Static components will always be added to the generated form */
    protected List<ConditionalComponent> staticComponentFactories;

    /** The list of properties for which to show edit sections */
    protected List<Property> predefinedProperties;

    /** Mapping of properties to specific editors of their value collections */
    protected Function<Property, CollectionEditor> propertyEditor;

    /** The default editor for a property's collection of values */
    protected CollectionEditor defaultCollectionEditor;


    // Mixins are forms for editing certain schema parts of resources
    // e.g. map widget / polygon editor to add spatial attributes.
    // The editor allows
    // availableMixinComponents

    class ConditionalComponent {
        ComponentFactory componentFactory;
        Predicate<Resource> condition;
    }

    class PropertyToEditor {
        Property property;

    }

    List<PropertyToEditor> propertyEditors;
    List<ConditionalComponent> fixedSchemaElements;


    public static Map<Node, String> allocateIris(Resource r) {
        IRIxResolver iriResolver = IRIxResolver.create().noBase().allowRelative(true).build();
        Map<Node, String> result = allocateIris(r, iriResolver);
        return result;
    }

    public static Map<Node, String> allocateIris(Resource r, IRIxResolver iriResolver) {
        Map<Node, String> result = new LinkedHashMap<>();
        allocateIris(r, iriResolver, result, new HashSet<>());

        return result;
    }

    public static String allocateIris(Resource r, IRIxResolver iriResolver, Map<Node, String> map, Set<Node> seen) {
        Node node = r.asNode();

        String result;
        if (map.containsKey(node)) {
            result = map.get(node);
        } else if (seen.contains(node)) {
            throw new RuntimeException("Cycle detected");
        } else {
            seen.add(node);

            ChildResource child = r.as(ChildResource.class);

            Resource parent = child.getParent();

            String iriValue = child.getIriValue();
            String iriMode = child.getIriMode();


            iriValue = iriValue == null ? "" : iriValue;
            boolean isAbsolute = iriMode == null || iriMode.equalsIgnoreCase("absolute");

            if (isAbsolute) {
                result = iriValue;
            } else {
                String parentIri = parent == null
                        ? ""
                        : allocateIris(parent, iriResolver, map, seen);

                result = parentIri + iriValue;
            }

            map.put(node, iriValue);
        }

        return result;
    }


    void bindTo(Node rootNode) {
        this.rootNode = rootNode;

        // Model clientSideModel = ModelFactory.createDefaultModel();



        Resource resource = null;




//        ViewFactory x;
//        x.getViewTemplate().getCondition()

        // Create any applicable manually specified component
        for (ConditionalComponent e : staticComponentFactories) {
            if (e.condition.test(resource)) {
//                Component component = e.componentFactory.createComponent();
            }
        }


        // Create editors for properties in the given orders
        Collection<Node> properties = null;
        Resource s;
        for (Node p : properties) {
//            Collection<Node> os = new SetFromPropertyValues<>(s, p, RDFNode.class);


        }


        // Add generic edit options
    }




    void appendToComponent(Component component) {
    }


    public void test() {
        Resource x;
        Model m;
        DatasetChanges dc;
        DatasetGraphMonitor dgm;


    }
}
