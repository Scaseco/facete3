package org.aksw.facete3.app.shared.concept;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;

public class RDFNodeSpecFromCollectionImpl
    extends RDFNodeSpecFromCollectionBase
{
    protected Collection<? extends RDFNode> rdfNodes;

    public RDFNodeSpecFromCollectionImpl(Collection<? extends RDFNode> rdfNodes) {
        super();
        this.rdfNodes = rdfNodes;
    }

    @Override
    public Collection<? extends RDFNode> getRDFNodes() {
        return rdfNodes;
    }
}
