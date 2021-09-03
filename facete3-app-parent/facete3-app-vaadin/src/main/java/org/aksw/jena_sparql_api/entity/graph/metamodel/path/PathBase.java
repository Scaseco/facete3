package org.aksw.jena_sparql_api.entity.graph.metamodel.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;


/**
 * Generic base implementation of the path interface.
 * Internally keeps a flag for whether the path is
 * absolute and a list of path segments.
 *
 * All methods of this class are implemented against the {@link PathOps} interface.
 *
 * Complex or verbose type expressions can be abstracted by extending this
 * class as sketched below.
 *
 * <pre>
 * class MyPath extends PathBase<ComplexTypeExpression, MyPath> {
 *    MyPath(PathOps<T, P> pathOps, boolean isAbsolute, List<T> segments) {
 *      super(pathOps, isAbsolute, segments);
 *    }
 * }
 *
 * class MyPathOps implements PathOps<MyPath> {
 *   MyPath newPath(boolean isAbsolute, List<T> segments) {
 *     return new MyPath(this, isAbsolute, segments);
 *   }
 *
 *   // Implement remaining methods
 * }
 * </pre>
 *
 *
 * @author raven
 *
 * @param <T>
 * @param <P>
 */
public class PathBase<T, P extends Path<T>>
    implements Path<T>
{
    protected boolean isAbsolute;
    protected List<T> segments;
    protected List<T> segmentsView;

    protected PathOps<T, P> pathOps;


    public PathBase(PathOps<T, P> pathOps, boolean isAbsolute, List<T> segments) {
        super();
        this.pathOps = pathOps;
        this.isAbsolute = isAbsolute;
        this.segments = segments;
        this.segmentsView = Collections.unmodifiableList(segments);
    }

    protected P newPath(boolean isAbsolute, List<T> segments) {
        return getPathOps().newPath(isAbsolute, segments);
    }

    public PathOps<T, P> getPathOps() {
        return pathOps;
    }


    @Override
    public P toAbsolutePath() {
        P basePath = newPath(true, getPathOps().getBasePathSegments());
        @SuppressWarnings("unchecked")
        P result = (P)basePath.resolve(this);

        return result;
    }


    @Override
    public boolean isAbsolute() {
        return isAbsolute;
    }

    @Override
    public P getRoot() {
        return newPath(true, Collections.emptyList());
    }

    @Override
    public P getFileName() {
        P result = segments.isEmpty()
            ? null
            : newPath(false, Collections.singletonList(segments.get(segments.size() - 1)));
        return result;
    }

    @Override
    public P getParent() {
        P result = segments.isEmpty()
                ? null
                : newPath(isAbsolute(), segments.subList(0, segments.size() - 1));
        return result;
    }

    @Override
    public int getNameCount() {
        return segments.size();
    }

    @Override
    public P getName(int index) {
        return newPath(false, Collections.singletonList(segments.get(index)));
    }

    @Override
    public P subpath(int beginIndex, int endIndex) {
        return newPath(false, segments.subList(endIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path<T> other) {
        boolean result;
        int n = other.getNameCount();
        if (n <= getNameCount()) {
            for (int i = 0; i < n; ++i) {
                String part = other.getName(i).toString();
                if (!Objects.equals(segments.get(i), part)) {
                    result = false;
                }
            }
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public boolean endsWith(Path<T> other) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public P normalize() {
        List<T> tmp = new ArrayList<>();

        Iterator<T> it = segments.iterator();
        while (it.hasNext()) {
            T item = it.next();

            if (isParentToken(item)) {
                if (!tmp.isEmpty()) {
                    // Remove the last item from the newSteps
                    ListIterator<T> delIt = tmp.listIterator(tmp.size());
                    T seenItem = delIt.previous();

                    if (isParentToken(seenItem)) {
                        tmp.add(item);
                    } else {
                        delIt.remove();
                    }
                } else {
                    tmp.add(item);
                }
            } else {
                tmp.add(item);
            }
        }

        return newPath(isAbsolute(), tmp);
    }

    protected boolean isParentToken(T item) {
        T parentToken = getPathOps().getParentToken();
        int v = getPathOps().getComparator().compare(item, parentToken);
        return v == 0;
    }

    public static <T> List<T> toList(Path<T> path) {
        int n = path.getNameCount();
        List<T> result = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            Path<T> tmp = path.getName(i);
            T segment = tmp.getSegments().get(0);
            result.add(segment);
        }

        return result;
    }

    @Override
    public P resolve(Path<T> other) {
        P result;
        if (other.isAbsolute()) {
            result = getPathOps().upcast(other);
        } else {
            List<T> newSteps = new ArrayList<>(segments.size() + other.getNameCount());
            newSteps.addAll(segments);
            newSteps.addAll(toList(other));
            result = newPath(isAbsolute, newSteps);
        }
        return result;
    }

    @Override
    public P relativize(Path<T> other) {
        return newPath(isAbsolute, relativize(this.segments, toList(other), getPathOps().getParentToken()));
    }

//	protected String getParentToken() {
//		return "..";
//	}

//	@Override
//	public URI toUri() {
//
//		// getFileSystem().
//		// new URI(schema, authority, path, qury, fragment);
//	}

//	@Override
//	public Path toAbsolutePath() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public Path toRealPath(LinkOption... options) throws IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}


    // @Override
    public int compareTo(Path<T> other) {
        int result;
        if (other instanceof PathBase) {
            @SuppressWarnings("unchecked")
            PathBase<T, ?> o = (PathBase<T, ?>)other;

            Comparator<T> comparator = getPathOps().getComparator();

            // Sort absolute paths first
            result = (o.isAbsolute ? 0 : 1) - (isAbsolute ? 0 : 1);
            if (result == 0) {
                result = compareLists(segments, o.segments, comparator);
            }
        } else {
            result = -1;
        }
        return result;
    }

    public static <T> int compareLists(List<T> a, List<T> b, Comparator<T> comparator) {
        int result = 0;
        int as = a.size();
        int bs = b.size();
        int n = Math.min(a.size(), b.size());

        for (int i = 0; i < n; ++i) {
            T ai = a.get(i);
            T bi = b.get(i);

            //result = comparator.compare(ai, bi);
            result = comparator.compare(ai, bi);

            if (result != 0) {
                break;
            }
        }

        // If elements were equal then compare by length, shorter first
        result = result != 0
            ? result
            : bs - as;

        return result;
    }

//    public String relativeString() {
//        String sep = getFileSystem().getSeparator();
//        String result = segments.stream().collect(Collectors.joining(sep));
//        return result;
//    }

    @Override
    public String toString() {
        @SuppressWarnings("unchecked")
        String result = getPathOps() .toString((P)this);
        return result;
    }


    public static <T> List<T> relativize(List<T> a, List<T> b, T parentToken) {
        List<T> result = new ArrayList<>();

        // Find number of common elements
        int as = a.size();
        int bs = b.size();

        int i = 0;
        while (i < as && i < bs && Objects.equals(a.get(i), b.get(i))) {
            i++;
        }

        // Add as many 'go to parent folder' items as there are remaining items
        // in a starting from i
        for (int j = i; j < as; j++) {
            result.add(parentToken);
        }

        // Add the elements of b starting with index i
        result.addAll(b.subList(i, bs - 1));
        return result;
    }

    @Override
    public List<T> getSegments() {
        return segmentsView;
    }

    @Override
    public P resolve(T other) {
        return resolve(getPathOps().newPath(other));
    }

    @Override
    public P resolveSibling(T other) {
        return resolveSibling(getPathOps().newPath(other));
    }

    @Override
    public P resolveSibling(Path<T> other) {
        P parent = getParent();
        @SuppressWarnings("unchecked")
        P result = (P)(parent == null ? other : parent.resolve(other));
        return result;
    }
}
