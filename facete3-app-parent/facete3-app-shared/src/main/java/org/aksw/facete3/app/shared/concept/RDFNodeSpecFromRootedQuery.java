package org.aksw.facete3.app.shared.concept;

import org.aksw.jenax.analytics.core.RootedQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

/**
 * An intensional description of a set of {@link RDFNode} objects,
 * i.e. a set of {@link Node}s with related information.
 *
 * A {@link RootedQuery} is essentially a SPARQL CONSTRUCT query with a designated
 * root node.
 *
 * @author raven
 *
 */
public interface RDFNodeSpecFromRootedQuery
    extends RDFNodeSpec
{
    RootedQuery getRootedQuery();
}
