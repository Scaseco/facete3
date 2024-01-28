package org.aksw.facete3.app.vaadin.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.commons.util.history.History;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.PrefixMapping;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;


/** Not used anymore; can be eventually deleted, but keeping it as reference for now */
public class ResourceComponentOld extends VerticalLayout {

    private static final long serialVersionUID = -6150238480758268911L;

    protected PrefixMapping prefixMapping;

    private Span subjectIdSpan;
    private Span subjectLabelSpan;
    private RDFNode node;
    private Grid<Row> grid;
    private HashMap<Object,List<Property>> objectToProperty = new HashMap<>();

    private Button backBtn = new Button(VaadinIcon.ARROW_LEFT.create());
    private Button forwardBtn = new Button(VaadinIcon.ARROW_RIGHT.create());

    private History history = new History();

    protected LabelService<Node, String> labelMgr;

    protected boolean enableSummary = false;

    /** A view manager that yield components for a summary views */
    protected ViewManager viewManager;

    protected HorizontalLayout summaryArea = null;

    public void setNode(RDFNode node) {
        history.addMemento(() -> {
            this.node = node;
            refesh();
        });
    }

    public class Row {

        private Property predicate;
        private RDFNode object;

        public Property getPredicate() {
            return predicate;
        }

        public RDFNode getObject() {
            return object;
        }

        public Row(Property labelPredicate, RDFNode object) {
            this.predicate = labelPredicate;
            this.object = object;
        }
    }

    protected String toDisplayString(RDFNode rdfNode) {
        Node node = rdfNode == null ? null : rdfNode.asNode();
        String result = LabelUtils.str(node, prefixMapping);
        return result;
    }

    public ResourceComponentOld(PrefixMapping prefixMapping, ViewManager viewManager, LabelService<Node, String> labelMgr) {
        this.viewManager = viewManager;
        this.labelMgr = labelMgr;

        this.prefixMapping = prefixMapping;

        HorizontalLayout controlPane = new HorizontalLayout();
        controlPane.add(backBtn);
        controlPane.add(forwardBtn);

        add(controlPane);

        backBtn.addClickListener(ev -> history.doBackward());
        forwardBtn.addClickListener(ev -> history.doFoward());

        subjectIdSpan = new Span();
        add(subjectIdSpan);

        subjectLabelSpan = new Span();
        add(subjectLabelSpan);

        if (enableSummary) {
            summaryArea = new HorizontalLayout();
            summaryArea.setWidthFull();
            add(summaryArea);
        }

        //this.transformService = transformService;
        // ..
        grid = new Grid<>(Row.class);
        grid.getClassNames().add("compact");

        boolean asExternalLink = false;

        grid.setItems(getRows());
        // add(new Span("Paper Data"));
        //add(new ComponentRenderer<>(paper -> {
        grid.getColumns()
                .forEach(grid::removeColumn);
        grid.addColumn(new ComponentRenderer<>(row -> {
            Component r;
            Property p = row.getPredicate();
            if (p != null) {
                Node node = p.asNode();
                if (asExternalLink) {
                    Anchor anchor = new Anchor();
                    VaadinLabelMgr.forHasText(labelMgr, anchor, node);
                    // anchor.setText(toDisplayString(p));
                    anchor.setHref(node.getURI());
                    anchor.setTarget("_blank");
                    r = anchor;
                } else {
                    Span span = new Span();
                    VaadinLabelMgr.forHasText(labelMgr, span, p.asNode());
                    span.addClickListener(ev -> setNode(p));
                    r = span;
                }
            } else {
                r = new Span("");
            }
            return r;
        }))
        .setResizable(true)
        .setHeader("Predicate").setWidth("200px").setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(row -> {
            Component r;
            String displayStr = toDisplayString(row.getObject());
            RDFNode o = row.getObject();
            if (o.isResource()) {

                Node node = o.asNode();
                if (asExternalLink) {
                    Anchor anchor = new Anchor();
                    anchor.setText(displayStr);
                        // anchor.setText(toDisplayString(row.getObject()));
                        VaadinLabelMgr.forHasText(labelMgr, anchor, node);

                        anchor.setHref(o.toString());
                        anchor.setTarget("_blank");
                        r = anchor;
                } else {
                    Span span = new Span();
                    VaadinLabelMgr.forHasText(labelMgr, span, node);
                    span.addClickListener(ev -> setNode(o));
                    r = span;
                }
            }
            else {
                Span span = new Span(displayStr);
                // span.getStyle().set("color", "hsla(214, 40%, 16%, 0.94)");
                r = span;
            }
            return r;
        }))
        .setResizable(true)
        .setHeader("Object").setFlexGrow(1);

        add(grid);
        refesh();
    }

    private List<Row> getRows() {
        LinkedList<Row> rows = new LinkedList<Row>();
        if (node != null && node.isResource()) {
            Resource resource = node.asResource();
            List<Property> predicates = resource.listProperties()
                    .mapWith(Statement::getPredicate)
                    .toList()
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());
            for (Property predicate : predicates) {
                List<RDFNode> objects = ResourceUtils.listPropertyValues(resource, predicate)
                        .toList();
                Property currentPredicate = predicate;
                for (RDFNode object : objects) {
                    object.toString();
                    rows.add(new Row(currentPredicate, object));
                    currentPredicate = null;
                }
            }
        }
        return rows;

    }

    // TODO: clear if there are no resources
    private String getViewText(Property property) {
        Resource resource = node.asResource();
        String text = "";
                if (resource.hasProperty(property)) {
                    StmtIterator it =  resource.listProperties(property);
                    while (it.hasNext()) {
                        String nextText = it.next().getString();
                        text = text.concat(nextText);
                        text = text.concat(" ");
                    }
                }
        return text;
    }

    public void refesh() {
        backBtn.setEnabled(history.canDoBackward());
        forwardBtn.setEnabled(history.canDoForward());

        if (node != null) {

            if (enableSummary && summaryArea != null) {
                summaryArea.removeAll();

                Component summaryContent = viewManager.getComponent(node.asNode());

                if (summaryContent != null) {
                    summaryArea.add(summaryContent);
                }
            }

            subjectIdSpan.setText("Subject: " + node.toString());
            VaadinLabelMgr.forHasText(labelMgr, subjectLabelSpan, node.asNode());
        }
        grid.setItems(getRows());
    }
}
