package org.aksw.facete3.app.vaadin.components;

import java.util.List;

import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderNodeQuery;
import org.aksw.jenax.vaadin.component.grid.shacl.VaadinShaclGridUtils;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;

/**
 * The view component for values matching the given facet constraints
 *
 * @author raven
 *
 */
public class ItemComponent extends VerticalLayout {
    private static final long serialVersionUID = 1848553144669545835L;

    protected DataProviderNodeQuery dataProvider;
    protected LabelService<Node, String> labelService;

    protected ViewManager viewManager;
    protected TableContext tableContext;


    protected TextField searchField = new TextField();
    protected FacetedBrowserView facetedBrowserView;
    protected Grid<RDFNode> grid = new Grid<>(RDFNode.class);


    /** Refresh the grid, especially updating the columns. Also, a cache is used to remember components in cells. */
    public void refreshGrid() {
//        DataProvider<EnrichedItem, Void> effectiveDataProvider = DataProviderWithConversion.wrapWithBulkConvert(
//                itemProvider, this::enrich, ei -> (RDFNode)ei.getItem());


        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);


        grid.removeAllColumns();
        VaadinShaclGridUtils.configureGrid(grid, dataProvider, labelService);


        // grid.getClassNames().add("compact");
//        grid.getColumns()
//                .forEach(grid::removeColumn);
       // grid.addColumn(new ComponentRenderer<>(item -> {
       // 	Anchor anchor = new Anchor();
       // 	anchor.setText(FacetProvider.getLabel(item));
            //anchor.setHref(item.asResource().getProperty(linkProperty).getString());
       // 	anchor.setHref("");
       // 	return anchor;
       // 	})).setSortProperty("value").setHeader(searchField);
//        Column<?> col = grid.addColumn(
//                new ComponentRenderer<Component, EnrichedItem>(enrichedItem -> {
//                    RDFNode item = (RDFNode)enrichedItem.getItem();
//                    Node node = item.asNode();
////                    Component r = viewManager.getComponents(Collections.singleton(node)).get(node);
//                    Component r = (Component)enrichedItem.getClassToInstanceMap().getInstance(Component.class);
//                    if (r == null) {
//                        String str = LabelUtils.getOrDeriveLabel(item);
//                        r = new Span(str);
//                    }
//
//                    HorizontalLayout card = new HorizontalLayout();
//                    card.setWidthFull();
//                    card.addClassName("card");
//                    card.setSpacing(false);
//                    card.getThemeList().add("spacing-s");
//                    card.add(r);
//
//                    return card;
//                // item -> FacetProvider.getLabel(item)
//                //item -> LabelUtils.getOrDeriveLabel(item)
//                }))
//                .setSortProperty("value")
//                .setHeader("Items");

        HeaderRow filterRow = grid.appendHeaderRow();
        // filterRow.getCell(col).setComponent(searchField);



        grid.setDataProvider(DataProviderUtils.wrapWithErrorHandler(dataProvider));
        grid.asSingleSelect()
                .addValueChangeListener(event -> {
//                    Node node = event.getValue().asNode();
                    Node node = ((RDFNode)event.getValue()).asNode();
                    facetedBrowserView.viewNode(node);
                });

    }

    public ItemComponent(
            FacetedBrowserView facetedBrowserView,
            DataProviderNodeQuery dataProvider,
            ViewManager viewManager,
            LabelService<Node, String> labelService) {

        this.facetedBrowserView = facetedBrowserView;
        this.dataProvider = dataProvider;
        this.viewManager = viewManager;
        this.labelService = labelService;

        Button btn = new Button(VaadinIcon.COG.create()); //"Available columns");

        btn.addClickListener(event -> {
            List<RDFNode> nodes = facetedBrowserView.getFacetedSearchSession().getFacetedQuery()
                    .focus()
                    .availableValues()
                    .toFacetedQuery()
                    .focus().fwd().facetCounts()
                    .exec()
                    .map(x -> (RDFNode)x)
                    .toList()
                    .blockingGet();

            MultiSelectListBox<RDFNode> lb = new MultiSelectListBox<>();
            lb.setItems(nodes);

            Button acceptBtn = new Button("Accept");


            acceptBtn.addClickListener(ev -> {
                // Refresh the table with the current selection of columns
                // Note that this may have to recursively refresh all child tables


            });

            Dialog dialog = new Dialog();
            dialog.add(lb);
            dialog.add(acceptBtn);


            dialog.add(new Text("Close me with the esc-key or an outside click"));
            dialog.open();
        });
        Style tableSettingsStyle = btn.getStyle();
        tableSettingsStyle.set("position", "absolute");
        tableSettingsStyle.set("top", "0");
        tableSettingsStyle.set("right", "0");

        // add(btn);


        // add(new Label("Items"));
        searchField.setPlaceholder("Filter Items...");
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            // dataProvider.setFilter(filter);
        });

        VerticalLayout gridDiv = new VerticalLayout();
        gridDiv.setSizeFull();
        gridDiv.getStyle().set("position", "relative");

        // Grid<EnrichedItem> grid = new Grid<>(EnrichedItem.class);

        refreshGrid();

        // add(grid);
        gridDiv.add(grid);
        gridDiv.add(btn);
        add(gridDiv);
    }


    public void showTableMapperDialog() {

    }


    public void refresh() {
        dataProvider.refreshAll();
    }
}
