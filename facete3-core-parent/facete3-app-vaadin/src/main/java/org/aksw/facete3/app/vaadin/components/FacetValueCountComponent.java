package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.providers.FacetProvider;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountProvider;
import org.apache.jena.graph.Node;

public class FacetValueCountComponent extends Grid<FacetValueCount> {

    private FacetValueCountProvider dataProvider;
    private MainView mainView;
    private static final long serialVersionUID = 6326933457620254296L;

    public FacetValueCountComponent(MainView mainView, FacetValueCountProvider dataProvider) {
        super(FacetValueCount.class);
        this.dataProvider = dataProvider;
        this.mainView = mainView;
        init();
    }

    public void refresh() {
        dataProvider.refreshAll();
    }

    private void init() {

        setDataProvider(dataProvider);

        removeAllColumns();
        addColumn(new ComponentRenderer<>(facetValueCount -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(dataProvider.isActive(facetValueCount));
            checkbox.addValueChangeListener(event -> {
                if (event.getValue()) {
                    mainView.activateConstraint(facetValueCount);
                } else {
                    mainView.deactivateConstraint(facetValueCount);
                }
            });
            return checkbox;
        }));
        addColumn(FacetProvider::getLabel).setSortProperty("value")
                .setHeader(getSearchField())
                .setResizable(true);
        addColumn("focusCount.count").setSortProperty("facetCount");
        addItemClickListener(event -> mainView.viewNode(event.getItem()));
    }

    private TextField getSearchField() {
        TextField searchField = new TextField();
        searchField.setPlaceholder("Filter FacetValues...");
        searchField.addValueChangeListener(event -> dataProvider.setFilter(event.getValue()));
        return searchField;
    }
}
