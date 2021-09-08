package org.aksw.jena_sparql_api.schema.traversal.sparql;

import java.util.function.Supplier;

import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.path.core.PathOpsNode;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravAlias;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravDirection;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravProperty;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravTripleStateComputer;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravTripleStateComputerAlwaysNull;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravValues;
import org.apache.jena.graph.Node;

public class TravProviderTripleImpl<S>
    implements TravProviderTriple<S>
{
//    protected Path<Node> rootPath;

//    public TravProviderTripleImpl(Path<Node> rootPath) {
//        super();
//        this.rootPath = rootPath;
//    }

    protected Supplier<S> rootStateSupp;
    protected TravTripleStateComputer<S> stateComputer;

    public TravProviderTripleImpl(Supplier<S> rootStateSupp, TravTripleStateComputer<S> stateComputer) {
        super();
        this.rootStateSupp = rootStateSupp;
        this.stateComputer = stateComputer;
    }

    public static TravProviderTriple<Void> create() {
        return new TravProviderTripleImpl<>(() -> null, new TravTripleStateComputerAlwaysNull<Void>());
    }

    public static <S> TravProviderTriple<S> create(S rootState, TravTripleStateComputer<S> stateComputer) {
        return new TravProviderTripleImpl<>(() -> rootState, stateComputer);
    }

    public static <S> TravProviderTriple<S> create(Supplier<S> rootStateSupp, TravTripleStateComputer<S> stateComputer) {
        return new TravProviderTripleImpl<>(rootStateSupp, stateComputer);
    }

    @Override
    public TravValues<S> root() {
        Path<Node> rootPath = PathOpsNode.get().newRoot();
        S rootState = rootStateSupp.get();
        return new TravValues<>(this, rootPath, null, rootState);
    }

    @Override
    public TravDirection<S> toDirection(TravValues<S> from, Node value) {
        Path<Node> next = from.path().resolve(value);

        S nextState = stateComputer.nextState(from, value);

        return new TravDirection<>(this, next, from, nextState);
    }

    @Override
    public TravProperty<S> toProperty(TravDirection<S> from, boolean isFwd) {
        Node segment = isFwd ? TravDirection.FWD : TravDirection.BWD;
        Path<Node> next = from.path().resolve(segment);

        S nextState = stateComputer.nextState(from, isFwd);

        return new TravProperty<>(this, next, from, nextState);
    }

    @Override
    public TravAlias<S> toAlias(TravProperty<S> from, Node property) {
        Path<Node> next = from.path().resolve(property);

        S nextState = stateComputer.nextState(from, property);

        return new TravAlias<>(this, next, from, nextState);
    }

    @Override
    public TravValues<S> toValues(TravAlias<S> from, Node alias) {
        Path<Node> next = from.path().resolve(alias);

        S nextState = stateComputer.nextState(from, alias);

        return new TravValues<>(this, next, from, nextState);
    }

}
