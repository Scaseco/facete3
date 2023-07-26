package org.aksw.facete3.app.vaadin.components;

import java.util.List;
import java.util.Set;

import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete3.app.shared.label.FaceteLabelUtils;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.apache.jena.graph.Node;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;


public class ConstraintsComponent
    extends HorizontalLayout {
// extends Grid<HLFacetConstraint<?>> {

    private static final long serialVersionUID = -522469945728916745L;
    private Facete3Wrapper facete3;
    private FacetedBrowserView mainView;
    // private LookupService<Node, String> labelService;
    private LabelService<Node, String> labelService;

    public ConstraintsComponent(
            FacetedBrowserView mainView,
            Facete3Wrapper facete3,
            LabelService<Node, String> labelService) {

        this.facete3 = facete3;
        this.mainView = mainView;
        this.labelService = labelService;
        init();
        refresh();
    }

    public void setItems(List<HLFacetConstraint<?>> constraints) {
        removeAll();
        for (HLFacetConstraint<?> constraint : constraints) {
            Component cc = createConstraintComponent(constraint);
            add(cc);
        }
    }

    protected Component createConstraintComponent(HLFacetConstraint<?> constraint) {
        boolean isEnabled = constraint.isActive();

        HorizontalLayout result = new HorizontalLayout();
        // Checkbox toggle = new Checkbox();
        // TODO The facete3 API does not support natively toggling constraints
        Span toggle = new Span();
        toggle.addClickListener(ev -> {
            constraint.toggle();
            mainView.refreshAll();
            // mainView.activateConstraint(null);
        });

        // toggle.setValue(isEnabled);

        // Span label = new Span();
        Set<Node> nodes = FaceteLabelUtils.extractNodes(constraint);
        // LabelMgr.<Span, HLFacetConstraint<?>, Node, String>register(label, constraint, FaceteLabelUtils::extractNodes, FaceteLabelUtils::toString);
        labelService.register(toggle, nodes, (c, m) -> toggle.setText(FaceteLabelUtils.toString(constraint, m)));
        // VaadinLabelMgr.forHasText(labelService, label, nodes, labelMap -> FaceteLabelUtils.toString(constraint, labelMap));

        Button removeBtn = new Button(VaadinIcon.TRASH.create());
        removeBtn.addClickListener(ev -> {
            constraint.remove();
            mainView.refreshAll();
        });

        result.add(toggle, removeBtn);

        return result;
    }

    public void refresh() {
        // facete3.getFacetConstraints().forEach(i -> System.out.println(FaceteLabelUtils.toString(i, labelService)));
        setItems(facete3.getFacetConstraints());
    }

    private void init() {
        // removeAllColumns();
        // addColumn(constraint -> FaceteLabelUtils.toString(constraint, labelService));
        // addItemClickListener(this::deactivateConstraint);
    }

    private void deactivateConstraint(ItemClickEvent<HLFacetConstraint<?>> event) {
        // This should refresh the constraints
        mainView.deactivateConstraint(event.getItem());
    }
}
