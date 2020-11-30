package org.aksw.facete3.app.vaadin.components.rdf.editor;

import java.util.Collection;

import org.apache.jena.graph.Node;

public interface NodeBackedCollection<T>
    extends Collection<T>
{
    /**
     * A collection of the raw backing Node objects.
     * For example, a Collection<String> may be backed by an RDF resource with a property
     * that leads to a collection of IRIs or literals of type xsd:string.
     *
     * @return
     */
    Collection<Node> getRawCollection();
}
