package org.aksw.jena_sparql_api.schema.traversal.sparql;


import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.schema.traversal.api.Trav;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

public class TravTripleViews {

    public interface TravTripleStateComputer<S> {
        S nextState(TravValues<S> from, Node value);
        S nextState(TravDirection<S> from, boolean isFwd);
        S nextState(TravProperty<S> from, Node property);
        S nextState(TravAlias<S> from, Node alias);
    }

    public static class TravTripleStateComputerAlwaysNull<S>
        implements TravTripleStateComputer<S>
    {
        @Override
        public S nextState(TravValues<S> from, Node value) {
            return null;
        }

        @Override
        public S nextState(TravDirection<S> from, boolean isFwd) {
            return null;
        }

        @Override
        public S nextState(TravProperty<S> from, Node property) {
            return null;
        }

        @Override
        public S nextState(TravAlias<S> from, Node alias) {
            return null;
        }
    }



    public interface TravTripleStateVisitor<T, S> {
        T visit(TravValues<S> trav);
        T visit(TravDirection<S> trav);
        T visit(TravProperty<S> trav);
        T visit(TravAlias<S> trav);
    }

    public interface TravTripleVisitor<T> {
        T visit(TravValues<?> trav);
        T visit(TravDirection<?> trav);
        T visit(TravProperty<?> trav);
        T visit(TravAlias<?> trav);
    }


    public static enum TravTripleType {
        VALUES,
        DIRECTION,
        PROPERTY,
        ALIAS
    }

    public static class TravTripleVisitorClassify
        implements TravTripleVisitor<TravTripleType>
    {
        public static final TravTripleVisitorClassify INSTANCE = new TravTripleVisitorClassify();

        @Override
        public TravTripleType visit(TravValues<?> trav) {
            return TravTripleType.VALUES;
        }

        @Override
        public TravTripleType visit(TravDirection<?> trav) {
            return TravTripleType.DIRECTION;
        }

        @Override
        public TravTripleType visit(TravProperty<?> trav) {
            return TravTripleType.PROPERTY;
        }

        @Override
        public TravTripleType visit(TravAlias<?> trav) {
            return TravTripleType.ALIAS;
        }

    }

    public interface TravTriple<S>
         extends Trav<Node, S>
    {
//        Path<Node> path();
//        TravTriple<S> parent();
//        S state();

        TravProviderTriple<S> provider();
        TravTriple<S> traverse(Node segment);
        // V payload();

        <T> T accept(TravTripleVisitor<T> visitor);
        <T> T accept(TravTripleStateVisitor<T, S> visitor);

        default TravTripleType type() {
            return accept(TravTripleVisitorClassify.INSTANCE);
        }
    }

    public static abstract class TravTripleBase<S>
        implements TravTriple<S>
        // extends TravViewBase<V, StateTriple<V>>
    {
        protected TravProviderTriple<S> provider;
        protected Path<Node> path;
        protected S state;

        public TravTripleBase(TravProviderTriple<S> provider, Path<Node> path, S state) {
            super();
            this.provider = provider;
            this.path = path;
            this.state = state;
        }

        /** Providers may use this field to directly put information into instances of this class*/
        public S state() {
            return state;
        }

        @Override
        public TravProviderTriple<S> provider() {
            return provider;
        }

        public Path<Node> path() {
            return path;
        }
    }



