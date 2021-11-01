package org.aksw.facete3.app.shared.concept;

import org.aksw.jenax.analytics.core.RootedQuery;

public class RDFNodeSpecFromRootedQueryImpl
    extends RDFNodeSpecFromRootedQueryBase
{
    protected RootedQuery rootedQuery;

    public RDFNodeSpecFromRootedQueryImpl(RootedQuery rootedQuery) {
        super();
        this.rootedQuery = rootedQuery;
    }

    @Override
    public RootedQuery getRootedQuery() {
        return rootedQuery;
    }
}
