package org.aksw.jena_sparql_api.schema.traversal.sparql;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravAlias;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravDirection;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravProperty;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravValues;
import org.apache.jena.graph.Node;

public abstract class TravProviderTripleBase<V>
    implements TravProviderTriple<V>
{
    protected Path<Node> rootPath;

    public TravProviderTripleBase(Path<Node> rootPath) {
        super();
        this.rootPath = rootPath;
    }

    @Override
    public TravValues<V> root() {
        return new TravValues<>(this, rootPath, null);
    }

    @Override
    public TravDirection<V> toDirection(TravValues<V> from, Node value) {
        Path<Node> next = from.getPath().resolve(value);
        return new TravDirection<>(this, next, from);
    }

    @Override
    public TravProperty<V> toProperty(TravDirection<V> from, boolean isFwd) {
        Node segment = isFwd ? TravDirection.FWD : TravDirection.BWD;
        Path<Node> next = from.getPath().resolve(segment);
        return new TravProperty<>(this, next, from);
    }

    @Override
    public TravAlias<V> toAlias(TravProperty<V> from, Node property) {
        Path<Node> next = from.getPath().resolve(property);
        return new TravAlias<>(this, next, from);
    }

    @Override
    public TravValues<V> toValues(TravAlias<V> from, Node alias) {
        Path<Node> next = from.getPath().resolve(alias);
        return new TravValues<>(this, next, from);
    }
}
