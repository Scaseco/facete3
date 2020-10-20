package org.aksw.facete3.app.shared.concept;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;

/**
 * A directly provided collection of {@link RDFNode} objects.
 *
 * @author raven
 *
 */
public interface RDFNodeSpecFromCollection
    extends RDFNodeSpec
{
    Collection<? extends RDFNode> getRDFNodes();
}
