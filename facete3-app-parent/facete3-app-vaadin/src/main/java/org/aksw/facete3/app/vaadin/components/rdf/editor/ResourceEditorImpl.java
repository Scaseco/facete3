package org.aksw.facete3.app.vaadin.components.rdf.editor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.facete3.app.vaadin.plugin.ManagedComponent;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.DatasetChanges;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphMonitor;
import org.apache.jena.update.UpdateRequest;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;


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




class RdfBinder {

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

class ResourceEditorFactory {


    ResourceEditorImpl appendToComponent(Component target) {
        return null;
    }
}


class PropertyEditor {

}

//class ResourceSchema {
//    List<PropertyEditor> availablePropertyEditors;
//}




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

public class ResourceEditorImpl
    implements ResourceEditor
{

    protected DatasetGraph datasetGraph;
    protected LookupService<Node, Graph> nodeToGraph;
    protected Node rootNode;

    /** Static components will always be added to the generated form */
    protected List<ConditionalComponent> staticComponentFactories;


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





    void bindTo(Node rootNode) {
        this.rootNode = rootNode;

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
