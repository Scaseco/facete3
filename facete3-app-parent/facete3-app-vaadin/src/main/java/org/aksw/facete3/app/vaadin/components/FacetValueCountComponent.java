package org.aksw.facete3.app.vaadin.components;

import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountProvider;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class FacetValueCountComponent extends Grid<FacetValueCount> {

    private FacetValueCountProvider dataProvider;
    private FacetedBrowserView mainView;
    private static final long serialVersionUID = 6326933457620254296L;

    public FacetValueCountComponent(FacetedBrowserView mainView, FacetValueCountProvider dataProvider) {
        super(FacetValueCount.class);
        this.dataProvider = dataProvider;
        this.mainView = mainView;
        init();
    }

    public void refresh() {
        dataProvider.refreshAll();
    }

    private void init() {
        setPageSize(10000);

        setDataProvider(DataProviderUtils.wrapWithErrorHandler(dataProvider));

        getClassNames().add("compact");
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
        }))
            .setHeader("Filter")
            .setResizable(true);


        Column<FacetValueCount> facetValueColumn =
                // addColumn(LabelUtils::getOrDeriveLabel).setSortProperty("value")
                addComponentColumn(item -> mainView.getLabelMgr().forHasText(new Span("" + item.getValue()), item.getValue()))
                .setSortProperty("value")
                .setHeader("Facet Value")
                //.setHeader(getSearchField())
                .setResizable(true);

        addColumn("focusCount.count")
            .setHeader("Count")
            .setResizable(true)
            .setSortProperty("facetCount");


        HeaderRow filterRow = appendHeaderRow();
        filterRow.getCell(facetValueColumn).setComponent(getSearchField());


        addItemClickListener(event -> mainView.viewNode(event.getItem()));
    }

    private TextField getSearchField() {
        TextField searchField = new TextField();
        searchField.setPlaceholder("Filter FacetValues...");
        searchField.setWidthFull();
        searchField.addValueChangeListener(event -> dataProvider.setFilter(event.getValue()));
        return searchField;
    }
}
