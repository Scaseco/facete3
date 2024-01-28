package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.vaadin.plugin.search.SearchPlugin;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountDataProvider;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.aksw.facete3.app.vaadin.qualifier.DisplayLabelConfig;
import org.aksw.facete3.app.vaadin.qualifier.FullView;
import org.aksw.facete3.app.vaadin.qualifier.SnippetView;
import org.aksw.jenax.arq.aggregation.BestLiteralConfig;
import org.aksw.jenax.vaadin.label.LabelService;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.shared.PrefixMapping;

import com.vaadin.flow.data.provider.InMemoryDataProvider;

public class FacetedSearchSession {
    protected RDFConnection baseDataConnection;
//  SearchPlugin searchPlugin,
    protected InMemoryDataProvider<SearchPlugin> searchPluginProvider;
    protected PrefixMapping prefixMapping;
    protected Facete3Wrapper facetedQueryConf;
    protected FacetCountProvider facetCountProvider;
    protected FacetValueCountDataProvider facetValueCountProvider;
    protected ItemProvider itemProvider;
    protected ConfigFaceteVaadin config;
    protected @FullView ViewManager viewManagerFull;
    protected @SnippetView ViewManager viewManagerDetail;
    protected @DisplayLabelConfig BestLiteralConfig bestLabelConfig;
    protected LabelService<Node, String> labelMgr;
}
