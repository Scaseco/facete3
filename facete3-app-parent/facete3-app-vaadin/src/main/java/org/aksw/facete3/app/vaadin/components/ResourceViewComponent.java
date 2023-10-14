package org.aksw.facete3.app.vaadin.components;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.aksw.commons.util.delegate.Unwrappable;
import org.aksw.commons.util.history.History;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBase;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactories;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.aksw.vaadin.common.provider.util.DataProviderReduce;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;



public class ResourceViewComponent extends VerticalLayout {

    private static final long serialVersionUID = -6150238480758268911L;

    public static final Var PREDICATE_VAR = Var.alloc("Predicate");
    public static final Var OBJECT_VAR = Var.alloc("Object");

    protected PrefixMapping prefixMapping;

    private Span subjectIdSpan;
    private Span subjectLabelSpan;
    private Node subjectNode;
    private Grid<Entry<Binding, Binding>> grid;
    // private HashMap<Object,List<Property>> objectToProperty = new HashMap<>();

    private Button backBtn = new Button(VaadinIcon.ARROW_LEFT.create());
    private Button forwardBtn = new Button(VaadinIcon.ARROW_RIGHT.create());

    private History history = new History();

    protected LabelService<Node, String> labelMgr;

    protected boolean enableSummary = false;

    /** A view manager that yield components for a summary views */
    protected ViewManager viewManager;

    protected HorizontalLayout summaryArea = null;


    private void setNodeCore(Node node, QueryExecutionFactoryQuery qef) {
        subjectNode = node;
        Fragment relation = FragmentUtils.fromQuery("SELECT ?Predicate ?Object { ?s ?Predicate ?Object }");

        if (node == null) {
            node = NodeValue.FALSE.asNode();
        }

        if (qef == null) {
            qef = QueryExecutionFactories.empty();
        }

        relation = relation.filter(Vars.s, node);
        DataProviderSparqlBase<?> dataProvider = Unwrappable.unwrap(grid.getDataProvider(), DataProviderSparqlBase.class, true).orElse(null);
        dataProvider.setRelation(relation);
        dataProvider.setQueryExecutionFactory(qef);
        dataProvider.refreshAll();
    }

//    public void setQueryExecutionFactory(QueryExecutionFactoryQuery qef) {
//        DataProviderSparqlBase<?> dataProvider = Unwrappable.unwrap(grid.getDataProvider(), DataProviderSparqlBase.class, true).orElse(null);
//        dataProvider.setQueryExecutionFactory(qef);
//        dataProvider.refreshAll();
//    }

    private void setNode(Node node) {
        DataProviderSparqlBase<?> dataProvider = Unwrappable.unwrap(grid.getDataProvider(), DataProviderSparqlBase.class, true).orElse(null);
        QueryExecutionFactoryQuery qef = dataProvider.getQueryExecutionFactory();
        setNode(node, qef);
    }

    public void setNode(Node node, QueryExecutionFactoryQuery qef) {
        history.addMemento(() -> {
            setNodeCore(node, qef);
            refesh();
        });
    }

//    public void setNode(RDFNode node, QueryExecutionFactoryQuery qef) {
//        history.addMemento(() -> {
//            this.subjectNode = node.asNode();
//            refesh();
//        });
//    }

    protected String toDisplayString(Node node) {
        // Node node = rdfNode == null ? null : node;
        String result = LabelUtils.str(node, prefixMapping);
        return result;
    }

