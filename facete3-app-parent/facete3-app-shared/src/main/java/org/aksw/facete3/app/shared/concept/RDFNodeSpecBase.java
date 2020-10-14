package org.aksw.facete3.app.shared.concept;

public abstract class RDFNodeSpecBase
    implements RDFNodeSpec
{

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public RDFNodeSpecFromCollection asCollection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPartitionedQuery() {
        return false;
    }

    @Override
    public RDFNodeSpecFromPartitionedQuery asPartitionedQuery() {
        throw new UnsupportedOperationException();
    }
}
