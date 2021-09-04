package org.aksw.jena_sparql_api.schema.traversal.api;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;

public interface Trav<S, V> {
    Trav<S, V> back();
    Path<S> getPath();
    V getValue();
    Trav<S, V> traverse(Path<S> path);
    Trav<S, V> traverse(S segment);

}
