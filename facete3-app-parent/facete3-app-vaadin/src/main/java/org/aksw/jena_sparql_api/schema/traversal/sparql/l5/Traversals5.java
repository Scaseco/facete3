package org.aksw.jena_sparql_api.schema.traversal.sparql.l5;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.schema.traversal.api.Trav;

/**
 * A rotating traversal of length 5
 *
 * @author raven
 *
 * @param <T> The path segment type of the underlying path object
 * @param <S> The state object of this traversal
 * @param <V> The base class of all values involved in the traversal. May be simply Object.
 */
public class Traversals5 {


    public static abstract class Traversal5Base<
        T,
        S,
        A extends S, B extends S, C extends S, D extends S, E extends S>
        implements Trav<T, S>
    {
        protected Trav5Provider<T, S, A, B, C, D, E> provider;

        protected Path<T> path;

        public Traversal5Base(Trav5Provider<T, S, A, B, C, D, E> provider, Path<T> path) {
            super();
            this.provider = provider;
            this.path = path;
        }

        // public abstract Traversal5Base<T, S, B, C, D, E, A> traverse(T segment);


        @Override
        public Path<T> path() {
            return path;
        }
    }


    public static class Traversal5A<T, S, A extends S, B extends S, C extends S, D extends S, E extends S>
        extends Traversal5Base<T, S, A, B, C, D, E>
    {
        protected Traversal5E<T, S, A, B, C, D, E> parent;
        protected A state;

        public Traversal5A(Trav5Provider<T, S, A, B, C, D, E> provider, Path<T> path,
                Traversal5E<T, S, A, B, C, D, E> parent, A state) {
            super(provider, path);
            this.parent = parent;
            this.state = state;
        }

        @Override
        public A state() {
            return state;
        }


        @Override
        public Traversal5E<T, S, A, B, C, D, E> parent() {
            return parent;
        }

        @Override
        public Traversal5B<T, S, A, B, C, D, E> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            B b = provider.toB(this);
            return new Traversal5B<T, S, A, B, C, D, E>(provider, nextPath, b, this);
        }
    }


    public static class Traversal5B<T, S, A extends S, B extends S, C extends S, D extends S, E extends S>
        extends Traversal5Base<T, S, A, B, C, D, E>
    {
        protected Traversal5A<T, S, A, B, C, D, E> parent;
        protected B state;

        public Traversal5B(Trav5Provider<T, S, A, B, C, D, E> provider, Path<T> path, B state,
                Traversal5A<T, S, A, B, C, D, E> parent) {
            super(provider, path);
            this.state = state;
            this.parent = parent;
        }

        @Override
        public B state() {
            return  state;
        }

        public Traversal5A<T, S, A, B, C, D, E> parent() {
            return parent;
        }

        @Override
        public Traversal5C<T, S, A, B, C, D, E> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            C c = provider.toC(this);
            return new Traversal5C<T, S, A, B, C, D, E>(provider, nextPath, c, this);
        }
    }

    public static class Traversal5C<T, S, A extends S, B extends S, C extends S, D extends S, E extends S>
        extends Traversal5Base<T, S, A, B, C, D, E>
    {
        protected Traversal5B<T, S, A, B, C, D, E> parent;
        protected C state;

        public Traversal5C(Trav5Provider<T, S, A, B, C, D, E> provider, Path<T> path, C state,
                Traversal5B<T, S, A, B, C, D, E> parent) {
            super(provider, path);
            this.state = state;
            this.parent = parent;
        }

        @Override
        public C state() {
            return  state;
        }


        public Traversal5B<T, S, A, B, C, D, E> parent() {
            return parent;
        }

        @Override
        public Traversal5D<T, S, A, B, C, D, E> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            D d = provider.toD(this);
            return new Traversal5D<T, S, A, B, C, D, E>(provider, nextPath, d, this);
        }
    }


    public static class Traversal5D<T, S, A extends S, B extends S, C extends S, D extends S, E extends S>
        extends Traversal5Base<T, S, A, B, C, D, E>
    {
        protected Traversal5C<T, S, A, B, C, D, E> parent;
        protected D state;

        public Traversal5D(Trav5Provider<T, S, A, B, C, D, E> provider, Path<T> path, D state,
                Traversal5C<T, S, A, B, C, D, E> parent) {
            super(provider, path);
            this.state = state;
            this.parent = parent;
        }

        @Override
        public D state() {
            return  state;
        }


        public Traversal5C<T, S, A, B, C, D, E> parent() {
            return parent;
        }

        @Override
        public Traversal5E<T, S, A, B, C, D, E> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            E e = provider.toE(this);
            return new Traversal5E<T, S, A, B, C, D, E>(provider, nextPath, e, this);
        }
    }


    public static class Traversal5E<T, S, A extends S, B extends S, C extends S, D extends S, E extends S>
        extends Traversal5Base<T, S, A, B, C, D, E>
    {
        protected Traversal5D<T, S, A, B, C, D, E> parent;
        protected E state;

        public Traversal5E(Trav5Provider<T, S, A, B, C, D, E> provider, Path<T> path, E state,
                Traversal5D<T, S, A, B, C, D, E> parent) {
            super(provider, path);
            this.state = state;
            this.parent = parent;
        }

        @Override
        public E state() {
            return  state;
        }


        public Traversal5D<T, S, A, B, C, D, E> parent() {
            return parent;
        }

        @Override
        public Traversal5A<T, S, A, B, C, D, E> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            A a = provider.toA(this);
            return new Traversal5A<T, S, A, B, C, D, E>(provider, nextPath, this, a);
        }
    }


}

