package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.shared.label.LabelUtils;
import org.aksw.facete3.app.vaadin.components.FacetedBrowserView;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.aksw.facete3.app.vaadin.providers.SearchProvider;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.shared.PrefixMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

@Import({ConfigEndpoint.class})
public class ConfigCord19 {

    @Bean
    @Autowired
    public Facete3Wrapper facetedQueryConf(RDFConnection baseDataConnection) {
        return new Facete3Wrapper(baseDataConnection);
    }



    @Bean
    @Autowired
    public ApplicationListener<ApplicationEvent> genericListener () {
        return new ApplicationListener<ApplicationEvent>() {
            @Override
            public void onApplicationEvent(ApplicationEvent event) {
                System.out.println("SAW EVENT: " + event);
            }
        };
    }


    public static class RefreshHandler
        implements ApplicationListener<RefreshScopeRefreshedEvent>
    {
        @Autowired
        protected ItemProvider itemProvider;

        @Override
        public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
            System.out.println("GOT REFRESH EVENT");
            itemProvider.refreshAll();
        }

        //
//      @EventListener
//      public void handleRefreshScopeRefreshedEvent(RefreshScopeRefreshedEvent ev) {
//      }

    }

    @EventListener
    public void handleRefreshScopeRefreshedEvent(RefreshScopeRefreshedEvent ev) {
        System.out.println("THIS REFRESH WORKED");
    }


    @Bean
    @Autowired
    public ItemProvider itemProvider(
            RDFConnection baseDataConnection,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            Config config) {
        //LabelService titleService = new LabelService(baseDataConnection, config.getAlternativeLabel());
        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
                baseDataConnection,
                config.getAlternativeLabel(),
                prefixMapping);

        return new ItemProvider(facetedQueryConf, labelService);
    }


    @Bean
    @Autowired
    public FacetedBrowserView factedBrowserView(
            RDFConnection baseDataConnection,
            SearchProvider searchProvider,
            PrefixMapping prefixMapping,
            Facete3Wrapper facetedQueryConf,
            ItemProvider itemProvider,
            Config config
    ) {
        return new FacetedBrowserView(
                baseDataConnection,
                searchProvider,
                prefixMapping,
                facetedQueryConf,
                itemProvider,
                config);
    }


}
