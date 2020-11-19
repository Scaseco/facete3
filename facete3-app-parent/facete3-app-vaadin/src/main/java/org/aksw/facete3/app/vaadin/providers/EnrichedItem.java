package org.aksw.facete3.app.vaadin.providers;

import org.apache.jena.ext.com.google.common.collect.ClassToInstanceMap;
import org.apache.jena.ext.com.google.common.collect.MutableClassToInstanceMap;


public class EnrichedItem<T> {
    protected T item;
    protected ClassToInstanceMap<Object> classToInstanceMap;

    public EnrichedItem(T item) {
        this(item, MutableClassToInstanceMap.create());
    }

    public EnrichedItem(T item, ClassToInstanceMap<Object> classToInstanceMap) {
        super();
        this.item = item;
        this.classToInstanceMap = classToInstanceMap;
    }

    public T getItem() {
        return item;
    }

    public ClassToInstanceMap<Object> getClassToInstanceMap() {
        return classToInstanceMap;
    }
}