package org.aksw.jena_sparql_api.schema.traversal.api;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;

public class TravBase<S, V>
    implements Trav<S, V>
{
    protected Trav<S, V> parent;
    protected Path<S> path;
    protected V value;

    protected TravProvider<S, V> provider;

    public TravBase(TravProvider<S, V> provider, Trav<S, V> parent, Path<S> path, V value) {
        super();
        this.provider = provider;
        this.parent = parent;
        this.path = path;
        this.value = value;
    }

    @Override
    public Trav<S, V> parent() {
        return parent;
    }

    @Override
    public Path<S> path() {
        return path;
    }
    @Override
    public V state() {
        return value;
    }

//    @Override
//    public Trav<S, V> traverse(Path<S> path) {
//        return provider.traverse(parent, path);
//    }

    @Override
    public Trav<S, V> traverse(S segment) {
        return provider.traverse(parent, segment);
    }


}
