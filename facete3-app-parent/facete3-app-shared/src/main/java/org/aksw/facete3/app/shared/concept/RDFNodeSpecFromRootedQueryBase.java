package org.aksw.facete3.app.shared.concept;

public abstract class RDFNodeSpecFromRootedQueryBase
    extends RDFNodeSpecBase
    implements RDFNodeSpecFromRootedQuery
{
    @Override
    public boolean isRootedQuery() {
        return true;
    }

    @Override
    public RDFNodeSpecFromRootedQuery asRootedQuery() {
        return this;
    }
}
