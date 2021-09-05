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
public interface TravProviderTriple<S>
{
    TravValues<S> root();

    TravDirection<S> toDirection(TravValues<S> from, Node value);
    TravProperty<S> toProperty(TravDirection<S> from, boolean isFwd);
    TravAlias<S> toAlias(TravProperty<S> from, Node property);
    TravValues<S> toValues(TravAlias<S> from, Node alias);

//    V computeValue(TravValues<V, S> node);
//    V computeValue(TravDirection<V, S> node);
//    V computeValue(TravProperty<V, S> node);
//    V computeValue(TravAlias<V, S> node);
}
