package org.aksw.facete3.app.vaadin.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.facete3.app.shared.label.LabelUtils;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.PrefixMapping;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;



public class ResourceComponent extends VerticalLayout {

    private static final long serialVersionUID = -6150238480758268911L;

    protected PrefixMapping prefixMapping;

    private Span subject;
    private RDFNode node;
    private Grid<Row> grid;
    private HashMap<Object,List<Property>> objectToProperty = new HashMap<>();

    protected boolean enableSummary = false;

    /** A view manager that yield components for a summary views */
    protected ViewManager viewManager;

    protected HorizontalLayout summaryArea = null;

    public void setNode(RDFNode node) {
        this.node = node;
        refesh();
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

    public ResourceComponent(PrefixMapping prefixMapping, ViewManager viewManager) {
        this.viewManager = viewManager;

        this.prefixMapping = prefixMapping;

        subject = new Span();
        add(subject);


        if (enableSummary) {
            summaryArea = new HorizontalLayout();
            summaryArea.setWidthFull();
            add(summaryArea);
        }

        //this.transformService = transformService;
        // ..
        grid = new Grid<>(Row.class);
        grid.getClassNames().add("compact");

        grid.setItems(getRows());
        add(new Span("Paper Data"));
        //add(new ComponentRenderer<>(paper -> {
        grid.getColumns()
                .forEach(grid::removeColumn);
        grid.addColumn(new ComponentRenderer<>(row -> {
            Component r;
            Property p = row.getPredicate();
            if (p != null) {
                Anchor anchor = new Anchor();
                anchor.setText(toDisplayString(p));
                anchor.setHref(row.getPredicate().getURI());
                anchor.setTarget("_blank");
                r = anchor;
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
            if (row.getObject().isResource()) {
                Anchor anchor = new Anchor();
                anchor.setText(displayStr);
                    anchor.setText(toDisplayString(row.getObject()));
                    anchor.setHref(row.getObject().toString());
                    anchor.setTarget("_blank");
                r = anchor;
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
        if (node != null) {

            if (enableSummary && summaryArea != null) {
                summaryArea.removeAll();

                Component summaryContent = viewManager.getComponent(node.asNode());

                if (summaryContent != null) {
                    summaryArea.add(summaryContent);
                }
            }

            subject.setText("Subject: " + node.toString());
        }
        grid.setItems(getRows());
    }
}
