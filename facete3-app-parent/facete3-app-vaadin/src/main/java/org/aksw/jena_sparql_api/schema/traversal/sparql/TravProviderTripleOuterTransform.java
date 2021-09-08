package org.aksw.jena_sparql_api.schema.traversal.sparql;

import java.util.function.Function;

import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravAlias;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravDirection;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravProperty;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravTriple;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravValues;
import org.apache.jena.graph.Node;

//public class TravProviderTripleOuterTransform<W, V>
//    implements TravProviderTriple<W, TravTriple<V, ?>>
//{
//    protected TravProviderTriple<V, ?> backend;
//    protected Function<? super V, ? extends W> transform;
//
//    public TravProviderTripleOuterTransform(
//            TravProviderTriple<V, ?> backend,
//            Function<? super V, ? extends W> transform) {
//        super();
//        this.backend = backend;
//        this.transform = transform;
//    }
//
//
//    public static <W, V> TravProviderTriple<W, ?> create(
//            TravProviderTriple<V, ?> backend,
//            Function<? super V, ? extends W> transform) {
//        return new TravProviderTripleOuterTransform<>(backend, transform);
//    }
//
//
//    @Override
//    public TravValues<W, TravTriple<V, ?>> root() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
//    @Override
//    public TravDirection<W, TravTriple<V, ?>> toDirection(TravValues<W, TravTriple<V, ?>> from, Node value) {
//        return new TravDirection<>(
//            this,
//            from.state().path(),
//            from,
//            from.state().traverse(value));
//    }
//
//
//    @Override
//    public TravProperty<W, TravTriple<V, ?>> toProperty(TravDirection<W, TravTriple<V, ?>> from, boolean isFwd) {
//        Node node = isFwd ? TravDirection.FWD : TravDirection.BWD;
//        return new TravProperty<>(
//                this,
//                from.state().path(),
//                from,
//                from.state().traverse(node));
//    }
//
//
//    @Override
//    public TravAlias<W, TravTriple<V, ?>> toAlias(TravProperty<W, TravTriple<V, ?>> from, Node property) {
//        return new TravAlias<>(
//                this,
//                from.state().path(),
//                from,
//                from.state().traverse(property));
//    }
//
//
//    @Override
//    public TravValues<W, TravTriple<V, ?>> toValues(TravAlias<W, TravTriple<V, ?>> from, Node alias) {
//        return new TravValues<>(
//                this,
//                from.state().path(),
//                from,
//                from.state().traverse(alias));
//    }
//
//
//    @Override
//    public W computeValue(TravValues<W, TravTriple<V, ?>> node) {
//        V v = node.state().payload();
//        W result = transform.apply(v);
//        return result;
//    }
//
//
//    @Override
//    public W computeValue(TravDirection<W, TravTriple<V, ?>> node) {
//        V v = node.state().payload();
//        W result = transform.apply(v);
//        return result;
//    }
//
//
//    @Override
//    public W computeValue(TravProperty<W, TravTriple<V, ?>> node) {
//        V v = node.state().payload();
//        W result = transform.apply(v);
//        return result;
//    }
//
//
//    @Override
//    public W computeValue(TravAlias<W, TravTriple<V, ?>> node) {
//        V v = node.state().payload();
//        W result = transform.apply(v);
//        return result;
//    }
//
//
//
//
//
//
///*
//    public static class TravValues2<V, X>
//        extends TravValues<V>
//    {
//        protected X extra;
//
//        public TravValues2(TravProviderTriple<V> provider, Path<Node> path, TravAlias<V> parent, V state, X extra) {
//            super(provider, path, parent, state);
//            this.extra = extra;
//        }
//
//    }
//*/
//
//
//
//}

