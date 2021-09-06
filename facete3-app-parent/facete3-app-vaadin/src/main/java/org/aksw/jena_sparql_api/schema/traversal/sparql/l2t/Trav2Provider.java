package org.aksw.jena_sparql_api.schema.traversal.sparql.l2t;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l2.Travs2.Trav2A;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l2.Travs2.Trav2B;

import com.google.common.graph.Graph;

public interface Trav2Provider<T, S, A extends S, B extends S>
{
    // protected Trav2<T, S, A B> rootNode;

    protected Graph<N>


    default Trav2A<T, S, A, B> newRoot(Path<T> rootPath) {
        A a = mkRoot();
        return new Trav2A<T, S, A, B>(this, rootPath, null, a);
    }

    A mkRoot();

    B toB(Trav2A<T, S, A, B> a, T segment);
    A toA(Trav2B<T, S, A, B> b, T segment);
}
