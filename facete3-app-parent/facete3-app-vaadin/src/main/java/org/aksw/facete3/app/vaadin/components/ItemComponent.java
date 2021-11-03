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
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;

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

    public ItemComponent(
            FacetedBrowserView facetedBrowserView,
            ItemProvider dataProvider,
            ViewManager viewManager) {
        this.itemProvider = dataProvider;


        this.viewManager = viewManager;


        DataProvider<EnrichedItem, Void> effectiveDataProvider = DataProviderWithConversion.wrapWithBulkConvert(
                itemProvider, this::enrich, ei -> (RDFNode)ei.getItem());



        Button btn = new Button("Available columns");
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

            ListBox<RDFNode> lb = new ListBox<>();
            lb.setItems(nodes);

            Dialog dialog = new Dialog();
            dialog.add(lb);
            dialog.add(new Text("Close me with the esc-key or an outside click"));
            dialog.open();
        });
        add(btn);


        add(new Label("Items"));
        TextField searchField = new TextField();
        searchField.setPlaceholder("Filter Items...");
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            dataProvider.setFilter(filter);
        });
        Grid<EnrichedItem> grid = new Grid<>(EnrichedItem.class);
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
        add(grid);
    }

    public void refresh() {
        itemProvider.refreshAll();
    }
}
