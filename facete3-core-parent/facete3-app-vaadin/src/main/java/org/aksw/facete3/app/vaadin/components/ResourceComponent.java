package org.aksw.facete3.app.vaadin.components;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class ResourceComponent extends VerticalLayout {

    private static final long serialVersionUID = -6150238480758268911L;
    private RDFNode node;
    private Grid<Row> grid;
    private Label subject;

    public void setNode(RDFNode node) {
        this.node = node;
        refesh();
    }

    public class Row {

        private String predicate;
        private String object;

        public String getPredicate() {
            return predicate;
        }

        public String getObject() {
            return object;
        }

        public Row(String predicate, String object) {
            this.predicate = predicate;
            this.object = object;
        }
    }

    public ResourceComponent() {
        grid = new Grid<>(Row.class);
        grid.setItems(getRows());
        add(new Label("Resources"));
        subject = new Label();
        add(subject);
        grid.getColumns()
                .forEach(grid::removeColumn);
        grid.addColumn(Row::getPredicate)
                .setHeader("Predicate");
        grid.addColumn(Row::getObject)
                .setHeader("Object");
        add(grid);
        refesh();
    }

    private List<Row> getRows() {
        List<Row> rows = new LinkedList<Row>();
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
                String labelPredicate = predicate.toString();
                for (RDFNode object : objects) {
                    rows.add(new Row(labelPredicate, object.toString()));
                    labelPredicate = "";
                }
            }
        }
        return rows;
    }

    public void refesh() {
        if (node != null) {
            subject.setText("Subject: " + node.toString());
        }
        grid.setItems(getRows());
    }
}
