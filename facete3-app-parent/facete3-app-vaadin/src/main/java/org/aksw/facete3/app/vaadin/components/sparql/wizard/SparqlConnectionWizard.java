package org.aksw.facete3.app.vaadin.components.sparql.wizard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.facete3.app.vaadin.ConfigEndpoint;
import org.aksw.facete3.app.vaadin.components.SparqlEndpointForm;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfAuth;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpJavaRewrite;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnionDefaultGraph;
import org.aksw.jena_sparql_api.vaadin.util.Grid2;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourcePolyfill;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.Suggestion;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.exec.http.QuerySendMode;

import com.mlottmann.vstepper.Step;
import com.mlottmann.vstepper.VStepper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

public class SparqlConnectionWizard
    extends VStepper
{
    private static final long serialVersionUID = 1L;

    protected SparqlEndpointForm sparqlEndpointForm;
    protected Grid2<QuerySolution> graphGrid;
    protected Grid2<QuerySolution> typeGrid;

    protected Select<GraphMode> graphModeSelect = new Select<>();

    // protected Set<Node> selectedGraphs = Collections.emptySet();

    protected ExecutorService executorService;


    Var GRAPH_VAR = Var.alloc("Graph");
    Var TYPE_VAR = Var.alloc("Type");

    public SparqlConnectionWizard(ExecutorService executorService) {
        this.executorService = executorService;
        init();
    }

    /** Implement this method to handle completion of the wizard */
    public void onWizardCompleted() {

    }

    /**
     *
     * @param applyGraphs Whether to restrict queries to the specified graphs.
     * @param unionDefaultGraph Whether to apply union default graph transform.
     * @return
     */
    public Op getConjureSpecification(boolean applyGraphs, Boolean unionDefaultGraphMode, boolean applyPolyfills) {
        String urlStr = getEndpointUrl();

        RdfDataRefSparqlEndpoint dataRef = ModelFactory.createDefaultModel().createResource().as(RdfDataRefSparqlEndpoint.class);
        dataRef.setServiceUrl(urlStr);

        RdfAuth auth = sparqlEndpointForm.getAuth();
        if (auth != null) {
            dataRef.getModel().add(auth.getModel());
            dataRef.setAuth(auth);
        }

        Set<String> defaultGraphIris = getSelectedDefaultGraphIris();

        if (applyGraphs) {
            dataRef.getDefaultGraphs().addAll(defaultGraphIris);
        }

        if (true) {
            // Virtuoso incorrectly returns an empty json on empty result sets which breaks with jena
            dataRef.setAcceptHeaderSelectQuery(WebContent.contentTypeResultsXML);

            dataRef.setSendModeQuery(QuerySendMode.asGetWithLimitForm.name());
        }

        Op result = OpDataRefResource.from(dataRef);

        if (Boolean.TRUE.equals(unionDefaultGraphMode)) {
            result = OpUnionDefaultGraph.create(result);
        }

        if (applyPolyfills) {
            result = createTransformApplyPolyfills(gridItems, result);
        }

        return result;
    }

    public String getEndpointUrl() {
        String result = sparqlEndpointForm.getServiceUrl();
        return result;
    }

    public Set<String> getSelectedDefaultGraphIris() {
        Set<String> result = graphGrid.getSelectedItems().stream().map(qs -> qs.get(GRAPH_VAR.getName()))
                .map(RDFNode::asNode)
                .filter(Node::isURI)
                .map(Node::getURI)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return result;
    }

    public Set<Node> getSelectedTypes() {
        Set<Node> result = typeGrid.getSelectedItems().stream().map(qs -> qs.get(TYPE_VAR.getName()))
                .map(RDFNode::asNode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return result;
    }

    private void init() {
        this.setWidthFull();
        sparqlEndpointForm = new SparqlEndpointForm();
        graphGrid = new Grid2<>(QuerySolution.class);
        typeGrid = new Grid2<>(QuerySolution.class);

        this.addStep(createStepSelectEndpoint(new Label("Sparql Endpoint"), sparqlEndpointForm));
        this.addStep(createStepPolyfills(new Label("Polyfills")));
        this.addStep(createStepSelectGraphs(new Label("Graphs")));
        this.addStep(createStepSelectDatasetId(new Label("DatasetId")));
        this.addStep(createStepSelectTypes(new Label("Types")));
        // this.addStep(createStep(new Label("Step 3"), new Label("Step 3")));
        // return customSteps;
    }


//    class SparqlEndpointSelector
//        extends VerticalLayout
//    {
//
//    }

//    class GraphSelector
//        extends VerticalLayout
//    {
//
//    }

    public static Op createTransformApplyPolyfills(List<Selectable<Suggestion<String>>> gridItems, Op baseOp) {
        OpJavaRewrite result = OpJavaRewrite.create(baseOp);
        for (Selectable<Suggestion<String>> item : gridItems) {
            if (item.isSelected()) {
                result.addRewrite(item.getValue().getValue());
            }
        }
        return result;
    }

    protected Selectable<Suggestion<String>> draggedItem = null;
    protected List<Selectable<Suggestion<String>>> gridItems = new ArrayList<>();

    private Step createStepPolyfills(Component header) {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new Span("SPARQL polyfill is a query rewriting middleware that abstracts away vendor-specific differences."));
        Grid<Selectable<Suggestion<String>>> grid = new Grid<>();
        grid.setItems(gridItems);
        grid.addComponentColumn(row -> {
            Checkbox cb = new Checkbox(row.isSelected());
            cb.addValueChangeListener(ev -> {
                row.setSelected(Boolean.TRUE.equals(cb.getValue()));
            });
            return cb;
        }).setKey("isEnabled");

        grid.addComponentColumn(row -> {
            return new Span(row.getValue().getName());
        }).setKey("name");

        grid.setRowsDraggable(true);

        grid.addDragStartListener(event -> {
            draggedItem = event.getDraggedItems().get(0);
            grid.setDropMode(GridDropMode.BETWEEN);
        });

        grid.addDragEndListener(event -> {
            draggedItem = null;
            grid.setDropMode(null);
        });

        grid.addDropListener(event -> {
            Selectable<Suggestion<String>> dropOverItem = event.getDropTargetItem().get();
            if (!dropOverItem.equals(draggedItem)) {
                // reorder dragged item the backing gridItems container
                gridItems.remove(draggedItem);
                // calculate drop index based on the dropOverItem
                int dropIndex = gridItems.indexOf(dropOverItem) + (event.getDropLocation() == GridDropLocation.BELOW ? 1 : 0);
                gridItems.add(dropIndex, draggedItem);
                grid.getDataProvider().refreshAll();
            }
        });

        layout.add(grid);

        return new Step(header, layout) {
            @Override protected void onEnter() {
                Op dsOp = getConjureSpecification(true, false, false);
                RdfDataSource dataSource = ConfigEndpoint.createDataSource(dsOp);
                List<Selectable<Suggestion<String>>> suggestions = RdfDataSourcePolyfill.suggestPolyfills(dataSource).stream()
                        .map(Selectable::of)
                        .collect(Collectors.toList());

                gridItems.clear();
                gridItems.addAll(suggestions);
                // grid.getData .setItems(suggestions);
            }
            @Override protected void onAbort() { }
            @Override protected void onComplete() { }
            @Override public boolean isValid() { return true; }
        };
    }

    private Step createStepSelectEndpoint(Component header, Component content) {
        return new Step(header, content) {
            @Override protected void onEnter() { }
            @Override protected void onAbort() { }
            @Override protected void onComplete() { }
            @Override public boolean isValid() { return true; }
        };
    }

    private Step createStepSelectDatasetId(Component header) {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new Span("The following dataset ids have been recorded for the given configuration."));

        layout.add("Current hash: ");
        Span datasetId = new Span();
        layout.add(datasetId);
        return new Step(header, layout) {
            @Override
            protected void onEnter() {
                Boolean unionDefaultGraphMode = isUnionDefaultGraphMode();
                Op dsOp = getConjureSpecification(true, unionDefaultGraphMode, true);
                RdfDataSource dataSource = ConfigEndpoint.createDataSource(dsOp);
                String datasetHashId = RdfDataSources.fetchDatasetHash(dataSource);
                datasetId.setText(datasetHashId);

                // RdfDataSources.fetchDatasetHash(dataSource)
            }

            @Override protected void onAbort() {}
            @Override protected void onComplete() { }
            @Override public boolean isValid() { return true; }
        };
    }

    public boolean isUnionDefaultGraphMode() {
        boolean result = GraphMode.UNION_DEFAULT_GRAPH.equals(graphModeSelect.getValue());
        return result;
    }

    public static enum GraphMode {
        CUSTOM("Custom"),
        UNION_DEFAULT_GRAPH("Default Graph Mode");

        protected String label;

        private GraphMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private Step createStepSelectGraphs(Component header) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.add(new H3("Query named graphs instead of the default graph?"));
        layout.add(new Span("The list below shows the named graphs detected in the endpoint. If you proceed with an empty selection then SPARQL queries will be evaluated against the endpoint's default graph. Otherwise, requests will be evaluated over the union of the selected named graphs. Querying across default graph and named graphs is unsupported."));

        graphModeSelect.setWidthFull();
        graphModeSelect.setLabel("Graph Mode");
        graphModeSelect.setItems(GraphMode.CUSTOM, GraphMode.UNION_DEFAULT_GRAPH);
        graphModeSelect.setTextRenderer(GraphMode::getLabel);
        layout.add(graphModeSelect);

        Span disabledInfo = new Span("This option does apply if union default graph is specified");
        layout.add(disabledInfo);

        graphGrid.setSelectionMode(SelectionMode.MULTI);
        graphGrid.setWidthFull();
        graphGrid.getDataCommunicator().enablePushUpdates(executorService);
        Query query = QueryFactory.create("SELECT ?Graph { GRAPH ?Graph { } }");

        HeaderRow headerRow = graphGrid.appendHeaderRow();
        HeaderRow filterRow = graphGrid.appendHeaderRow();

        boolean[] isInitialized = { false };

        Runnable refreshGridAction = () -> {
            GraphMode mode = graphModeSelect.getValue();
            if (isInitialized[0] && GraphMode.CUSTOM.equals(mode)) {
                Op dsOp = getConjureSpecification(false, false, true);
                QueryExecutionFactoryQuery qef = ConfigEndpoint.createDataSource(dsOp).asQef();
                VaadinSparqlUtils.setQueryForGridSolution(graphGrid, headerRow, qef, query, DataProviderUtils::wrapWithErrorHandler);
                VaadinSparqlUtils.configureGridFilter(graphGrid, filterRow, List.of(GRAPH_VAR)); // , var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));
                DataProviderUtils.wrapWithErrorHandler(graphGrid);
            } else {
                graphGrid.setItems();
            }

            switch (mode) {
            case CUSTOM:
                graphGrid.setEnabled(true);
                disabledInfo.setVisible(false);
                break;
            case UNION_DEFAULT_GRAPH:
                graphGrid.setEnabled(false);
                disabledInfo.setVisible(true);
                break;
            default:
                throw new RuntimeException("Should not happen");
            }

            graphGrid.recalculateColumnWidths();
        };

        graphModeSelect.addValueChangeListener(ev -> refreshGridAction.run());
        graphModeSelect.setValue(GraphMode.CUSTOM);

        layout.add(graphGrid);

        return new Step(header, layout) {
            @Override
            protected void onEnter() {
                isInitialized[0] = true;
                refreshGridAction.run();
                // boolean unionDefaultGraphMode = isUnionDefaultGraphMode();
//                Boolean unionDefaultGraphMode = sparqlEndpointForm.getUnionDefaultGraphMode().getValue();
//                disabledInfo.setVisible(unionDefaultGraphMode);
//                graphGrid.setEnabled(!unionDefaultGraphMode);
            }

            @Override protected void onAbort() {}
            @Override protected void onComplete() { }
            @Override public boolean isValid() { return true; }
        };
    }

    private Step createStepSelectTypes(Component header) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.add(new H3("Initial data"));
        layout.add(new Span("The list below shows the classes (aka types) available in the configured default graph of the specified SPARQL endpoint. The initial set of items can be restricted by selecting items for the list below."));

        typeGrid.setWidthFull();
        typeGrid.getDataCommunicator().enablePushUpdates(executorService);
        typeGrid.setSelectionMode(SelectionMode.MULTI);
        Query baseQuery = QueryFactory.create("SELECT DISTINCT ?Type { ?s a ?Type }");

        HeaderRow headerRow = typeGrid.appendHeaderRow();
        HeaderRow filterRow = typeGrid.appendHeaderRow();

        layout.add(typeGrid);

        return new Step(header, layout) {
            @Override
            protected void onEnter() {
                Set<String> graphNames = getSelectedDefaultGraphIris();
                // System.err.println("Selected graph names: " + graphNames);

                Query query = baseQuery.cloneQuery();
                graphNames.forEach(query::addGraphURI);

//                QueryExecutionFactoryQuery qef = q -> {
//                    Query clone = q.cloneQuery();
//                    Set<String> presentGraphs = new LinkedHashSet<>(clone.getGraphURIs());
//                    Set<String> missingGraphs = new LinkedHashSet<>(Sets.difference(graphNames, presentGraphs));
//                    missingGraphs.forEach(clone::addGraphURI);
//                    QueryExecution r = QueryExecutionFactory.createServiceRequest(sparqlEndpointForm.getServiceUrl().getValue().getEndpoint(), clone).build();
//                    return r;
//                };

                Boolean unionDefaultGraphMode = isUnionDefaultGraphMode();
                Op dsOp = getConjureSpecification(true, unionDefaultGraphMode, true);
                QueryExecutionFactoryQuery qef = ConfigEndpoint.createDataSource(dsOp).asQef();

                VaadinSparqlUtils.setQueryForGridSolution(typeGrid, headerRow, qef, query, DataProviderUtils::wrapWithErrorHandler);
                VaadinSparqlUtils.configureGridFilter(typeGrid, filterRow, List.of(TYPE_VAR), var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));
                DataProviderUtils.wrapWithErrorHandler(typeGrid);
                typeGrid.recalculateColumnWidths();
            }

            @Override
            protected void onAbort() {}

            @Override
            protected void onComplete() { onWizardCompleted(); }

            @Override
            public boolean isValid() { return true; }
        };
    }

}
