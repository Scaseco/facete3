package org.aksw.jena_sparql_api.schema.traversal.sparql.l1;

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
public class Trav1X {


    public interface Trav1StateVisitor<X, T, S> { // <T, S, A extends S, B extends S> {
        X visit(Trav1<T, S> trav);
    }

    public interface Trav1Visitor<X> {
        X visit(Trav1<?, ?> trav);
    }

    public static abstract class Trav1Base<T, S>
        implements Trav<T, S>
    {
        protected Trav1Provider<T, S> provider;
        protected Path<T> path;

        public Trav1Base(Trav1Provider<T, S> provider, Path<T> path) {
            super();
            this.provider = provider;
            this.path = path;
        }

        // public abstract Trav1ase<T, S, B, A> traverse(T segment);


        @Override
        public Path<T> path() {
            return path;
        }

        public abstract <X> X accept(Trav1Visitor<X> visitor);
        public abstract <X> X accept(Trav1StateVisitor<X, T, S> visitor);
    }


    public static class Trav1<T, S>
        extends Trav1Base<T, S>
    {
        protected Trav1<T, S> parent;
        protected S state;

        public Trav1(Trav1Provider<T, S> provider, Path<T> path,
                Trav1<T, S> parent, S state) {
            super(provider, path);
            this.parent = parent;
            this.state = state;
        }

        @Override
        public S state() {
            return state;
        }


        @Override
        public Trav1<T, S> parent() {
            return parent;
        }

        @Override
        public Trav1<T, S> traverse(T segment) {
            Path<T> nextPath = path.resolve(segment);
            S a = provider.next(this, segment);
            return new Trav1<T, S>(provider, nextPath, this, a);
        }

        @Override
        public <X> X accept(Trav1Visitor<X> visitor) {
            return visitor.visit(this);
        }

        @Override
        public <X> X accept(Trav1StateVisitor<X, T, S> visitor) {
            return visitor.visit(this);
        }
    }



}

