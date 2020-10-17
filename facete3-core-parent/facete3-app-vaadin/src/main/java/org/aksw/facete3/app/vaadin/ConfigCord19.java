package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.vaadin.components.FacetedBrowserView;
import org.aksw.facete3.app.vaadin.providers.SearchProvider;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.shared.PrefixMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({ConfigEndpoint.class})
public class ConfigCord19 {

    @Bean
    @Autowired
    public FacetedBrowserView factedBrowserView(
            RDFConnection baseDataConnection,
            SearchProvider searchProvider,
            PrefixMapping prefixMapping,
            Config config
    ) {
        return new FacetedBrowserView(
                baseDataConnection,
                searchProvider,
                prefixMapping,
                config);
    }

}
