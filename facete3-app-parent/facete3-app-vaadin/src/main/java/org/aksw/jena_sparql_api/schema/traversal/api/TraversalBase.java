package org.aksw.jena_sparql_api.schema.traversal.api;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;

public class TraversalBase<V, S, P extends Path<S>, T extends Traversal<V, S, P, T>>
    implements Traversal<V, S, P, T>
{
    protected T parent;
    protected P path;
    protected V value;

    public TraversalBase(T parent, P path, V value) {
        super();
        this.parent = parent;
        this.path = path;
        this.value = value;
    }

    @Override
    public P getPath() {
        return path;
    }

    @Override
    public T back() {
        return parent;
    }

    @Override
    public V getValue() {
        return value;
    }

    // coming from /foo: back(/foo/bar)       -> /foo
    // coming from /foo: backAsNext(/foo/bar) -> /foo/bar/..
    // backAsNext = traverse(PARENT)

    @Override
    public T traverse(Path<? extends S> path) {
        // TODO Auto-generated method stub
        return null;
    }

}
