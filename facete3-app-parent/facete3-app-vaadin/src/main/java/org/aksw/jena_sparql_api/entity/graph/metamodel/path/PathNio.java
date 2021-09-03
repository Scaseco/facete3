package org.aksw.jena_sparql_api.entity.graph.metamodel.path;

import java.util.List;

/**
 * Implementation of the {@link Path} interface for {@link java.nio.file.Path}.
 * Use {@link #wrapInternal(java.nio.file.Path)} to create an instance of this class.
 *
 * @author Claus Stadler
 *
 */
public class PathNio implements Path<String>
{
    protected java.nio.file.Path delegate;

    protected PathNio(java.nio.file.Path delegate) {
        super();
        this.delegate = delegate;
    }

    public java.nio.file.Path getDelegate() {
        return delegate;
    }

    public static PathNio wrap(java.nio.file.Path nioPath) {
        return new PathNio(nioPath);
    }

    protected PathNio wrapInternal(java.nio.file.Path nioPath) {
        return new PathNio(nioPath);
    }

    @Override
    public Path<String> toAbsolutePath() {
        return wrapInternal(getDelegate().toAbsolutePath());
    }

    @Override
    public boolean isAbsolute() {
        return getDelegate().isAbsolute();
    }

    @Override
    public List<String> getSegments() {
        return PathBase.toList(this);
    }

    @Override
    public Path<String> getRoot() {
        return wrapInternal(getDelegate().getRoot());
    }

    @Override
    public Path<String> getFileName() {
        return wrapInternal(getDelegate().getFileName());
    }

    @Override
    public Path<String> getParent() {
        return wrapInternal(getDelegate().getParent());
    }

    @Override
    public int getNameCount() {
        return getDelegate().getNameCount();
    }

    @Override
    public Path<String> getName(int index) {
        return wrapInternal(getDelegate().getName(index));
    }

    @Override
    public Path<String> subpath(int beginIndex, int endIndex) {
        return wrapInternal(getDelegate().subpath(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path<String> other) {
        return getDelegate().startsWith(((PathNio)other).getDelegate());
    }

    @Override
    public boolean endsWith(Path<String> other) {
        return getDelegate().endsWith(((PathNio)other).getDelegate());
    }

    @Override
    public Path<String> normalize() {
        return wrapInternal(getDelegate().normalize());
    }

    @Override
    public Path<String> resolve(String other) {
        return wrapInternal(getDelegate().resolve(other));
    }

    @Override
    public Path<String> resolve(Path<String> other) {
        return wrapInternal(getDelegate().resolve(((PathNio)other).getDelegate()));
    }

    @Override
    public Path<String> resolveSibling(String other) {
        return wrapInternal(getDelegate().resolveSibling(other));
    }

    @Override
    public Path<String> resolveSibling(Path<String> other) {
        return wrapInternal(getDelegate().resolveSibling(((PathNio)other).getDelegate()));
    }

    @Override
    public Path<String> relativize(Path<String> other) {
        return wrapInternal(getDelegate().relativize(((PathNio)other).getDelegate()));
    }

}
