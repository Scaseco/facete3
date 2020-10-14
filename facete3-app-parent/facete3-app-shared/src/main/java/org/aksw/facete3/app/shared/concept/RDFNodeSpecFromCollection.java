package org.aksw.facete3.app.shared.concept;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;

public interface RDFNodeSpecFromCollection
    extends RDFNodeSpec
{
    Collection<? extends RDFNode> getRDFNodes();
}
