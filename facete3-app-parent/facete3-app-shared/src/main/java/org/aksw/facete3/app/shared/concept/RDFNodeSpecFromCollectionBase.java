package org.aksw.facete3.app.shared.concept;

public abstract class RDFNodeSpecFromCollectionBase
    extends RDFNodeSpecBase
    implements RDFNodeSpecFromCollection
{
    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public RDFNodeSpecFromCollection asCollection() {
        return this;
    }
}