    public ResourceViewComponent(PrefixMapping prefixMapping, ViewManager viewManager, LabelService<Node, String> labelMgr) {
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

        QueryExecutionFactoryQuery qef = QueryExecutionFactories.empty();
        DataProviderSparqlBinding coreProvider = new DataProviderSparqlBinding(null, qef);
        DataProvider<Binding, Expr> tmpProvider = VaadinSparqlUtils.wrapDataProviderWithFilter(coreProvider);


        grid = new Grid<>();
        // grid.setSortableColumns(PREDICATE_VAR.getName(), OBJECT_VAR.getName());
        grid.setMultiSort(true);
        grid.setDataProvider(DataProviderReduce.of(tmpProvider));
        grid.getClassNames().add("compact");

        setNodeCore(null, null);

        // HeaderRow headerRow = grid.appendHeaderRow();
        boolean asExternalLink = false;

        // grid.setItems(getRows());
        // add(new Span("Paper Data"));
        //add(new ComponentRenderer<>(paper -> {
        grid.getColumns()
                .forEach(grid::removeColumn);

        Column<Entry<Binding, Binding>> predicateColumn = grid.addColumn(new ComponentRenderer<>(row -> {
            Component r;
            Node priorP = Optional.ofNullable(row.getKey()).map(b -> b.get(PREDICATE_VAR)).orElse(null);
            Node p = row.getValue().get(PREDICATE_VAR); // row.getPredicate();
            if (p != null && !p.equals(priorP)) {
                // Node node = p.asNode();
                if (asExternalLink) {
                    Anchor anchor = new Anchor();
                    VaadinLabelMgr.forHasText(labelMgr, anchor, p);
                    // anchor.setText(toDisplayString(p));
                    anchor.setHref(p.getURI());
                    anchor.setTarget("_blank");
                    r = anchor;
                } else {
                    Span span = new Span();
                    VaadinLabelMgr.forHasText(labelMgr, span, p);
                    span.addClickListener(ev -> setNode(p));
                    r = span;
                }
            } else {
                r = new Span("");
            }
            return r;
        }))
        .setKey(PREDICATE_VAR.getName())
        .setHeader(PREDICATE_VAR.getName())
        .setSortable(true)
        .setResizable(true)
        .setWidth("200px").setFlexGrow(0);

        Column<Entry<Binding, Binding>> objectColumn = grid.addColumn(new ComponentRenderer<>(row -> {
            Component r;
            Node o = row.getValue().get(OBJECT_VAR); // row.getPredicate();
            String displayStr = toDisplayString(o);
            // RDFNode o = row.getObject();
            if (o != null && o.isURI()) {

                // Node node = o.asNode();
                if (asExternalLink) {
                    Anchor anchor = new Anchor();
                    anchor.setText(displayStr);
                        // anchor.setText(toDisplayString(row.getObject()));
                        VaadinLabelMgr.forHasText(labelMgr, anchor, o);

                        anchor.setHref(o.toString());
                        anchor.setTarget("_blank");
                        r = anchor;
                } else {
                    Span span = new Span();
                    VaadinLabelMgr.forHasText(labelMgr, span, o);
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
        .setKey(OBJECT_VAR.getName())
        .setHeader(OBJECT_VAR.getName())
        .setResizable(true)
        .setSortable(true)
        .setFlexGrow(1);

        grid.sort(Arrays.asList(
            // new GridSortOrder<>(objectColumn, SortDirection.ASCENDING),
            new GridSortOrder<>(predicateColumn, SortDirection.ASCENDING)
        ));

        HeaderRow filterRow = grid.appendHeaderRow();

        // VaadinSparqlUtils.setQueryForGridBinding(null, headerRow, coreProvider);
        VaadinSparqlUtils.configureGridFilter(grid, filterRow, List.of(PREDICATE_VAR, OBJECT_VAR));

        add(grid);
        refesh();
    }


//
//    private List<Row> getRows() {
//        LinkedList<Row> rows = new LinkedList<Row>();
//        if (node != null && node.isResource()) {
//            Resource resource = node.asResource();
//            List<Property> predicates = resource.listProperties()
//                    .mapWith(Statement::getPredicate)
//                    .toList()
//                    .stream()
//                    .distinct()
//                    .collect(Collectors.toList());
//            for (Property predicate : predicates) {
//                List<RDFNode> objects = ResourceUtils.listPropertyValues(resource, predicate)
//                        .toList();
//                Property currentPredicate = predicate;
//                for (RDFNode object : objects) {
//                    object.toString();
//                    rows.add(new Row(currentPredicate, object));
//                    currentPredicate = null;
//                }
//            }
//        }
//        return rows;
//
//    }

    // TODO: clear if there are no resources
    private String getViewText(Property property) {
        // Resource resource = subjectNode.asResource();
        String text = "";
//                if (resource.hasProperty(property)) {
//                    StmtIterator it =  resource.listProperties(property);
//                    while (it.hasNext()) {
//                        String nextText = it.next().getString();
//                        text = text.concat(nextText);
//                        text = text.concat(" ");
//                    }
//                }
        return text;
    }

    public void refesh() {
        backBtn.setEnabled(history.canDoBackward());
        forwardBtn.setEnabled(history.canDoForward());

        if (subjectNode != null) {

            if (enableSummary && summaryArea != null) {
                summaryArea.removeAll();

                Component summaryContent = viewManager.getComponent(subjectNode);

                if (summaryContent != null) {
                    summaryArea.add(summaryContent);
                }
            }

            subjectIdSpan.setText("Subject: " + subjectNode.toString());
            VaadinLabelMgr.forHasText(labelMgr, subjectLabelSpan, subjectNode);

        }
        // grid.setItems(getRows());
    }
}
