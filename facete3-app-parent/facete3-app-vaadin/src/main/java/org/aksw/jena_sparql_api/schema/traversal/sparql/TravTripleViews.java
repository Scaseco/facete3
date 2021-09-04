package org.aksw.jena_sparql_api.schema.traversal.sparql;


import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;

public class TravTripleViews {

    public interface TravTripleVisitor<V> {
        <T> T visit(TravValues<V> trav);
        <T> T visit(TravDirection<V> trav);
        <T> T visit(TravProperty<V> trav);
        <T> T visit(TravAlias<V> trav);
    }

    public interface TravTriple<V> {
        Path<Node> getPath();
        TravTriple<V> getParent();

        TravProviderTriple<V> getProvider();
        TravTriple<V> traverse(Node segment);
        V getValue();

        <T> T accept(TravTripleVisitor<V> visitor);
    }

    public static abstract class TravTripleBase<V>
        implements TravTriple<V>
        // extends TravViewBase<V, StateTriple<V>>
    {
        protected TravProviderTriple<V> provider;
        protected Path<Node> path;

        public TravTripleBase(TravProviderTriple<V> provider, Path<Node> path) {
            super();
            this.provider = provider;
            this.path = path;
        }

        @Override
        public TravProviderTriple<V> getProvider() {
            return provider;
        }

        public Path<Node> getPath() {
            return path;
        }
    }

    public static class TravValues<V>
        extends TravTripleBase<V>
    {
        protected TravAlias<V> parent;

        public TravValues(TravProviderTriple<V> provider, Path<Node> path, TravAlias<V> parent) {
            super(provider, path);
            this.parent = parent;
        }

        public TravAlias<V> getParent() {
            return parent;
        }

        @Override
        public TravDirection<V> traverse(Node segment) {
            return provider.toDirection(this, segment);
        }

        @Override
        public V getValue() {
            return provider.computeValue(this);
        }

        @Override
        public <T> T accept(TravTripleVisitor<V> visitor) {
            T result = visitor.visit(this);
            return result;
        }

    }


    public static class TravDirection<V>
        extends TravTripleBase<V>
    {
        public static final Node FWD = NodeValue.TRUE.asNode();
        public static final Node BWD = NodeValue.FALSE.asNode();

        protected TravValues<V> parent;

        public TravDirection(TravProviderTriple<V> provider, Path<Node> path, TravValues<V> parent) {
            super(provider, path);
            this.parent = parent;
        }

        public TravValues<V> getParent() {
            return parent;
        }

        public TravProperty<V> fwd() {
            return provider.toProperty(this, true);
        }

        public TravProperty<V> bwd() {
            return provider.toProperty(this, false);
        }

        @Override
        public TravProperty<V> traverse(Node segment) {
            TravProperty<V> result;

            if (FWD.equals(segment)) {
                result = fwd();
            } else if (BWD.equals(segment)) {
                result = bwd();
            } else {
                throw new IllegalArgumentException();
            }

            return result;
        }

        @Override
        public V getValue() {
            return provider.computeValue(this);
        }

        @Override
        public <T> T accept(TravTripleVisitor<V> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    public static class TravProperty<V>
        extends TravTripleBase<V>
        // implements TraversalProperty<TravViewAlias<V>>
    {

        protected TravDirection<V> parent;

        public TravProperty(TravProviderTriple<V> provider, Path<Node> path, TravDirection<V> parent) {
            super(provider, path);
            this.parent = parent;
        }

        public TravDirection<V> getParent() {
            return parent;
        }

        @Override
        public TravAlias<V> traverse(Node segment) {
            return provider.toAlias(this, segment);
        }

        @Override
        public V getValue() {
            return provider.computeValue(this);
        }

        @Override
        public <T> T accept(TravTripleVisitor<V> visitor) {
            T result = visitor.visit(this);
            return result;
        }

    }

    public static class TravAlias<V>
        extends TravTripleBase<V>
    {
        protected TravProperty<V> parent;


        public TravAlias(TravProviderTriple<V> provider, Path<Node> path, TravProperty<V> parent) {
            super(provider, path);
            this.parent = parent;
        }

        public TravProperty<V> getParent() {
            return parent;
        }

        @Override
        public TravValues<V> traverse(Node segment) {
            return provider.toValues(this, segment);
        }

        @Override
        public V getValue() {
            return provider.computeValue(this);
        }

        @Override
        public <T> T accept(TravTripleVisitor<V> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

}
