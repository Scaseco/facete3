package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.QueryConf;
import org.apache.jena.rdf.model.RDFNode;

public class ItemComponent extends VerticalLayout {

    public ItemComponent(MainView mainView, QueryConf queryConf) {

        add(new Label("Items"));

        Grid<RDFNode> grid = new Grid<>(RDFNode.class);
        grid.getColumns().forEach(grid::removeColumn);
        grid.addColumn(RDFNode::toString).setSortProperty("value").setAutoWidth(true);
        grid.setDataProvider(mainView.ItemProvider);

        add(grid);
    }
}
