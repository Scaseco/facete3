package org.aksw.jena_sparql_api.schema.traversal.api;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;

public interface Trav<S, V> {
    Trav<S, V> parent();
    Path<S> path();
    V state();
    // Trav<S, V> traverse(Path<S> path);
    Trav<S, V> traverse(S segment);

    default S reachingSegment() {
        Path<S> path = path();
        S result = path.getNameCount() == 0
                ? null
                : path.getFileName().toSegment();
        return result;
    }
}
