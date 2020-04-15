package org.aksw.facete3.app.vaadin;

import java.util.Set;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.plugin.JenaPluginFacete3;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

@Route("")
@PWA(name = "Vaadin Application", shortName = "Vaadin App", description = "This is an example Vaadin application.",
        enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends AppLayout {

    private final QueryConf queryConf;

    private static final long serialVersionUID = 155L;

    public RDFNode resourceViewActiveNode = null;

    public MainView() {
        FacetedQuery facetedQuery = getFaceteQuery();
        queryConf = new QueryConf(facetedQuery.focus().fwd(), facetedQuery, RDF.type.asNode());
        HorizontalLayout mainPanel = new HorizontalLayout();
        setContent(mainPanel);
        mainPanel.add(composeFacetPanel());
        mainPanel.add(composeFacetValuePanel());
        mainPanel.add(composeItemPanel());
    }

    private FacetedQuery getFaceteQuery() {
        JenaSystem.init();
        JenaPluginFacete3.init();
        RDFConnection conn = RDFConnectionRemote
                .create()
                .destination("https://databus.dbpedia.org/repo/sparql")
                .acceptHeaderQuery(WebContent.contentTypeResultsXML)
                .build();
        Model dataModel = ModelFactory.createDefaultModel();
        XFacetedQuery xFacetedQuery = dataModel.createResource().as(XFacetedQuery.class);
        FacetedQueryImpl.initResource(xFacetedQuery);
        return FacetedQueryImpl.create(xFacetedQuery, conn);
    }

    private Component composeFacetPanel() {
        VerticalLayout facetPanel = new VerticalLayout();
        facetPanel.add(new Label("Facets"));

        TextField facetSearchField = new TextField();
        facetPanel.add(facetSearchField);
        facetSearchField.setWidthFull();
        DataProvider<FacetCount, String> provider = new FacetCountProvider(queryConf);
        ConfigurableFilterDataProvider<FacetCount, Void, String> wrapper = provider.withConfigurableFilter();

        Grid<FacetCount> grid = new Grid<>(FacetCount.class);
        facetPanel.add(grid);
        // Necessary because vaadin adds all getX methods as columns
        grid.getColumns().forEach(grid::removeColumn);
        grid.addColumn(FacetCount::getPredicate).setSortProperty("");
        // How to do nested ::?
        grid.addColumn("distinctValueCount.count").setSortProperty("facetCount");
        facetSearchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            if (filter.trim().isEmpty()) {
                // null disables filtering
                filter = null;
            }

            System.out.println("Text2: " + filter);
            wrapper.setFilter(filter);
        });
        grid.setDataProvider(wrapper);
        grid.asSingleSelect().addValueChangeListener(event -> {
            System.out.println(event + event.getValue().toString());
            queryConf.setSelectedFacet(event.getValue().getPredicate());
            // facetDirNode =
        });
        return facetPanel;
    }

    private Component composeFacetValuePanel() {
        VerticalLayout facetValuePanel = new VerticalLayout();
        facetValuePanel.add(new Label("FacetsValues"));

        TextField facetValueSearchField = new TextField();
        facetValuePanel.add(facetValueSearchField);
        facetValueSearchField.setWidthFull();
        DataProvider<FacetValueCount, String> providerFacetValue = new FacetValueCountProvider(queryConf);
        ConfigurableFilterDataProvider<FacetValueCount, Void, String> wrapperFacetValue =
                providerFacetValue.withConfigurableFilter();

        Grid<FacetValueCount> gridFacetValue = new Grid<>(FacetValueCount.class);
        facetValuePanel.add(gridFacetValue);
        // Necessary because vaadin adds all getX methods as columns
        gridFacetValue.getColumns().forEach(gridFacetValue::removeColumn);
        gridFacetValue.addColumn(FacetValueCount::getValue).setSortProperty("value").setAutoWidth(true);
        gridFacetValue.addColumn("focusCount.count");
        facetValueSearchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            if (filter.trim().isEmpty()) {
                // null disables filtering
                filter = null;
            }

            System.out.println("Text2: " + filter);
            wrapperFacetValue.setFilter(filter);
        });
        gridFacetValue.setDataProvider(wrapperFacetValue);

        gridFacetValue.setSelectionMode(SelectionMode.MULTI);
        gridFacetValue.asMultiSelect().addValueChangeListener(event -> {
            String message = String.format("Selection changed from %s to %s", event.getOldValue(), event.getValue());
            System.out.println(message);
            setConstraints(event.getOldValue(), false);
            setConstraints(event.getValue(), true);
            // FacetValueCount item = facetValueList.getItemAt(itemIndex);
            // Node node = event.getValue().getValue();
            // HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> tmp =
            // queryConf.getFacetDirNode().via(item.getPredicate()).one()
            // .constraints().eq(v);
            // tmp.setActive(checked);
            //
        });
        return facetValuePanel;
    }


    private void setConstraints(Set<FacetValueCount> facetValueCount, boolean active) {
        for (FacetValueCount facet : facetValueCount) {
            Node v = facet.getValue();
            HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> tmp =
                    queryConf.getFacetDirNode().via(facet.getPredicate()).one().constraints().eq(v);
            tmp.setActive(active);
        }
    }

    private Component composeItemPanel() {
        VerticalLayout facetValuePanel = new VerticalLayout();
        facetValuePanel.add(new Label("Items"));

        TextField facetValueSearchField = new TextField();
        facetValuePanel.add(facetValueSearchField);
        facetValueSearchField.setWidthFull();
        DataProvider<RDFNode, String> providerFacetValue = new ItemProvider(queryConf);
        ConfigurableFilterDataProvider<RDFNode, Void, String> wrapperFacetValue =
                providerFacetValue.withConfigurableFilter();

        Grid<RDFNode> gridFacetValue = new Grid<>(RDFNode.class);
        facetValuePanel.add(gridFacetValue);
        // Necessary because vaadin adds all getX methods as columns
        gridFacetValue.getColumns().forEach(gridFacetValue::removeColumn);
        gridFacetValue.addColumn(RDFNode::toString).setSortProperty("value").setAutoWidth(true);
        gridFacetValue.setDataProvider(wrapperFacetValue);
        Button button = new Button("Click Me");
        button.addClickListener(e -> {
            gridFacetValue.getDataProvider().refreshAll();
        });
        facetValuePanel.add(button);
        return facetValuePanel;
    }
}
