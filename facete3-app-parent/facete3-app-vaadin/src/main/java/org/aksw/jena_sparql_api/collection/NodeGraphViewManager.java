package org.aksw.jena_sparql_api.collection;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Set;

import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.expr.Expr;

/*
 * Bulk data retrieval: There should be some 'turn-based' mechanism where all components
 * that need data can state their need.
 *
 * DataConsumer can request the start of a data retrieval on the DataProvider.
 * The provider then asks all other known consumers to express their needs as well.
 *
 *
 *
 */


class GraphViewManagerImpl {
    protected RDFConnection conn;

    protected Set<SubGraphView> subGraphViews;

    /**
     * Fetch data for all graph views from the backend
     *
     */
    public void refresh() {
        //Slot<SubGraphView> slot = subGraphViews.newSlot()

        for (SubGraphView subGraphView : subGraphViews) {
            // fetch data
            Graph newState = fetchData(subGraphView);

            // Update the subGraphView with the diff
            //SetDiff<Triple> diff =
            //subGraphView.

            // Should postpone events be a flag on a delegating event listener?
//            subGraphView.setPostposeEvents(true);
            // perform update
//            subGraphView.setPostposeEvents(false);


        }

    }

    public Graph fetchData(SubGraphView subGraphView) {
        return null;
    }
}


interface PropertyField
    extends GraphField
{
    Node getProperty();

}


interface GraphFieldPredicateLangString {
    Node getProperty();

    String getWriteLanguageTag();
    Set<String> getReadLanguageTags();
}

interface GraphFieldPredicateDatatype {
    Node getProperty();
    Set<String> getReadDatatypes();
    String setWriteDatatype();
}



interface GraphField {
    /** An expression in terms of ?p and ?o that intensionally restricts a resource's triples
     * to those for which the expression evaluates to true */
    Expr getExpr();

    /**
     * Whether the field matches triples in forwards or backwards direction
     *
     * @return
     */
    boolean isForward();


    /** Create an observable graph view for only the triples that match the field */
    SubGraphView bindGraph(Node source, Graph graph);

    ObservableSet<Node> bindValues(Node source, Graph graph);
    // ObservableSet<Node> getValue(Node node, GraphMonitor graph);
}




interface NodeGraphViewFactory {

}

public interface NodeGraphViewManager {
    NodeGraphViewFactory getGraphView(Node node);

}

interface DataRetriever {
    // void requestAsync(Node node, NodeSchema schema);
    void requestRetrieval(Node node, NodeSchema schema);
}


interface DataRequest {

}

interface DataConsumer {
}





class Foo
implements PropertyEditor
{

@Override
public void setValue(Object value) {
    // TODO Auto-generated method stub

}

@Override
public Object getValue() {
    // TODO Auto-generated method stub
    return null;
}

@Override
public boolean isPaintable() {
    return false;
}

@Override
public void paintValue(Graphics gfx, Rectangle box) {
}

@Override
public String getJavaInitializationString() {
    return null;
}

@Override
public String getAsText() {
    // TODO Auto-generated method stub
    return null;
}

@Override
public void setAsText(String text) throws IllegalArgumentException {
    // TODO Auto-generated method stub

}

@Override
public String[] getTags() {
    // TODO Auto-generated method stub
    return null;
}

@Override
public Component getCustomEditor() {
    // TODO Auto-generated method stub
    return null;
}

@Override
public boolean supportsCustomEditor() {
    // TODO Auto-generated method stub
    return false;
}

@Override
public void addPropertyChangeListener(PropertyChangeListener listener) {
    // TODO Auto-generated method stub

}

@Override
public void removePropertyChangeListener(PropertyChangeListener listener) {
    // TODO Auto-generated method stub

}
}
