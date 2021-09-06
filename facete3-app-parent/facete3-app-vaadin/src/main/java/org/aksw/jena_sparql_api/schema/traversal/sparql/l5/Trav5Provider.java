package org.aksw.jena_sparql_api.schema.traversal.sparql.l5;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l5.Traversals5.Traversal5A;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l5.Traversals5.Traversal5B;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l5.Traversals5.Traversal5C;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l5.Traversals5.Traversal5D;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l5.Traversals5.Traversal5E;

public interface Trav5Provider<T, S, A extends S, B extends S, C extends S, D extends S, E extends S>
{

    default Traversal5A<T, S, A, B, C, D, E> newRoot(Path<T> rootPath) {
        A a = mkRoot();
        return new Traversal5A<T, S, A, B, C, D, E>(this, rootPath, null, a);
    }

    A mkRoot();

    B toB(Traversal5A<T, S, A, B, C, D, E> a);
    C toC(Traversal5B<T, S, A, B, C, D, E> b);
    D toD(Traversal5C<T, S, A, B, C, D, E> c);
    E toE(Traversal5D<T, S, A, B, C, D, E> d);
    A toA(Traversal5E<T, S, A, B, C, D, E> e);
}
