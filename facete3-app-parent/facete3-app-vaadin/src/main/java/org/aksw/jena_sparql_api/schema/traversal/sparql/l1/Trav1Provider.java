package org.aksw.jena_sparql_api.schema.traversal.sparql.l1;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l1.Trav1X.Trav1;

public interface Trav1Provider<T, S>
{

    default Trav1<T, S> newRoot(Path<T> rootPath) {
        S a = mkRoot();
        return new Trav1<T, S>(this, rootPath, null, a);
    }

    S mkRoot();

    S next(Trav1<T, S> a, T segment);
}
