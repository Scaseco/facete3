package org.aksw.jena_sparql_api.collection;

public interface ValueChangeListener<T> {
    void propertyChange(ValueChangeEvent<T> evt);

}
