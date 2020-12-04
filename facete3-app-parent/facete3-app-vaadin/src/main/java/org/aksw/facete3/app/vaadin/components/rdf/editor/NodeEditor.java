package org.aksw.facete3.app.vaadin.components.rdf.editor;

import org.apache.jena.graph.Node;

/** Get or set an aspect of a node (e.g. the lexical value, the language tag or the datatype) using a string form. */
public interface NodeEditor
{
    /** Initialize the editor's state from the given node */
    void readBean(Node node);

    /** Get the node based on the editor's state */
    Node getNode();

    /** Get the string aspect of the node */
    String getText(Node node);

    /** Set the string aspect of the node */
    Node setText(String str);
}
