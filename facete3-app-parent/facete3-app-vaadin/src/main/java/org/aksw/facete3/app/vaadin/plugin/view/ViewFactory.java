package org.aksw.facete3.app.vaadin.plugin.view;

import org.aksw.facete3.app.shared.viewselector.ViewTemplate;
import org.apache.jena.rdf.model.Resource;

import com.vaadin.flow.component.Component;

public interface ViewFactory
{
    ViewTemplate getViewTemplate();
    Component createComponent(Resource data);
}
