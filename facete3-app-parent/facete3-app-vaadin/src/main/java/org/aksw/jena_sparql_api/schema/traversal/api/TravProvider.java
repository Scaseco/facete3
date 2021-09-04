package org.aksw.jena_sparql_api.schema.traversal.api;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;

public interface TravProvider<S, V> {
    Trav<S, V> root();

    Trav<S, V> traverse(Trav<S, V> from, S segment);
    Trav<S, V> traverse(Trav<S, V> from, Path<S> path);
}
