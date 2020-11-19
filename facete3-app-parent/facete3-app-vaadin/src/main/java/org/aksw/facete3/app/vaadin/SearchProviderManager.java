package org.aksw.facete3.app.vaadin;

import java.util.Collection;

import org.aksw.facete3.app.vaadin.providers.SearchProvider;

public interface SearchProviderManager
//	implements
{
    Collection<SearchProvider> getAvailableSearchProviders();
    void setActiveSearchProvider(SearchProvider searchProvider);
    SearchProvider getActiveSearchProvider();
}
