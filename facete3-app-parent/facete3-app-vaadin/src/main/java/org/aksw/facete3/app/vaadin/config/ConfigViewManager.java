package org.aksw.facete3.app.vaadin.config;

import org.aksw.facete3.app.vaadin.plugin.view.ViewFactoryDoiPdfViewer;
import org.aksw.facete3.app.vaadin.plugin.view.ViewFactoryPaper;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManagerImpl;
import org.aksw.facete3.app.vaadin.qualifier.DetailView;
import org.aksw.facete3.app.vaadin.qualifier.FullView;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.springframework.context.annotation.Bean;

public class ConfigViewManager
{

    @Bean
    @FullView
    public ViewManager viewManagerFull(SparqlQueryConnection conn) {
        ViewManagerImpl result = new ViewManagerImpl(conn);

        result.register(new ViewFactoryPaper());
        result.register(new ViewFactoryDoiPdfViewer());

        return result;
    }

    @Bean
    @DetailView
    public ViewManager viewManagerDetail(SparqlQueryConnection conn) {
        ViewManagerImpl result = new ViewManagerImpl(conn);

        result.register(new ViewFactoryPaper());

        return result;
    }

}
