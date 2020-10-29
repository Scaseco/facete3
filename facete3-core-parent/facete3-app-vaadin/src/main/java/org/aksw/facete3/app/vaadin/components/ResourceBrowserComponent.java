package org.aksw.facete3.app.vaadin.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.aksw.facete3.app.shared.viewselector.ViewTemplate;
import org.aksw.facete3.app.vaadin.plugin.ManagedComponentSimple;
import org.aksw.facete3.app.vaadin.plugin.view.ViewFactory;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.vaadin.alejandro.PdfBrowserViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

public class ResourceBrowserComponent
    extends VerticalLayout
{
    protected ViewManager viewManager;
    protected PreconfiguredTabs tabs;

    public ResourceBrowserComponent(ViewManager viewManager) {
        this.viewManager = viewManager;

        tabs = new PreconfiguredTabs();
        add(tabs);
    }

    void setNode(RDFNode rdfNode) {
        tabs.removeAllTabs();

        if (rdfNode != null) {
            Node node = rdfNode.asNode();
            ResourceComponent tmp = new ResourceComponent(PrefixMapping.Extended, viewManager);
            tmp.setNode(rdfNode);
            tabs.newTab("Data", new ManagedComponentSimple(tmp));

            List<ViewFactory> viewFactories = viewManager.getApplicableViewFactories(node);

            for (ViewFactory viewFactory : viewFactories) {
                ViewTemplate template = viewFactory.getViewTemplate();
                String id = template.getMetadata().getURI();

                Resource initialData = viewManager.fetchData(node, viewFactory);
                Component component = viewFactory.createComponent(initialData);

                tabs.newTab(id, new ManagedComponentSimple(component));
            }


            String classPathPdf = "FacetQueryRewrite.pdf";
            StreamResource streamResource = new StreamResource(classPathPdf, () -> getClass().getClassLoader().getResourceAsStream(classPathPdf));

//            Path filename = Paths.get("/home/raven/Projects/EclipseOld2/geolink-parent/presentation/system.pdf");
//            StreamResource streamResource = new StreamResource(
//                    filename.getFileName().toString(),
//                    () -> {
//                        try {
//                            return Files.newInputStream(filename, StandardOpenOption.READ);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    });

            // show file in pdf viewer
            PdfBrowserViewer pdfViewer = new PdfBrowserViewer(streamResource);
            pdfViewer.setHeight("100%");
            pdfViewer.setWidth("100%");

            tabs.newTab("PDF", new ManagedComponentSimple(pdfViewer));
        }

    }
}
