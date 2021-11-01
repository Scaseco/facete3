package org.aksw.facete3.app.shared.concept;

import java.util.Collection;

import org.aksw.jenax.analytics.core.RootedQuery;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Core interface for the specification of a collection of {@link RDFNode}s by
 * intensional and extensional means.
 * Practically, intensional implies query-based (and thus requires a dataset to evaluate the query upon),
 * whereas extensional implies that the collection is explicitly given and can thus be
 * readily iterated.
 *
 * @author raven
 *
 */
public interface RDFNodeSpec {
    /**
     * If true, then a set of RDFNodes can be directly obtained from this instance.
     *
     * @return
     */
    boolean isCollection();
    RDFNodeSpecFromCollection asCollection();

    /**
     * If true, then this instance holds a {@link RootedQuery} that can be evaluated
     * on a SPARQL endpoint in order to obtain a set of RDFNodes.
     *
     * @return
     */
    boolean isRootedQuery();
    RDFNodeSpecFromRootedQuery asRootedQuery();


    default RootedQuery getRootedQuery() {
        return asRootedQuery().getRootedQuery();
    }

    default Collection<? extends RDFNode> getCollection() {
        return asCollection().getRDFNodes();
    }
}
