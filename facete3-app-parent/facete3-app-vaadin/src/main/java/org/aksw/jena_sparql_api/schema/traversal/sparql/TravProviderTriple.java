package org.aksw.jena_sparql_api.schema.traversal.sparql;

import org.aksw.jena_sparql_api.schema.traversal.api.TravProvider;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravAlias;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravDirection;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravProperty;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravValues;
import org.apache.jena.graph.Node;


/**
 * A {@link TravProvider} that offers domain views for triple-like
 * traversal.
 *
 * @author raven
 *
 * @param <V>
 */
public interface TravProviderTriple<V>
{
    TravValues<V> root();

    TravDirection<V> toDirection(TravValues<V> from, Node value);
    TravProperty<V> toProperty(TravDirection<V> from, boolean isFwd);
    TravAlias<V> toAlias(TravProperty<V> from, Node property);
    TravValues<V> toValues(TravAlias<V> from, Node alias);

    V computeValue(TravValues<V> node);
    V computeValue(TravDirection<V> node);
    V computeValue(TravProperty<V> node);
    V computeValue(TravAlias<V> node);
}
