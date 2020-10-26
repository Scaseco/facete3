package org.aksw.facete3.app.vaadin.config;

import org.aksw.facete3.app.vaadin.plugin.view.ViewFactoryPaper;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManagerImpl;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.springframework.context.annotation.Bean;

public class ConfigViewManager
{

    @Bean
    public ViewManager viewManager(SparqlQueryConnection conn) {
        ViewManagerImpl result = new ViewManagerImpl(conn);

        result.register(new ViewFactoryPaper());

        return result;
    }

}
