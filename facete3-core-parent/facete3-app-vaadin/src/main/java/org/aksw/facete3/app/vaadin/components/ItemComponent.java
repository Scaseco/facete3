package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

public class ItemComponent extends VerticalLayout {

    private ItemProvider itemProvider;
    private static final long serialVersionUID = 1848553144669545835L;

    public ItemComponent(MainView mainView, ItemProvider dataProvider) {
        this.itemProvider = dataProvider;
        add(new Label("Items"));
        TextField searchField = new TextField();
        searchField.setPlaceholder("Filter Items...");
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            dataProvider.setFilter(filter);
        });
        Grid<RDFNode> grid = new Grid<>(RDFNode.class);
        grid.getColumns()
                .forEach(grid::removeColumn);
        grid.addColumn(RDFNode::toString)
                .setSortProperty("value")
                .setHeader(searchField);
        grid.setDataProvider(dataProvider);
        grid.asSingleSelect()
                .addValueChangeListener(event -> {
                    Node node = event.getValue().asNode();
                    mainView.selectResource(node);
                });
        add(grid);
    }

    public void refresh() {
        itemProvider.refreshAll();
    }
}
