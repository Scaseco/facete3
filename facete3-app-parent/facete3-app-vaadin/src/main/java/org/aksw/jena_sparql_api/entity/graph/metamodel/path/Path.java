package org.aksw.jena_sparql_api.entity.graph.metamodel.path;

import java.util.List;

/**
 * An interface summarized from {@link java.nio.file.Path}.
 * Purely provides the path manipulation functions - without FileSystem dependency.
 *
 * This enables the use of all the
 * path operations in different contexts, such as
 * for use as keys in hierarchical representations of RDF data (e.g. TreeGrids).
 *
 * @author Claus Stadler
 *
 * @param <T> The types of segments in the path
 */
public interface Path<T> {
    Path<T> toAbsolutePath();
    boolean isAbsolute();
    List<T> getSegments();
    Path<T> getRoot();
    Path<T> getFileName();
    Path<T> getParent();
    int getNameCount();
    Path<T> getName(int index);

    Path<T> subpath(int beginIndex, int endIndex);

    boolean startsWith(Path<T> other);
    boolean endsWith(Path<T> other);

    Path<T> normalize();
    Path<T> resolve(String other);
    Path<T> resolve(T other);
    Path<T> resolve(Path<T> other);

    Path<T> resolveSibling(String other);
    Path<T> resolveSibling(T other);
    Path<T> resolveSibling(Path<T> other);

    Path<T> relativize(Path<T> other);
}
