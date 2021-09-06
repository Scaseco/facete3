package org.aksw.jena_sparql_api.schema.traversal.sparql.l2t;

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
public class Travs2 {


    interface Trav2<T, S, A extends S, B extends S> {

    }

    public interface Trav2StateVisitor<X, T, A, B> { // <T, S, A extends S, B extends S> {
        X visit(Trav2A<T, ?, A, B> trav);
        X visit(Trav2B<T, ?, A, B> trav);
    }

    public interface Trav2Visitor<X> {
        X visit(Trav2A<?, ?, ?, ?> trav);
        X visit(Trav2B<?, ?, ?, ?> trav);
    }


    //public interface Trav2<>

    public static abstract class Trav2Base<T, S, A extends S, B extends S>
        implements Trav<T, S>
    {
        protected Trav2Provider<T, S, A, B> provider;

        protected Path<T> path;

        public Trav2Base(Trav2Provider<T, S, A, B> provider, Path<T> path) {
            super();
            this.provider = provider;
            this.path = path;
        }

        // public abstract Trav2Base<T, S, B, A> traverse(T segment);


        @Override
        public Path<T> path() {
            return path;
        }

        public abstract <X> X accept(Trav2Visitor<X> visitor);
        public abstract <X> X accept(Trav2StateVisitor<X, T, A, B> visitor);
    }


    public static class Trav2A<T, S, A extends S, B extends S>
        extends Trav2Base<T, S, A, B>
    {
        protected Trav2B<T, S, A, B> parent;
        protected A state;

        public Trav2A(Trav2Provider<T, S, A, B> provider, Path<T> path,
                Trav2B<T, S, A, B> parent, A state) {
            super(provider, path);
            this.parent = parent;
            this.state = state;
        }

        @Override
        public A state() {
            return state;
        }


        @Override
        public Trav2B<T, S, A, B> parent() {
            return parent;
        }

        @Override
        public Trav2B<T, S, A, B> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            B b = provider.toB(this, segment);
            return new Trav2B<T, S, A, B>(provider, nextPath, this, b);
        }

        @Override
        public <X> X accept(Trav2Visitor<X> visitor) {
            return visitor.visit(this);
        }

        @Override
        public <X> X accept(Trav2StateVisitor<X, T, A, B> visitor) {
            return visitor.visit(this);
        }
    }


    public static class Trav2B<T, S, A extends S, B extends S>
        extends Trav2Base<T, S, A, B>
    {
        protected Trav2A<T, S, A, B> parent;
        protected B state;

        public Trav2B(Trav2Provider<T, S, A, B> provider, Path<T> path,
                Trav2A<T, S, A, B> parent, B state) {
            super(provider, path);
            this.parent = parent;
            this.state = state;
        }

        @Override
        public B state() {
            return  state;
        }

        public Trav2A<T, S, A, B> parent() {
            return parent;
        }

        @Override
        public Trav2A<T, S, A, B> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            A a = provider.toA(this, segment);
            return new Trav2A<T, S, A, B>(provider, nextPath, this, a);
        }

        @Override
        public <T> T accept(Trav2Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public <X> X accept(Trav2StateVisitor<X, T, A, B> visitor) {
            return visitor.visit(this);
        }
    }


}

