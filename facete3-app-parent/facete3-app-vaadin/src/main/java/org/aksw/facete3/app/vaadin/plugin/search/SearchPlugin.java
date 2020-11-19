package org.aksw.facete3.app.vaadin.plugin.search;

import org.aksw.facete3.app.vaadin.SearchSensitiveRDFConnectionTransform;
import org.aksw.facete3.app.vaadin.providers.SearchProvider;

public interface SearchPlugin {
    SearchProvider getSearchProvider();
    SearchSensitiveRDFConnectionTransform getConnectionTransform();
}
