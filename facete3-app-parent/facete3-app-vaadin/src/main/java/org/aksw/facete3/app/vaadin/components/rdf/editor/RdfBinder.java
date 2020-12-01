package org.aksw.facete3.app.vaadin.components.rdf.editor;

import org.aksw.jena_sparql_api.collection.ObservableCollection;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.common.base.Converter;
import com.vaadin.flow.component.HasValue;

public class RdfBinder {
    public static class RdfBindingBuilder<V> {
        protected HasValue<?, V> field;
        protected Converter<V, Node> fieldValueToNode;
        // protected Converter<Triple, V> tripleTo

        // The triple in case the value in the field replaces one
        protected Triple replacedTriple;

        protected ObservableCollection<V> rdfBackedCollection;

    }


    public <V> RdfBindingBuilder forField(HasValue<?, V> field) {
        return null;
    }
}
