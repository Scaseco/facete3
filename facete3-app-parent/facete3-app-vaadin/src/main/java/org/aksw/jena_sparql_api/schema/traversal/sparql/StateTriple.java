package org.aksw.jena_sparql_api.schema.traversal.sparql;



public class StateTriple<V> {
    public static enum Type {
        VALUE,
        DIRECTION,
        PROPERTY,
        ALIAS
    }

    protected Type type;
    protected V value;

    public Type getType() {
        return type;
    }

    public V getValue() {
        return value;
    }
}
