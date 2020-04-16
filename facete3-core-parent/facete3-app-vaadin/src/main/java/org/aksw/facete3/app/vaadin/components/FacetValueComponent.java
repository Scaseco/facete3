package org.aksw.facete3.app.vaadin.components;

import java.util.Set;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.QueryConf;
import org.apache.jena.graph.Node;

public class FacetValueComponent extends VerticalLayout {

    public FacetValueComponent(MainView mainView, QueryConf queryConf) {

        ConfigurableFilterDataProvider<FacetValueCount, Void, String> wrapper =
                mainView.facetValueProvider.withConfigurableFilter();

        add(new Label("FacetValues"));

        TextField searchField = new TextField();
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            if (filter.trim().isEmpty()) {
                filter = null;
            }
            System.out.println("Filter fvc: " + filter);
            wrapper.setFilter(filter);
        });
        add(searchField);

        Grid<FacetValueCount> grid = new Grid<>(FacetValueCount.class);
        grid.setDataProvider(wrapper);
        grid.getColumns().forEach(grid::removeColumn);
        grid.addColumn(FacetValueCount::getValue).setSortProperty("value").setAutoWidth(true);
        grid.addColumn("focusCount.count").setSortProperty("focusCount");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.asMultiSelect().addValueChangeListener(event -> {
            String message = String.format("Selection changed from %s to %s", event.getOldValue(), event.getValue());
            System.out.println(message);
            setConstraints(queryConf, event.getOldValue(), false);
            setConstraints(queryConf, event.getValue(), true);
            mainView.ItemProvider.refreshAll();
        });
        add(grid);
    }

    private void setConstraints(QueryConf queryConf, Set<FacetValueCount> facetValueCount, boolean active) {
        for (FacetValueCount facet : facetValueCount) {
            Node v = facet.getValue();
            HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> tmp =
                    queryConf.getFacetDirNode().via(facet.getPredicate()).one().constraints().eq(v);
            tmp.setActive(active);
        }
    }
}
