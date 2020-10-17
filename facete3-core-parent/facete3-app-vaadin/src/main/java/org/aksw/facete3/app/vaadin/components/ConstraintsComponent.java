package org.aksw.facete3.app.vaadin.components;

import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.facete3.app.vaadin.LabelService;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;


public class ConstraintsComponent extends Grid<HLFacetConstraint<?>> {

    private static final long serialVersionUID = -522469945728916745L;
    private Facete3Wrapper facete3;
    private FacetedBrowserView mainView;
    private LabelService labelService;

    public ConstraintsComponent(FacetedBrowserView mainView, Facete3Wrapper facete3, LabelService labelService) {

        this.facete3 = facete3;
        this.mainView = mainView;
        this.labelService = labelService;
        init();
        refresh();
    }

    public void refresh() {
        facete3.getFacetConstraints().forEach(i -> System.out.println(labelService.toString(i)));
        setItems(facete3.getFacetConstraints());
    }

    private void init() {
        removeAllColumns();
        addColumn(labelService::toString);
        addItemClickListener(this::deactivateConstraint);
    }

    private void deactivateConstraint(ItemClickEvent<HLFacetConstraint<?>> event) {
        mainView.deactivateConstraint(event.getItem());
    }
}
