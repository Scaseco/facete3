package org.aksw.facete3.app.vaadin.components;

import java.util.List;
import java.util.function.Function;

import org.aksw.facete3.app.shared.viewselector.ViewTemplate;
import org.aksw.facete3.app.vaadin.plugin.ManagedComponentSimple;
import org.aksw.facete3.app.vaadin.plugin.view.ViewFactory;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.server.StreamResource;

import software.xdev.vaadin.maps.leaflet.flow.LMap;

public class ResourceBrowserComponent
    extends VerticalLayout
{
    protected ViewManager viewManager;
    protected PreconfiguredTabs tabs;
    protected Function<? super RDFNode, ? extends String> viewMetadataToLabel;

    protected RDFNode activeRdfNode = null;

    /** Generic view factory to browse the raw data of a resource */
    protected ViewFactory defaultViewFactory;

    public ResourceBrowserComponent(
            ViewManager viewManager,
            Function<? super RDFNode, ? extends String> viewMetadataToLabel,
            ViewFactory defaultViewFactory) {
        this.viewManager = viewManager;
        this.viewMetadataToLabel = viewMetadataToLabel;
        this.defaultViewFactory = defaultViewFactory;

        tabs = new PreconfiguredTabs();


        Button expand = new Button(new Icon(VaadinIcon.EXPAND_FULL));
        expand.addClickListener(event -> {
            Dialog dialog = new Dialog();

            dialog.addDialogCloseActionListener(ev -> {
                add(tabs);
                dialog.close();
            });

//            dialog.setMinHeight("80%");
//            dialog.setMinWidth("80%");
            dialog.setHeight("calc(100vh - (2*var(--lumo-space-m)))");
            dialog.setWidth("calc(100vw - (4*var(--lumo-space-m)))");
            dialog.open();

            ResourceBrowserComponent newBrowser = new ResourceBrowserComponent(viewManager, viewMetadataToLabel, defaultViewFactory);
            newBrowser.setWidthFull();
            newBrowser.setHeightFull();

            dialog.add(newBrowser);
            newBrowser.setNode(getNode());
        });

        add(expand);
        add(tabs);
    }

    public void resetView() {
        // Set the node again which triggers an update of the view
        setNode(getNode());
    }


    public RDFNode getNode() {
        return activeRdfNode;
    }

    public void setNode(RDFNode rdfNode) {
        activeRdfNode = rdfNode;

        String selectedTabId = tabs.getSelectedTabId();
        tabs.removeAllTabs();

        if (rdfNode != null) {

            Node node = rdfNode.asNode();

            Component tmp = defaultViewFactory.createComponent(rdfNode);

            // ResourceComponent tmp = new ResourceComponent(PrefixMapping.Extended, viewManager);
            // tmp.setNode(rdfNode);
            tabs.newTab("data", "Data", new ManagedComponentSimple(tmp));

            List<ViewFactory> viewFactories = viewManager.getApplicableViewFactories(node);

            for (ViewFactory viewFactory : viewFactories) {
                ViewTemplate template = viewFactory.getViewTemplate();
                Resource metadata = template.getMetadata();
                String id = template.getMetadata().getURI();

                String label = viewMetadataToLabel.apply(metadata);

                Resource initialData = viewManager.fetchData(node, viewFactory);
                Component component = viewFactory.createComponent(initialData);

                tabs.newTab(id, label, new ManagedComponentSimple(component));
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
//            PdfBrowserViewer pdfViewer = new PdfBrowserViewer(streamResource);
//            pdfViewer.setHeight("100%");
//            pdfViewer.setWidth("100%");
//
//            tabs.newTab("pdf", "PDF", new ManagedComponentSimple(pdfViewer));


//            MapOptions options = new DefaultMapOptions();
//            options.setCenter(new LatLng(47.070121823, 19.204101562500004));
//            options.setZoom(7);
//            LeafletMap leafletMap = new LeafletMap(options);
//            leafletMap.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
//            tabs.newTab("map", "Map", new ManagedComponentSimple(leafletMap));

            LMap map = new LMap(49.675126, 12.160733, 17);
            map.setWidthFull();
            map.setHeightFull();
            tabs.newTab("map", "Map", new ManagedComponentSimple(map));
            //add(map);

            tabs.setSelectedTabId(selectedTabId);

            if (tabs.getSelectedTabId() == null) {
                Tab fallbackTab = Iterables.getFirst(tabs.getAvailableTabs(), null);
                tabs.getTabsComponent().setSelectedTab(fallbackTab);
            }
        }

    }
}
