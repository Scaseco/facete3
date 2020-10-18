package org.aksw.facete3.app.vaadin.components;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete3.app.shared.label.FaceteLabelUtils;
import org.aksw.facete3.app.shared.label.LabelUtils;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.facete3.app.vaadin.providers.FacetProvider;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.apache.jena.graph.Node;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class FacetPathComponent extends HorizontalLayout {

    private static final long serialVersionUID = 2507846860196682616L;

    protected Facete3Wrapper facete3;
    protected FacetedBrowserView mainView;
    protected LookupService<Node, String> labelService;

    public FacetPathComponent(
            FacetedBrowserView mainView,
            Facete3Wrapper facete3,
            LookupService<Node, String> labelService) {
        this.facete3 = facete3;
        this.mainView = mainView;
        this.labelService = labelService;
        refresh();
    }

    public void refresh() {
        removeAll();
        addHomeButton();
        addFacetPathButtons();
        addFacetDirectionButton();
    }

    public void addHomeButton() {
        Icon homeButton = new Icon(VaadinIcon.HOME);
        homeButton.addClickListener(event -> mainView.resetPath());
        add(homeButton);
    }

    public void addFacetPathButtons() {
        List<Directed<FacetNode>> path = facete3.getPath();
        List<Node> pathNodes = facete3.getPathNodes();
        Map<Node, String> labelMap = labelService.fetchMap(pathNodes); //(pathNodes, Function.identity());
        int n = path.size();
        for (int i = 0; i < n; ++i) {
            FacetNode facetNode = path.get(i)
                    .getValue();
            boolean isForward = facetNode.reachingDirection()
                    .isForward();
            String label = labelMap.get(facetNode.reachingPredicate());
            String str = (isForward ? "" : "^") + label;
            if (i + 1 == n) {
                // Last step
                add(new Button(str));
            } else {
                add(new Button(str, event -> mainView.changeFocus(facetNode)));
            }
        }
    }

    public void addFacetDirectionButton() {
        org.aksw.facete.v3.api.Direction direction = facete3.getFacetDirNode()
                .dir();
        switch (direction) {
            case FORWARD:
                Icon rightDirButton = new Icon(VaadinIcon.ANGLE_RIGHT);
                rightDirButton.addClickListener(event -> mainView
                        .setFacetDirection(org.aksw.facete.v3.api.Direction.BACKWARD));
                add(rightDirButton);
                break;
            case BACKWARD:
                Icon leftDirButton = new Icon(VaadinIcon.ANGLE_LEFT);
                leftDirButton.addClickListener(event -> mainView
                        .setFacetDirection(org.aksw.facete.v3.api.Direction.FORWARD));
                add(leftDirButton);
                break;
        }
    }
}
