package org.aksw.facete3.app.vaadin.config;

import org.aksw.facete3.app.vaadin.plugin.view.ViewFactoryDoiPdfViewer;
import org.aksw.facete3.app.vaadin.plugin.view.ViewFactoryLabel;
import org.aksw.facete3.app.vaadin.plugin.view.ViewFactoryPaper;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManagerImpl;
import org.aksw.facete3.app.vaadin.qualifier.FullView;
import org.aksw.facete3.app.vaadin.qualifier.SnippetView;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.springframework.context.annotation.Bean;

public class ConfigViewManager
{

    @Bean
    @FullView
    public ViewManager viewManagerFull(RdfDataSource dataSource) {
        ViewManagerImpl result = new ViewManagerImpl(dataSource.asQef());

        result.register(new ViewFactoryPaper());
        result.register(new ViewFactoryDoiPdfViewer());
        result.register(new ViewFactoryLabel());

        return result;
    }

    @Bean
    @SnippetView
    public ViewManager viewManagerDetail(RdfDataSource dataSource) {
        ViewManagerImpl result = new ViewManagerImpl(dataSource.asQef());

        result.register(new ViewFactoryPaper());
        result.register(new ViewFactoryLabel());


        return result;
    }

}
