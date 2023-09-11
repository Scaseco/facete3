package org.aksw.facete3.app.vaadin.components.sparql.wizard;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.facete3.app.vaadin.components.SparqlEndpointForm;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;

import com.mlottmann.vstepper.Step;
import com.mlottmann.vstepper.VStepper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SparqlConnectionWizard
    extends VStepper
{
    protected SparqlEndpointForm sparqlEndpointForm;
    protected Grid<QuerySolution> graphGrid;
    protected Grid<QuerySolution> typeGrid;

    public SparqlConnectionWizard() {
        init();
    }

    /** Implement this method to handle completion of the wizard */
    public void onWizardCompleted() {

    }

    public String getEndpointUrl() {
        String result = sparqlEndpointForm.getServiceUrl().getValue().getEndpoint();
        return result;
    }

    public Set<String> getSelectedDefaultGraphIris() {
        Set<String> result = graphGrid.getSelectedItems().stream().map(qs -> qs.get("g"))
                .map(RDFNode::asNode)
                .filter(Node::isURI)
                .map(Node::getURI)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return result;
    }

    public Set<Node> getSelectedTypes() {
        Set<Node> result = typeGrid.getSelectedItems().stream().map(qs -> qs.get("t"))
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


    private Step createStepSelectGraphs(Component header) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.add(new H3("Query named graphs instead of the default graph?"));
        layout.add(new Span("The list below shows the named graphs detected in the endpoint. If you proceed with an empty selection then SPARQL queries will be evaluated against the endpoint's default graph. Otherwise, requests will be evaluated over the union of the selected named graphs. Querying across default graph and named graphs is unsupported."));

        graphGrid.setWidthFull();
        graphGrid.setSelectionMode(SelectionMode.MULTI);
        layout.add(graphGrid);
        return new Step(header, layout) {
            @Override
            protected void onEnter() {
                QueryExecutionFactoryQuery qef = q -> QueryExecutionFactory.createServiceRequest(sparqlEndpointForm.getServiceUrl().getValue().getEndpoint(), q).build();
                VaadinSparqlUtils.setQueryForGridSolution(graphGrid, qef, QueryFactory.create("SELECT ?g { GRAPH ?g { } }"), DataProviderUtils::wrapWithErrorHandler);
                DataProviderUtils.wrapWithErrorHandler(graphGrid);
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
        typeGrid.setSelectionMode(SelectionMode.MULTI);
        layout.add(typeGrid);
        return new Step(header, layout) {
            @Override
            protected void onEnter() {
                Set<String> graphNames = getSelectedDefaultGraphIris();
                //Query query = QueryFactory.create("SELECT DISTINCT ?p { ?s ?p ?o }");
                Query query = QueryFactory.create("SELECT DISTINCT ?t { ?s a ?t }");
                graphNames.forEach(query::addGraphURI);

                QueryExecutionFactoryQuery qef = q -> QueryExecutionFactory.createServiceRequest(sparqlEndpointForm.getServiceUrl().getValue().getEndpoint(), q).build();
                VaadinSparqlUtils.setQueryForGridSolution(typeGrid, qef, query, DataProviderUtils::wrapWithErrorHandler);
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