    public static class TravValues<S>
        extends TravTripleBase<S>
        // implements TravV<V>
    {
        protected TravAlias<S> parent;

        public TravValues(TravProviderTriple<S> provider, Path<Node> path, TravAlias<S> parent, S state) {
            super(provider, path, state);
            this.parent = parent;
        }

        public String reachingAlias() {
            String result = path.getSegments().isEmpty()
                ? ""
                : path.getFileName().toSegment().getLiteralLexicalForm();

            return result;
        }

        @Override
        public TravAlias<S> parent() {
            return parent;
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravDirection<S> goTo(String iri) {
            return goTo(NodeFactory.createURI(iri));
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravDirection<S> goTo(Resource r) {
            return goTo(r.asNode());
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravDirection<S> goTo(Node value) {
            return traverse(value);
        }


        @Override
        public TravDirection<S> traverse(Node segment) {
            return provider.toDirection(this, segment);
        }

        @Override
        public <T> T accept(TravTripleVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }

        @Override
        public <T> T accept(TravTripleStateVisitor<T, S> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }


    public static class TravDirection<S>
        extends TravTripleBase<S>
    {
        public static final Node FWD = NodeFactory.createURI("urn:fwd"); // NodeValue.TRUE.asNode();
        public static final Node BWD = NodeFactory.createURI("urn:bwd"); // NodeValue.FALSE.asNode();

        protected TravValues<S> parent;

        public TravDirection(TravProviderTriple<S> provider, Path<Node> path, TravValues<S> parent, S state) {
            super(provider, path, state);
            this.parent = parent;
        }


        public Node reachingSource() {
            Node result = path.getFileName().toSegment();
            return result;
        }


        @Override
        public TravValues<S> parent() {
            return parent;
        }

        public TravProperty<S> fwd() {
            return provider.toProperty(this, true);
        }

        public TravProperty<S> bwd() {
            return provider.toProperty(this, false);
        }

        /* short hands */

        public TravAlias<S> fwd(String predicateIri) {
            return fwd().via(predicateIri);
        }

        public TravAlias<S> fwd(Resource property) {
            return fwd().via(property);
        }

        public TravAlias<S> fwd(Node node) {
            return fwd().via(node);
        }



        public TravAlias<S> bwd(String predicateIri) {
            return bwd().via(predicateIri);
        }

        public TravAlias<S> bwd(Resource property) {
            return bwd().via(property);
        }

        public TravAlias<S> bwd(Node node) {
            return bwd().via(node);
        }




        @Override
        public TravProperty<S> traverse(Node segment) {
            TravProperty<S> result;

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
        public <T> T accept(TravTripleVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }

        @Override
        public <T> T accept(TravTripleStateVisitor<T, S> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    public static class TravProperty<S>
        extends TravTripleBase<S>
        // implements TraversalProperty<TravViewAlias<V>>
    {

        protected TravDirection<S> parent;

        public TravProperty(TravProviderTriple<S> provider, Path<Node> path, TravDirection<S> parent, S state) {
            super(provider, path, state);
            this.parent = parent;
        }

        @Override
        public TravDirection<S> parent() {
            return parent;
        }


        public boolean reachedByFwd() {
            // TODO Throw exception if BWD also does not match
            return TravDirection.FWD.equals(path().getFileName().toSegment());
        }



        /** The domain alias for traversial via a predicate */
        public TravAlias<S> via(String iri) {
            return via(NodeFactory.createURI(iri));
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravAlias<S> via(Resource r) {
            return via(r.asNode());
        }

        /** The domain alias for 'going to' a certain value - delegates to traverse */
        public TravAlias<S> via(Node predicate) {
            return traverse(predicate);
        }



        @Override
        public TravAlias<S> traverse(Node segment) {
            return provider.toAlias(this, segment);
        }


        @Override
        public <T> T accept(TravTripleVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }

        @Override
        public <T> T accept(TravTripleStateVisitor<T, S> visitor) {
            T result = visitor.visit(this);
            return result;
        }

    }

    public static class TravAlias<S>
        extends TravTripleBase<S>
    {
        public static final Node DEFAULT_ALIAS = NodeFactory.createLiteral(""); // NodeValue.TRUE.asNode();

        protected TravProperty<S> parent;


        public TravAlias(TravProviderTriple<S> provider, Path<Node> path, TravProperty<S> parent, S state) {
            super(provider, path, state);
            this.parent = parent;
        }


        @Override
        public TravProperty<S> parent() {
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
        public TravValues<S> dft() {
            return traverse(DEFAULT_ALIAS);
        }

        public TravValues<S> alias(String alias) {
            return traverse(NodeFactory.createLiteral(alias));
        }

        @Override
        public TravValues<S> traverse(Node segment) {
            return provider.toValues(this, segment);
        }

//        @Override
//        public V payload() {
//            return provider.computeValue(this);
//        }

        @Override
        public <T> T accept(TravTripleVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }

        @Override
        public <T> T accept(TravTripleStateVisitor<T, S> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

}
