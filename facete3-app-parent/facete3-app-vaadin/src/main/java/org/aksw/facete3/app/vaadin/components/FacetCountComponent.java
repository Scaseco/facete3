package org.aksw.facete3.app.vaadin.components;

import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.aksw.vaadin.common.component.util.ConfirmDialogUtils;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.graph.Node;
import org.claspina.confirmdialog.ConfirmDialog;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.grid.contextmenu.GridSubMenu;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;

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

        addFacetOptions(cxtMenu);

        addFacetCountGrid();
    }

    public void addFacetOptions(GridContextMenu<FacetCount> cxtMenu) {
        {
            GridMenuItem<FacetCount> focusItem = cxtMenu.addItem("Focus on values of this property");
            focusItem.addMenuItemClickListener(ev -> {
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
            });
        }

        {
            GridMenuItem<FacetCount> showSubFacetsMenuItem = cxtMenu.addItem("Show nested facets");
            showSubFacetsMenuItem.addMenuItemClickListener(ev -> {
                FacetCount item = ev.getItem().orElse(null);
                if (item != null) {
                    addFacetToPathCallback(item);
                }
            });
        }

        {
            GridMenuItem<FacetCount> addToCustomFacets = cxtMenu.addItem("Add to custom facets ... ");
            GridSubMenu<FacetCount> subMenu = addToCustomFacets.getSubMenu();
            subMenu.addItem("... visible only at current focus path", ev -> {
                FacetDirNode facetDirNode = mainView.facete3.getFacetDirNode();
                Node predicate = ev.getItem().get().getPredicate();
                mainView.addCustomFacet(facetDirNode, predicate);
            });

            subMenu.addItem("... always visible");

//            addToCustomFacets.addMenuItemClickListener(ev -> {
//                FacetCount item = ev.getItem().orElse(null);
//                if (item != null) {
//                    // addFacetToPathCallback(item);
//                }
//            });
        }

        {
            cxtMenu.addItem("Show Query", ev -> {
                String queryStr = dataProvider.translateQuery(new Query<>()).baseRelation().toQuery().toString();
                ConfirmDialog dlg = ConfirmDialogUtils.info("Query" , queryStr, "Ok");
                dlg.setWidth("50%");
                dlg.setHeight("50%");
                dlg.open();
            });
        }
    }

    private void addFacetCountGrid() {
//        Grid<FacetCount> grid = new Grid<>(FacetCount.class);
        Grid<FacetCount> grid = this;
        grid.getClassNames().add("compact");

        grid.setDataProvider(DataProviderUtils.wrapWithErrorHandler(dataProvider));
        grid.removeAllColumns();
        Column<FacetCount> facetColumn = grid
                .addComponentColumn(item -> VaadinLabelMgr.forHasText(mainView.getLabelMgr(), new Span("" + item.getPredicate()), item.getPredicate()))
                    // .addColumn(item -> LabelUtils.getOrDeriveLabel(item))
                .setSortProperty("")
                .setHeader("Facet")
//                .setHeader(getSearchComponent())
                .setResizable(true);
        grid.addColumn("distinctValueCount.count")
                .setSortProperty("facetCount");
        grid.asSingleSelect()
                .addValueChangeListener(this::selectFacetCallback);
        grid.addItemDoubleClickListener(ev -> addFacetToPathCallback(ev.getItem()));
        grid.setPageSize(1000);

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(facetColumn).setComponent(getSearchComponent());
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

    private void addFacetToPathCallback(FacetCount facet) {
        // FacetCount facet = event.getItem();
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
