package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.providers.FacetProvider;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

public class FacetCountComponent extends VerticalLayout {

    private static final long serialVersionUID = -331380480912293631L;

    private FacetCountProvider dataProvider;

    public FacetCountComponent(MainView mainView, FacetCountProvider dataProvider) {
        this.dataProvider = dataProvider;
        add(new Label("Facets"));
        TextField searchField = new TextField();
        searchField.setPlaceholder("Filter Facets...");
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            dataProvider.setFilter(filter);
        });

        // FacetPathComponent facetPath = new FacetPathComponent(mainView, queryConf);
        // add(facetPath);

        Grid<FacetCount> grid = new Grid<>(FacetCount.class);
        grid.setDataProvider(dataProvider);
        grid.getColumns()
                .forEach(grid::removeColumn);
        grid.addColumn(item -> FacetProvider.getLabel(item))
                .setSortProperty("")
                .setHeader(searchField)
                .setResizable(true);
        grid.addColumn("distinctValueCount.count")
                .setSortProperty("facetCount");
        grid.asSingleSelect()
                .addValueChangeListener(event -> {
                    FacetCount facetCount = event.getValue();
                    if (facetCount != null) {
                        Node predicate = facetCount.getPredicate();
                        mainView.selectFacet(predicate);
                        Node node = facetCount.asNode();
                        mainView.selectResource(node);
                    } else {
                        mainView.selectFacet(RDF.type.asNode());
                    }
                });
        // grid.addItemDoubleClickListener(event -> {
        // org.aksw.facete.v3.api.Direction dir = queryConf.getFacetDirNode().dir();
        // Node node = event.getItem().getPredicate();
        // FacetedQuery facetedQuery = queryConf.getFacetedQuery();
        // facetedQuery.focus().step(node, dir).one().chFocus();
        // queryConf.setFacetDirNode(facetedQuery.focus().step(dir));
        // wrapper.refreshAll();
        // facetPath.refresh();
        // });
        add(grid);
    }

    public void refresh() {
        dataProvider.refreshAll();
    }
}
