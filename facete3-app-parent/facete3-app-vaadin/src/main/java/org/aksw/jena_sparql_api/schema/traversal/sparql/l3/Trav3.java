package org.aksw.jena_sparql_api.schema.traversal.sparql.l3;

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
public class Trav3 {


    public interface Trav3StateVisitor<X, T, A, B, C> { // <T, S, A extends S, B extends S, C extends S> {
        X visit(Trav3A<T, ?, A, B, C> trav);
        X visit(Trav3B<T, ?, A, B, C> trav);
        X visit(Trav3C<T, ?, A, B, C> trav);
    }

    public interface Trav3Visitor<X> {
        X visit(Trav3A<?, ?, ?, ?, ?> trav);
        X visit(Trav3B<?, ?, ?, ?, ?> trav);
        X visit(Trav3C<?, ?, ?, ?, ?> trav);
    }

    public static abstract class Trav3Base<
        T,
        S,
        A extends S, B extends S, C extends S>
        implements Trav<T, S>
    {
        protected Trav3Provider<T, S, A, B, C> provider;

        protected Path<T> path;

        public Trav3Base(Trav3Provider<T, S, A, B, C> provider, Path<T> path) {
            super();
            this.provider = provider;
            this.path = path;
        }

        // public abstract Trav3Base<T, S, B, C, A> traverse(T segment);


        @Override
        public Path<T> path() {
            return path;
        }

        public abstract <X> X accept(Trav3Visitor<X> visitor);
        public abstract <X> X accept(Trav3StateVisitor<X, T, A, B, C> visitor);
    }


    public static class Trav3A<T, S, A extends S, B extends S, C extends S>
        extends Trav3Base<T, S, A, B, C>
    {
        protected Trav3C<T, S, A, B, C> parent;
        protected A state;

        public Trav3A(Trav3Provider<T, S, A, B, C> provider, Path<T> path,
                Trav3C<T, S, A, B, C> parent, A state) {
            super(provider, path);
            this.parent = parent;
            this.state = state;
        }

        @Override
        public A state() {
            return state;
        }


        @Override
        public Trav3C<T, S, A, B, C> parent() {
            return parent;
        }

        @Override
        public Trav3B<T, S, A, B, C> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            B b = provider.toB(this, segment);
            return new Trav3B<T, S, A, B, C>(provider, nextPath, this, b);
        }

        @Override
        public <X> X accept(Trav3Visitor<X> visitor) {
            return visitor.visit(this);
        }

        @Override
        public <X> X accept(Trav3StateVisitor<X, T, A, B, C> visitor) {
            return visitor.visit(this);
        }
    }


    public static class Trav3B<T, S, A extends S, B extends S, C extends S>
        extends Trav3Base<T, S, A, B, C>
    {
        protected Trav3A<T, S, A, B, C> parent;
        protected B state;

        public Trav3B(Trav3Provider<T, S, A, B, C> provider, Path<T> path,
                Trav3A<T, S, A, B, C> parent, B state) {
            super(provider, path);
            this.parent = parent;
            this.state = state;
        }

        @Override
        public B state() {
            return  state;
        }

        public Trav3A<T, S, A, B, C> parent() {
            return parent;
        }

        @Override
        public Trav3C<T, S, A, B, C> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            C c = provider.toC(this, segment);
            return new Trav3C<T, S, A, B, C>(provider, nextPath, this, c);
        }

        @Override
        public <T> T accept(Trav3Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public <X> X accept(Trav3StateVisitor<X, T, A, B, C> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Trav3C<T, S, A extends S, B extends S, C extends S>
        extends Trav3Base<T, S, A, B, C>
    {
        protected Trav3B<T, S, A, B, C> parent;
        protected C state;

        public Trav3C(Trav3Provider<T, S, A, B, C> provider, Path<T> path, Trav3B<T, S, A, B, C> parent,
                C state) {
            super(provider, path);
            this.state = state;
            this.parent = parent;
        }

        @Override
        public C state() {
            return  state;
        }


        public Trav3B<T, S, A, B, C> parent() {
            return parent;
        }

        @Override
        public Trav3A<T, S, A, B, C> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            A a = provider.toA(this, segment);
            return new Trav3A<T, S, A, B, C>(provider, nextPath, this, a);
        }

        @Override
        public <T> T accept(Trav3Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public <X> X accept(Trav3StateVisitor<X, T, A, B, C> visitor) {
            return visitor.visit(this);
        }

    }



}

