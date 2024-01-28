package org.aksw.facete3.app.vaadin.component.facet;

import org.aksw.facete3.app.vaadin.components.FacetedBrowserView;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountDataProvider;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

public class FacetValueCountBox
    extends VerticalLayout
{
    private static final long serialVersionUID = 1L;

    protected FacetedBrowserView mainView;

    protected FacetValueCountDataProvider dataProvider;
    protected Registration dataProviderListenerRegistration;

    protected Span title;
    protected FacetValueCountGrid grid;


    public FacetValueCountBox(FacetedBrowserView mainView, FacetValueCountDataProvider dataProvider) {
        super();
        this.mainView = mainView;
        this.dataProvider = dataProvider;

        this.title = new Span();
        this.grid = new FacetValueCountGrid(mainView, dataProvider);

        add(title);
        add(grid);
    }

    public FacetValueCountDataProvider getDataProvider() {
        return dataProvider;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        grid.setDataProvider(dataProvider);
        dataProviderListenerRegistration = dataProvider.addDataProviderListener(ev -> {
            onChangeSelectedFacet();
        });
    }

    protected void onChangeSelectedFacet() {
        VaadinLabelMgr.forHasText(mainView.getLabelMgr(), title, dataProvider.getSelectedFacet());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
            dataProviderListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }

}
