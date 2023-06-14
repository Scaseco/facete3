package org.aksw.facete3.app.vaadin.components;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.facete3.app.vaadin.providers.EnrichedItem;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.aksw.vaadin.common.provider.util.DataProviderWithConversion;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Style;

/**
 * The view component for values matching the given facet constraints
 *
 * @author raven
 *
 */
public class ItemComponent extends VerticalLayout {

    private ItemProvider itemProvider;
    private static final long serialVersionUID = 1848553144669545835L;

    protected ViewManager viewManager;


    protected TableContext tableContext;


    protected TextField searchField = new TextField();
    protected FacetedBrowserView facetedBrowserView;
    protected Grid<EnrichedItem> grid = new Grid<>(EnrichedItem.class);


    public List<EnrichedItem> enrich(List<RDFNode> rdfNodes) {
        List<Node> nodes = rdfNodes.stream().map(RDFNode::asNode).collect(Collectors.toList());

        //Map<Node, ViewFactory> nodeToViewFactory = viewManager.getBestViewFactories(nodes);
        Map<Node, Component> nodeToComponent = viewManager.getComponents(nodes);

        List<EnrichedItem> result = rdfNodes.stream().map(rdfNode -> {
            Node node = rdfNode.asNode();
            Component component = nodeToComponent.get(node);
//        	ViewFactory viewFactory = nodeToViewFactory.get(node);

            EnrichedItem<RDFNode> r = new EnrichedItem<>(rdfNode);
            r.getClassToInstanceMap().putInstance(Component.class, component);
            return r;

        }).collect(Collectors.toList());


        return result;
    }

    /** Refresh the grid, especially updating the columns. Also, a cache is used to remember components in cells. */
    public void refreshGrid() {
        DataProvider<EnrichedItem, Void> effectiveDataProvider = DataProviderWithConversion.wrapWithBulkConvert(
                itemProvider, this::enrich, ei -> (RDFNode)ei.getItem());



        grid.getClassNames().add("compact");
        grid.getColumns()
                .forEach(grid::removeColumn);
       // grid.addColumn(new ComponentRenderer<>(item -> {
       // 	Anchor anchor = new Anchor();
       // 	anchor.setText(FacetProvider.getLabel(item));
            //anchor.setHref(item.asResource().getProperty(linkProperty).getString());
       // 	anchor.setHref("");
       // 	return anchor;
       // 	})).setSortProperty("value").setHeader(searchField);
        grid.addColumn(
                new ComponentRenderer<Component, EnrichedItem>(enrichedItem -> {
                    RDFNode item = (RDFNode)enrichedItem.getItem();
                    Node node = item.asNode();
//                    Component r = viewManager.getComponents(Collections.singleton(node)).get(node);
                    Component r = (Component)enrichedItem.getClassToInstanceMap().getInstance(Component.class);
                    if (r == null) {
                        String str = LabelUtils.getOrDeriveLabel(item);
                        r = new Span(str);
                    }
                    return r;
                // item -> FacetProvider.getLabel(item)
                //item -> LabelUtils.getOrDeriveLabel(item)
                }))
                .setSortProperty("value")
                .setHeader(searchField);
        grid.setDataProvider(DataProviderUtils.wrapWithErrorHandler(effectiveDataProvider));
        grid.asSingleSelect()
                .addValueChangeListener(event -> {
//                    Node node = event.getValue().asNode();
                    Node node = ((RDFNode)event.getValue().getItem()).asNode();
                    facetedBrowserView.viewNode(node);
                });

    }

    public ItemComponent(
            FacetedBrowserView facetedBrowserView,
            ItemProvider dataProvider,
            ViewManager viewManager) {

        this.facetedBrowserView = facetedBrowserView;

        this.itemProvider = dataProvider;


        this.viewManager = viewManager;



        Button btn = new Button(VaadinIcon.COG.create()); //"Available columns");
        btn.addClickListener(event -> {
            List<RDFNode> nodes = itemProvider.getFacete3().getFacetedQuery()
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


        add(new Label("Items"));
        searchField.setPlaceholder("Filter Items...");
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            dataProvider.setFilter(filter);
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

    public void refresh() {
        itemProvider.refreshAll();
    }
}
