package org.aksw.facete3.app.vaadin.plugin.search;

import org.aksw.facete3.app.vaadin.SearchSensitiveRDFConnectionTransform;
import org.aksw.facete3.app.vaadin.providers.SearchProvider;

public class SearchPluginImpl
    implements SearchPlugin
{
    protected SearchProvider searchProvider;
    protected SearchSensitiveRDFConnectionTransform connectionTransform;
//    protected boolean allowMatchEverything;

    public SearchPluginImpl(
            SearchProvider searchProvider,
            SearchSensitiveRDFConnectionTransform connectionTransform) {
        super();
        this.searchProvider = searchProvider;
        this.connectionTransform = connectionTransform;
    }

    @Override
    public SearchProvider getSearchProvider() {
        return searchProvider;
    }

    @Override
    public SearchSensitiveRDFConnectionTransform getConnectionTransform() {
        return connectionTransform;
    }

}
