package org.aksw.facete3.app.vaadin.plugin.search;

import org.aksw.facete3.app.vaadin.SearchSensitiveRDFConnectionTransform;
import org.aksw.facete3.app.vaadin.providers.SearchProvider;

/**
 * A search plugin bundles the following two components:
 * <ul>
 *   <li>SearchProvider: Turns query strings (typically keyword search) to specifications of matching resources.</li>
 *   <li>SearchSensitiveRDFConnectionTransform: A factory for query rewrites based on the specifications created by the search provider.</li>
 * </ul>
 *
 */
public interface SearchPlugin {
    SearchProvider getSearchProvider();
    SearchSensitiveRDFConnectionTransform getConnectionTransform();
}
