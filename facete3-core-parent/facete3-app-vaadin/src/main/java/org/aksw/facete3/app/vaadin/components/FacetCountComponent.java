package org.aksw.facete3.app.vaadin.components;

import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete3.app.shared.label.LabelUtils;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.util.DataProviderUtils;
import org.apache.jena.graph.Node;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class FacetCountComponent extends VerticalLayout {

    private static final long serialVersionUID = -331380480912293631L;
    private FacetCountProvider dataProvider;
    private FacetedBrowserView mainView;

    public FacetCountComponent(FacetedBrowserView mainView, FacetCountProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.mainView = mainView;

        addFacetCountGrid();
    }

    private void addFacetCountGrid() {
        Grid<FacetCount> grid = new Grid<>(FacetCount.class);
        grid.getClassNames().add("compact");

        grid.setDataProvider(DataProviderUtils.wrapWithErrorHandler(dataProvider));
        grid.removeAllColumns();
        Column<FacetCount> facetColumn = grid.addColumn(item -> LabelUtils.getOrDeriveLabel(item))
                .setSortProperty("")
                .setHeader("Facet")
//                .setHeader(getSearchComponent())
                .setResizable(true);
        grid.addColumn("distinctValueCount.count")
                .setSortProperty("facetCount");
        grid.asSingleSelect()
                .addValueChangeListener(this::selectFacetCallback);
        grid.addItemDoubleClickListener(this::addFacetToPathCallback);

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(facetColumn).setComponent(getSearchComponent());


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
        if (facetCount != null) {
            Node predicate = facetCount.getPredicate();
            mainView.selectFacet(predicate);
            Node node = facetCount.asNode();
            mainView.viewNode(node);
        }
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
