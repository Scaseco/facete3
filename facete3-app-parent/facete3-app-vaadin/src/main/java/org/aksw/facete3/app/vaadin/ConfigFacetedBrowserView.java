package org.aksw.facete3.app.vaadin;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.facete3.app.vaadin.components.FacetedBrowserView;
import org.aksw.facete3.app.vaadin.plugin.search.SearchPlugin;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountProvider;
import org.aksw.facete3.app.vaadin.qualifier.DisplayLabelConfig;
import org.aksw.facete3.app.vaadin.qualifier.FullView;
import org.aksw.facete3.app.vaadin.qualifier.SnippetView;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderNodeQuery;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataRetriever;
import org.aksw.jenax.arq.aggregation.BestLiteralConfig;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactoryOverSparqlQueryConnection;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.vaadin.component.grid.shacl.VaadinShaclGridUtils;
import org.aksw.jenax.vaadin.label.VaadinRdfLabelMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import com.vaadin.flow.data.provider.InMemoryDataProvider;

/**
 * This is a generic context configuration which declares all DataProviders
 * used by the {@link FacetedBrowserView} Vaadin component.
 *
 * The context also features built-in support for refreshing the data providers
 * by listening to {@link RefreshScopeRefreshedEvent} events
 *
 * The context requires an {@link RDFConnection} to function.
 *
 * @author raven
 *
 */
public class ConfigFacetedBrowserView {

    @Bean
    @Autowired
    public Facete3Wrapper facetedQueryConf(RdfDataSource dataSource) {
        return new Facete3Wrapper(dataSource);
    }

    @Bean
    @Autowired
    public DataProviderNodeQuery itemProvider(
            RdfDataSource dataSource,
            // SparqlQueryConnection baseDataConnection,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            ConfigFaceteVaadin config,
            VaadinRdfLabelMgr labelService
            ) {
//        baseDataConnection = RDFConnectionFactory.connect(DatasetFactory.create());

//        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
//                dataSource.asQef(),
//                // new QueryExecutionFactoryOverSparqlQueryConnection(baseDataConnection),
//                config.getAlternativeLabel(),
//                prefixMapping);

        Model shaclModel = RDFDataMgr.loadModel("r2rml.core.shacl.ttl");

        DataRetriever dataRetriever = VaadinShaclGridUtils.setupRetriever(dataSource, shaclModel);
        // DataProviderNodeQuery dataProvider = new DataProviderNodeQuery(dataSource, conceptSupplier, dataRetriever);

        // VaadinShaclGridUtils.fromShacl(null)
        return new DataProviderNodeQuery(dataSource, () ->  facetedQueryConf.getFacetedQuery().focus().availableValues().baseRelation().toUnaryRelation(), dataRetriever);
    }

//    @Bean
//    @Autowired
//    public ItemProvider itemProvider(
//            SparqlQueryConnection baseDataConnection,
//            PrefixMapping prefixMapping,
//            Facete3Wrapper facetedQueryConf,
//            ConfigFaceteVaadin config) {
////        baseDataConnection = RDFConnectionFactory.connect(DatasetFactory.create());
//
//        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
//                new QueryExecutionFactoryOverSparqlQueryConnection(baseDataConnection),
//                config.getAlternativeLabel(),
//                prefixMapping);
//
//        return new ItemProvider(facetedQueryConf, labelService);
//    }

    @Bean
    @Autowired
    public FacetCountProvider facetCountProvider(
            SparqlQueryConnection baseDataConnection,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            ConfigFaceteVaadin config) {

//        baseDataConnection = RDFConnectionFactory.connect(DatasetFactory.create());

        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
                new QueryExecutionFactoryOverSparqlQueryConnection(baseDataConnection),
                RDFS.label,
                prefixMapping);

        return new FacetCountProvider(facetedQueryConf, labelService);
    }

    @Bean
    @Autowired
    public FacetValueCountProvider facetValueCountProvider(
            SparqlQueryConnection baseDataConnection,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            ConfigFaceteVaadin config) {

        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
                new QueryExecutionFactoryOverSparqlQueryConnection(baseDataConnection),
                RDFS.label,
                prefixMapping);

        FacetValueCountProvider result = new FacetValueCountProvider(facetedQueryConf, labelService);
        return result;
    }


    @Bean
    @Autowired
    public FacetedBrowserView factedBrowserView(
            RdfDataSource dataSource,
            // RDFConnection baseDataConnection,
//            SearchPlugin searchPlugin,
            InMemoryDataProvider<SearchPlugin> searchPluginProvider,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            FacetCountProvider facetCountProvider,
            FacetValueCountProvider facetValueCountProvider,
            // ItemProvider itemProvider,
            DataProviderNodeQuery itemProvider,
            ConfigFaceteVaadin config,
            @FullView ViewManager viewManagerFull,
            @SnippetView ViewManager viewManagerDetail,
            @DisplayLabelConfig BestLiteralConfig bestLabelConfig,
            VaadinRdfLabelMgr labelMgr
    ) {
        return new FacetedBrowserView(
                dataSource,
                //searchPlugin,
                searchPluginProvider,
                prefixMapping,
                facetedQueryConf,
                facetCountProvider,
                facetValueCountProvider,
                itemProvider,
                config,
                viewManagerFull,
                viewManagerDetail,
                bestLabelConfig,
                labelMgr);
    }

    @Bean
    @Autowired
    public RefreshHandler refreshHandler () {
        return new RefreshHandler();
    }

    public static class RefreshHandler
        implements ApplicationListener<RefreshScopeRefreshedEvent>
    {
        // @Autowired protected ItemProvider itemProvider;
        @Autowired protected DataProviderNodeQuery itemProvider;
        @Autowired protected FacetCountProvider facetCountProvider;
        @Autowired protected FacetCountProvider facetValueCountProvider;

        @Autowired protected FacetedBrowserView facetedBrowserView;

        @Override
        public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
            itemProvider.refreshAll();
            facetCountProvider.refreshAll();
            facetValueCountProvider.refreshAll();

            facetedBrowserView.onRefresh();
        }
    }



}


//@Bean
//@Autowired
//public ApplicationListener<ApplicationEvent> genericListener () {
//  return new ApplicationListener<ApplicationEvent>() {
//      @Override
//      public void onApplicationEvent(ApplicationEvent event) {
//          System.out.println("SAW EVENT: " + event);
//      }
//  };
//}

//@EventListener
//public void handleRefreshScopeRefreshedEvent(RefreshScopeRefreshedEvent ev) {
//System.out.println("THIS REFRESH WORKED");
//}

