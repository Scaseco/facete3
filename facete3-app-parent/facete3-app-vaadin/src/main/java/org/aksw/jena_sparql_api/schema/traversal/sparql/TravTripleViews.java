package org.aksw.jena_sparql_api.schema.traversal.sparql;


import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

public class TravTripleViews {

    public interface TravTripleVisitor<V> {
        <T> T visit(TravValues<V> trav);
        <T> T visit(TravDirection<V> trav);
        <T> T visit(TravProperty<V> trav);
        <T> T visit(TravAlias<V> trav);
    }

    public interface TravTriple<V> {
        Path<Node> path();
        TravTriple<V> parent();

        TravProviderTriple<V> provider();
        TravTriple<V> traverse(Node segment);
        V payload();

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
        public TravProviderTriple<V> provider() {
            return provider;
        }

        public Path<Node> path() {
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

        public String reachingAlias() {
            String result = path.getSegments().isEmpty()
                ? ""
                : path.getFileName().toSegment().getLiteralLexicalForm();

            return result;
        }

        @Override
        public TravAlias<V> parent() {
            return parent;
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravDirection<V> goTo(String iri) {
            return goTo(NodeFactory.createURI(iri));
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravDirection<V> goTo(Resource r) {
            return goTo(r.asNode());
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravDirection<V> goTo(Node value) {
            return traverse(value);
        }


        @Override
        public TravDirection<V> traverse(Node segment) {
            return provider.toDirection(this, segment);
        }

        @Override
        public V payload() {
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
        public static final Node FWD = NodeFactory.createURI("urn:fwd"); // NodeValue.TRUE.asNode();
        public static final Node BWD = NodeFactory.createURI("urn:bwd"); // NodeValue.FALSE.asNode();

        protected TravValues<V> parent;

        public TravDirection(TravProviderTriple<V> provider, Path<Node> path, TravValues<V> parent) {
            super(provider, path);
            this.parent = parent;
        }


        public Node reachingSource() {
            Node result = path.getFileName().toSegment();
            return result;
        }


        @Override
        public TravValues<V> parent() {
            return parent;
        }

        public TravProperty<V> fwd() {
            return provider.toProperty(this, true);
        }

        public TravProperty<V> bwd() {
            return provider.toProperty(this, false);
        }

        /* short hands */

        public TravAlias<V> fwd(String predicateIri) {
            return fwd().via(predicateIri);
        }

        public TravAlias<V> fwd(Resource property) {
            return fwd().via(property);
        }

        public TravAlias<V> fwd(Node node) {
            return fwd().via(node);
        }



        public TravAlias<V> bwd(String predicateIri) {
            return bwd().via(predicateIri);
        }

        public TravAlias<V> bwd(Resource property) {
            return bwd().via(property);
        }

        public TravAlias<V> bwd(Node node) {
            return bwd().via(node);
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
        public V payload() {
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

        @Override
        public TravDirection<V> parent() {
            return parent;
        }


        public boolean reachedByFwd() {
            // TODO Throw exception if BWD also does not match
            return TravDirection.FWD.equals(path().getFileName().toSegment());
        }



        /** The domain alias for traversial via a predicate */
        public TravAlias<V> via(String iri) {
            return via(NodeFactory.createURI(iri));
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravAlias<V> via(Resource r) {
            return via(r.asNode());
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravAlias<V> via(Node predicate) {
            return traverse(predicate);
        }



        @Override
        public TravAlias<V> traverse(Node segment) {
            return provider.toAlias(this, segment);
        }

        @Override
        public V payload() {
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
        public static final Node DEFAULT_ALIAS = NodeFactory.createLiteral(""); // NodeValue.TRUE.asNode();

        protected TravProperty<V> parent;


        public TravAlias(TravProviderTriple<V> provider, Path<Node> path, TravProperty<V> parent) {
            super(provider, path);
            this.parent = parent;
        }


        @Override
        public TravProperty<V> parent() {
            return parent;
        }


        public Node reachingPredicate() {
            Node result = path.getFileName().toSegment();
            return result;
        }

        public String reachingPredicateIri() {
            return reachingPredicate().getURI();
        }


        /** default alias */
        public TravValues<V> dft() {
            return traverse(DEFAULT_ALIAS);
        }

        public TravValues<V> alias(String alias) {
            return traverse(NodeFactory.createLiteral(alias));
        }

        @Override
        public TravValues<V> traverse(Node segment) {
            return provider.toValues(this, segment);
        }

        @Override
        public V payload() {
            return provider.computeValue(this);
        }

        @Override
        public <T> T accept(TravTripleVisitor<V> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

}
