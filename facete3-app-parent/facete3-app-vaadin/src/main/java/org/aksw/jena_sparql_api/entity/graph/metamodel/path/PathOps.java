package org.aksw.jena_sparql_api.entity.graph.metamodel.path;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public interface PathOps<T, P extends Path<T>> {

    P upcast(Path<T> path);

    List<T> getBasePathSegments();
    Comparator<T> getComparator();
    P newPath(boolean isAbsolute, List<T> segments);
    // T requireSubType(Path other);

    /** Create a path from an instance of T.
     * Note that T may not by specific to segments - e.g. a string can denote both a full path or a single segment */
    P newPath(T element);

    /** Create a root path (absolute, no segments) */
    default P newRoot() {
        return newPath(true, Collections.emptyList());
    }

    /** To token for a path to refer to itself, such as '.' */
    T getSelfToken();

    /** The path segment to navigate to the parent, such as '..' */
    T getParentToken();

    /** Serialize a path as a string */
    String toString(P path);

    /** Deserialize a string into a path */
    Path<T> fromString(String str);
}