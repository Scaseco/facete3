package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
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
    private MainView mainView;

    public FacetCountComponent(MainView mainView, FacetCountProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.mainView = mainView;
        addFacetCountGrid();
    }

    private void addFacetCountGrid() {
        Grid<FacetCount> grid = new Grid<>(FacetCount.class);
        grid.setDataProvider(dataProvider);
        grid.removeAllColumns();
        grid.addColumn(item -> FacetProvider.getLabel(item))
                .setSortProperty("")
                .setHeader(getSearchComponent())
                .setResizable(true);
        grid.addColumn("distinctValueCount.count")
                .setSortProperty("facetCount");
        grid.asSingleSelect()
                .addValueChangeListener(this::selectFacetCallback);
        grid.addItemDoubleClickListener(this::addFacetToPathCallback);
        add(grid);
    }

    private Component getSearchComponent() {
        add(new Label("Facets"));
        TextField searchField = new TextField();
        searchField.setPlaceholder("Filter Facets...");
        searchField.addValueChangeListener(this::searchCallback);
        return searchField;
    }

    private void selectFacetCallback(
            ComponentValueChangeEvent<Grid<FacetCount>, FacetCount> event) {
        FacetCount facetCount = event.getValue();
        Node predicate = facetCount.getPredicate();
        mainView.selectFacet(predicate);
        Node node = facetCount.asNode();
        mainView.viewNode(node);
    }

    private void addFacetToPathCallback(ItemDoubleClickEvent<FacetCount> event) {
        FacetCount facet = event.getItem();
        mainView.addFacetToPath(facet);
    }

    private void searchCallback(ComponentValueChangeEvent<TextField, String> event) {
        String filter = event.getValue();
        dataProvider.setFilter(filter);
    }

    public void refresh() {
        dataProvider.refreshAll();
    }
}
