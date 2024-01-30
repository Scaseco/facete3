package org.aksw.facete3.app.vaadin.components.sparql.wizard;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.aksw.facete3.app.vaadin.ConfigEndpoint;
import org.aksw.facete3.app.vaadin.components.SparqlEndpointForm;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfAuth;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnionDefaultGraph;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

import com.mlottmann.vstepper.Step;
import com.mlottmann.vstepper.VStepper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SparqlConnectionWizard
    extends VStepper
{
    private static final long serialVersionUID = 1L;

    protected SparqlEndpointForm sparqlEndpointForm;
    protected Grid<QuerySolution> graphGrid;
    protected Grid<QuerySolution> typeGrid;

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
    public Op getConjureSpecification(boolean applyGraphs, Boolean unionDefaultGraphMode) {
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

        Op result = OpDataRefResource.from(dataRef);

        if (Boolean.TRUE.equals(unionDefaultGraphMode)) {
            result = OpUnionDefaultGraph.create(result);
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
        graphGrid = new Grid<>(QuerySolution.class);
        typeGrid = new Grid<>(QuerySolution.class);

        this.addStep(createStepSelectEndpoint(new Label("Sparql Endpoint"), sparqlEndpointForm));
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
                Boolean unionDefaultGraphMode = sparqlEndpointForm.getUnionDefaultGraphMode().getValue();
                Op dsOp = getConjureSpecification(true, unionDefaultGraphMode);
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

    private Step createStepSelectGraphs(Component header) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.add(new H3("Query named graphs instead of the default graph?"));
        layout.add(new Span("The list below shows the named graphs detected in the endpoint. If you proceed with an empty selection then SPARQL queries will be evaluated against the endpoint's default graph. Otherwise, requests will be evaluated over the union of the selected named graphs. Querying across default graph and named graphs is unsupported."));

        Span disabledInfo = new Span("This option does apply if union default graph is specified");
        layout.add(disabledInfo);

        graphGrid.setSelectionMode(SelectionMode.MULTI);
        graphGrid.setWidthFull();
        graphGrid.getDataCommunicator().enablePushUpdates(executorService);
        Query query = QueryFactory.create("SELECT ?Graph { GRAPH ?Graph { } }");

        HeaderRow headerRow = graphGrid.appendHeaderRow();
        HeaderRow filterRow = graphGrid.appendHeaderRow();

        layout.add(graphGrid);
        return new Step(header, layout) {
            @Override
            protected void onEnter() {
                Boolean unionDefaultGraphMode = sparqlEndpointForm.getUnionDefaultGraphMode().getValue();
                disabledInfo.setVisible(unionDefaultGraphMode);
                graphGrid.setEnabled(!unionDefaultGraphMode);
                if (!unionDefaultGraphMode) {
                    Op dsOp = getConjureSpecification(false, unionDefaultGraphMode);
                    QueryExecutionFactoryQuery qef = ConfigEndpoint.createDataSource(dsOp).asQef();

                    VaadinSparqlUtils.setQueryForGridSolution(graphGrid, headerRow, qef, query, DataProviderUtils::wrapWithErrorHandler);
                    VaadinSparqlUtils.configureGridFilter(graphGrid, filterRow, List.of(GRAPH_VAR)); // , var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));
                    DataProviderUtils.wrapWithErrorHandler(graphGrid);
                }
                graphGrid.recalculateColumnWidths();
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

                Boolean unionDefaultGraphMode = sparqlEndpointForm.getUnionDefaultGraphMode().getValue();
                Op dsOp = getConjureSpecification(true, unionDefaultGraphMode);
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
