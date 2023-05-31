package org.aksw.facete3.app.vaadin.components.sparql.wizard;

import org.aksw.facete3.app.vaadin.components.SparqlEndpointForm;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;

import com.mlottmann.vstepper.Step;
import com.mlottmann.vstepper.VStepper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Label;

public class SparqlConnectionWizard
    extends VStepper
{
    protected SparqlEndpointForm sparqlEndpointForm;
    protected Grid<QuerySolution> graphGrid;

    public SparqlConnectionWizard() {
        init();
    }

    public void onSelect() {

    }

    private void init() {
        this.setWidthFull();
        sparqlEndpointForm = new SparqlEndpointForm();
        graphGrid = new Grid<>(QuerySolution.class);

        this.addStep(createStepSelectEndpoint(new Label("Sparql Endpoint"), sparqlEndpointForm));
        this.addStep(createStepSelectGraphs(new Label("Graph")));
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
            @Override
            protected void onEnter() {
            }

            @Override
            protected void onAbort() {
            }

            @Override
            protected void onComplete() {
            }

            @Override
            public boolean isValid() {
                return true;
            }
        };
    }


    private Step createStepSelectGraphs(Component header) {

        graphGrid.setWidthFull();
        graphGrid.setSelectionMode(SelectionMode.MULTI);
        return new Step(header, graphGrid) {
            @Override
            protected void onEnter() {
                QueryExecutionFactoryQuery qef = q -> QueryExecutionFactory.createServiceRequest(sparqlEndpointForm.getServiceUrl().getValue().getEndpoint(), q).build();
                VaadinSparqlUtils.setQueryForGridSolution(graphGrid, qef, QueryFactory.create("SELECT ?g { GRAPH ?g { } }"));
                graphGrid.recalculateColumnWidths();
            }

            @Override
            protected void onAbort() {
            }

            @Override
            protected void onComplete() {
                onSelect();
            }

            @Override
            public boolean isValid() {
                return true;
            }
        };
    }

}
