package org.aksw.facete3.app.vaadin.components;

import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.graph.Node;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

public class FacetCountComponent extends Grid<FacetCount> {

    private static final long serialVersionUID = -331380480912293631L;
    private FacetCountProvider dataProvider;
    private FacetedBrowserView mainView;

    public FacetCountComponent(
            FacetedBrowserView mainView, FacetCountProvider dataProvider) {
        super(FacetCount.class);
        this.dataProvider = dataProvider;
        this.mainView = mainView;

        GridContextMenu<FacetCount> cxtMenu = this.addContextMenu();
        GridMenuItem<FacetCount> item = cxtMenu.addItem("Focus on values of this property");
        item.addMenuItemClickListener(ev -> {
            FacetCount fc = ev.getItem().orElse(null);
            Node predicate = fc.getPredicate();
            FacetDirNode facetDirNode = mainView.facete3.getFacetDirNode();
            Direction dir = facetDirNode.dir();
            FacetNode newFocusNode = facetDirNode.parent().step(predicate, dir).one();
            newFocusNode.chFocus();

            // FacetDirNode newFacetDirNode = newFocusNode.step(dir);
            FacetPath focusPath = newFocusNode.facetPath();
            FacetDirNode newFacetDirNode = newFocusNode.step(dir);
            mainView.facete3.getFocusToFacetDir().computeIfAbsent(focusPath, path -> newFacetDirNode);

            mainView.facete3.setFacetDirNode(newFacetDirNode);


            mainView.refreshAll();
            // mainView.facete3.changeFocus(newFocusNode);
//        	FacetedQuery facetedQuery = mainView.facete3.getFacetedQuery();
//        	facetedQuery.focus();
//            mainView.facete3.changeFocus(null);
            // Notification.show("YAY");
        });

        addFacetCountGrid();
    }

    private void addFacetCountGrid() {
//        Grid<FacetCount> grid = new Grid<>(FacetCount.class);
        Grid<FacetCount> grid = this;
        grid.getClassNames().add("compact");

        grid.setDataProvider(DataProviderUtils.wrapWithErrorHandler(dataProvider));
        grid.removeAllColumns();
        Column<FacetCount> facetColumn = grid
                .addComponentColumn(item -> mainView.getLabelMgr().forHasText(new Span("" + item.getPredicate()), item.getPredicate()))
                    // .addColumn(item -> LabelUtils.getOrDeriveLabel(item))
                .setSortProperty("")
                .setHeader("Facet")
//                .setHeader(getSearchComponent())
                .setResizable(true);
        grid.addColumn("distinctValueCount.count")
                .setSortProperty("facetCount");
        grid.asSingleSelect()
                .addValueChangeListener(this::selectFacetCallback);
        grid.addItemDoubleClickListener(this::addFacetToPathCallback);
        grid.setPageSize(1000);

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(facetColumn).setComponent(getSearchComponent());


        // add(grid);



    }

    private Component getSearchComponent() {
//        add(new Label("Facets"));
        TextField searchField = new TextField();
        searchField.setPlaceholder("Filter Facets...");
        searchField.addValueChangeListener(this::searchCallback);
        searchField.setWidthFull();
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
