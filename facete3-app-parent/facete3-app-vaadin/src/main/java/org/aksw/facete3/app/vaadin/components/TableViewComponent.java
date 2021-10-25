package org.aksw.facete3.app.vaadin.components;

import org.aksw.facete3.table.mapping.domain.TableMapping;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.rdf.model.RDFNode;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;


public class TableViewComponent<T extends RDFNode>  extends Grid<T> {
    
    private static final long serialVersionUID = 6326933457620254296L;

	protected DataProvider<T, ?> dataProvider;
    // private FacetedBrowserView mainView;
    protected TableMapping tableMapping;

    public TableViewComponent(Class<T> clazz, TableMapping tableMapping, DataProvider<T, ?> dataProvider) {
        super(clazz);
        this.dataProvider = dataProvider;
        this.tableMapping = tableMapping;
        
        init();
    }

    public void refresh() {
        dataProvider.refreshAll();
    }

    private void init() {

        setDataProvider(DataProviderUtils.wrapWithErrorHandler(dataProvider));

        getClassNames().add("compact");
        removeAllColumns();

//        ResourceUtils
        // ResourceUtils.getPro
        // addColumn();
        
        //addColumn
//        addColumn(new ComponentRenderer<>(facetValueCount -> {
//            Checkbox checkbox = new Checkbox();
//            checkbox.setValue(dataProvider.isActive(facetValueCount));
//            checkbox.addValueChangeListener(event -> {
//                if (event.getValue()) {
//                    mainView.activateConstraint(facetValueCount);
//                } else {
//                    mainView.deactivateConstraint(facetValueCount);
//                }
//            });
//            return checkbox;
//        }))
//            .setHeader("Filter")
//            .setResizable(true);


//        Column<FacetValueCount> facetValueColumn = addColumn(LabelUtils::getOrDeriveLabel).setSortProperty("value")
//                .setHeader("Facet Value")
//                //.setHeader(getSearchField())
//                .setResizable(true);

//        addColumn("focusCount.count")
//            .setHeader("Count")
//            .setResizable(true)
//            .setSortProperty("facetCount");
//
//
//        HeaderRow filterRow = appendHeaderRow();
//        filterRow.getCell(facetValueColumn).setComponent(getSearchField());
//
//
//        addItemClickListener(event -> mainView.viewNode(event.getItem()));
    }

//    private TextField getSearchField() {
//        TextField searchField = new TextField();
//        searchField.setPlaceholder("Filter FacetValues...");
//        searchField.setWidthFull();
//        searchField.addValueChangeListener(event -> dataProvider.setFilter(event.getValue()));
//        return searchField;
//    }
}