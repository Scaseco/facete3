package org.aksw.jena_sparql_api.schema.traversal.api;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;

public interface Traversal<
    V,
    S,
    P extends Path<S>,
    T extends Traversal<V, S, P, T>> {

    P getPath();
    T back();
    V getValue();

    T traverse(Path<? extends S> path);
}
