package org.aksw.facete3.app.vaadin.components;

import java.util.List;

import org.aksw.facete3.app.shared.label.LabelUtils;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.aksw.facete3.app.vaadin.util.DataProviderUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class ItemComponent extends VerticalLayout {

    private ItemProvider itemProvider;
    private static final long serialVersionUID = 1848553144669545835L;

    public ItemComponent(FacetedBrowserView facetedBrowserView, ItemProvider dataProvider) {
        this.itemProvider = dataProvider;

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
        Grid<RDFNode> grid = new Grid<>(RDFNode.class);
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
                // item -> FacetProvider.getLabel(item)
                item -> LabelUtils.getOrDeriveLabel(item)
                )
                .setSortProperty("value")
                .setHeader(searchField);
        grid.setDataProvider(DataProviderUtils.wrapWithErrorHandler(dataProvider));
        grid.asSingleSelect()
                .addValueChangeListener(event -> {
                    Node node = event.getValue().asNode();
                    facetedBrowserView.viewNode(node);
                });
        add(grid);
    }

    public void refresh() {
        itemProvider.refreshAll();
    }
}
