package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountProvider;
import org.apache.jena.graph.Node;

public class FacetValueCountComponent extends VerticalLayout {

    private FacetValueCountProvider dataProvider;
    private Label selectedFacet;
    private static final long serialVersionUID = 6326933457620254296L;

    public FacetValueCountComponent(MainView mainView, FacetValueCountProvider dataProvider) {
        this.dataProvider = dataProvider;
        add(new Label("FacetValues"));
        selectedFacet = new Label();
        add("Selected Facet:" + selectedFacet);
        TextField searchField = new TextField();
        searchField.setPlaceholder("Filter FacetValues...");
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            dataProvider.setFilter(filter);
        });
        Grid<FacetValueCount> grid = new Grid<>(FacetValueCount.class);
        grid.setDataProvider(dataProvider);
        grid.getColumns()
                .forEach(grid::removeColumn);
        grid.addColumn(FacetValueCount::getValue)
                .setSortProperty("value")
                .setHeader(searchField)
                .setResizable(true);
        grid.addColumn("focusCount.count")
                .setSortProperty("facetCount");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.asMultiSelect()
                .addValueChangeListener(event -> {
                    String message = String.format("Selection changed from %s to %s",
                            event.getOldValue(), event.getValue());
                    System.out.println(message);
                    mainView.setConstraints(event.getValue(), event.getOldValue());
                });
        grid.addItemClickListener(event -> {
            Node facetCount = event.getItem().getValue();
            mainView.selectResource(facetCount);
        });
        add(grid);
    }

    public void refresh(Node predicate) {
        selectedFacet.setText(predicate.toString());
        dataProvider.refreshAll();
    }
}
