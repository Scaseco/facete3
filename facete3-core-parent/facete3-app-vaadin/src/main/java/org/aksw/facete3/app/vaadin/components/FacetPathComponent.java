package org.aksw.facete3.app.vaadin.components;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.QueryConf;
import org.aksw.jena_sparql_api.utils.model.Directed;

public class FacetPathComponent extends HorizontalLayout {

    private QueryConf queryConf;
    private HorizontalLayout facetPath;
    private MainView mainView;

    public FacetPathComponent(MainView mainView, QueryConf queryConf) {

        this.queryConf = queryConf;
        this.mainView = mainView;

        FacetDirNode facetDirNode = queryConf.getFacetDirNode();
        Icon root = new Icon(VaadinIcon.HOME);
        root.addClickListener(event -> changeFocus(facetDirNode.parent().root()));
        add(root);

        facetPath = new HorizontalLayout();
        add(facetPath);

        refresh();
    }

    public void refresh() {

        facetPath.removeAll();
        FacetDirNode facetDirNode = queryConf.getFacetDirNode();
        // For each path element, create another button
        List<Directed<FacetNode>> path = facetDirNode.parent().path();
        int n = path.size();
        for (int i = 0; i < n; ++i) {
            Directed<FacetNode> step = path.get(i);
            FacetNode facetNode = step.getValue();
            boolean isFwd = facetNode.reachingDirection().isForward();
            String label = facetNode.reachingPredicate().toString();
            String str = (isFwd ? "" : "^") + label;
            if (i + 1 == n) {
                // Last step
                facetPath.add(new Button(str));
            } else {
                facetPath.add(new Button(str, event -> changeFocus(facetNode)));
            }
        }
    }

    private void changeFocus(FacetNode tmp) {

        FacetDirNode facetDirNode = queryConf.getFacetDirNode();
        org.aksw.facete.v3.api.Direction dir = tmp.reachingDirection();
        if (dir == null) {
            dir = facetDirNode.dir();
        }

        tmp.chFocus();

        // For robustness ; dir should never be null
        if (dir != null) {
            queryConf.setFacetDirNode(tmp.step(dir));
        }
        refresh();
        // mainView.facetProvider.refreshAll();
        // updateFacets(fq);
        // updateFacetPathPanel();
    }

}
