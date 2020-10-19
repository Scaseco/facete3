package org.aksw.facete3.app.vaadin.components;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.facete3.app.shared.label.LabelUtils;
import org.aksw.facete3.app.vaadin.Config;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.facete3.app.vaadin.SearchSensitiveRDFConnectionTransform;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountProvider;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.aksw.facete3.app.vaadin.providers.SearchProvider;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.core.connection.RDFConnectionTransform;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.ext.com.google.common.graph.Traverser;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;

public class FacetedBrowserView
    extends VerticalLayout {

    protected FacetedBrowserToolbar toolbar;

    protected ConstraintsComponent constraintsComponent;
    protected FacetCountComponent facetCountComponent;
    protected FacetPathComponent facetPathComponent;
    protected FacetValueCountComponent facetValueCountComponent;
    protected Facete3Wrapper facete3;
    protected ItemComponent itemComponent;
    protected ResourceComponent resourceComponent;


//  @Autowired
    protected RDFConnection baseDataConnection;

    protected SearchProvider searchProvider;

    @Autowired
    protected SearchSensitiveRDFConnectionTransform searchSensitiveRdfConnectionTransform;

    @Autowired
    protected ConfigurableApplicationContext cxt;



    public FacetedBrowserView(
            RDFConnection baseDataConnection,
            SearchProvider searchProvider,
            PrefixMapping prefixMapping,
            Facete3Wrapper facete3,
            FacetCountProvider facetCountProvider,
            FacetValueCountProvider facetValueCountProvider,
            ItemProvider itemProvider,
            Config config) {
        this.baseDataConnection = baseDataConnection;
        this.searchProvider = searchProvider;
        this.facete3 = facete3;

        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
                baseDataConnection,
                RDFS.label,
                prefixMapping);

//        TransformService transformService = new TransformService(config.getPrefixFile());


        toolbar = new FacetedBrowserToolbar();
        facetCountComponent = new FacetCountComponent(this, facetCountProvider);
        facetValueCountComponent = new FacetValueCountComponent(this, facetValueCountProvider);
        facetPathComponent = new FacetPathComponent(this, facete3, labelService);
        itemComponent = new ItemComponent(this, itemProvider);
        resourceComponent = new ResourceComponent(prefixMapping);
        constraintsComponent = new ConstraintsComponent(this, facete3, labelService);
        constraintsComponent.setMaxHeight("40px");


//        HorizontalLayout navbarLayout = new HorizontalLayout();
//        navbarLayout.setWidthFull();
//        navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
//
//        Button appSettingsBtn = new Button(new Icon(VaadinIcon.COG));
//        navbarLayout.add(appSettingsBtn);

        Dialog dialog = new Dialog();
        dialog.setWidth("50em");

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();

        SparqlEndpointForm input = new SparqlEndpointForm();
        input.setWidthFull();

        layout.add(input);
        Button applyBtn = new Button("Apply");
        layout.add(applyBtn);
        applyBtn.addClickListener(event -> {
            String urlStr = input.getServiceUrl().getValue().getEndpoint();
            DataRefSparqlEndpoint dataRef = cxt.getBean(DataRefSparqlEndpoint.class);
            dataRef.setServiceUrl(urlStr);
//            System.out.println("INVOKING REFRESH");
//            System.out.println("Given cxt:\n" + toString(cxt));
//            System.out.println("Updated dataRef " + System.identityHashCode(dataRef));
            refreshAllNew();

            // TODO Now all dataProviders need to refresh
            dialog.close();
        });


        dialog.add(layout);


        Button refreshBtn = new Button(new Icon(VaadinIcon.REFRESH));
        refreshBtn.getElement().setProperty("title", "Refresh all data providers assigned to this view");
        refreshBtn.addClickListener(event -> {
            refreshAllNew();
        });
        toolbar.add(refreshBtn);


        Button configBtn = new Button(new Icon(VaadinIcon.COG));
        toolbar.add(configBtn);

        configBtn.addClickListener(event -> {
            dialog.open();
//            input.focus();
        });


        add(getAppContent());
    }


    public static String toString(ApplicationContext cxt) {
        Iterable<ApplicationContext> ancestors = Traverser.<ApplicationContext>forTree(c ->
            c.getParent() == null
                ? Collections.emptyList()
                : Collections.singletonList(c.getParent()))
        .depthFirstPreOrder(cxt);

        String result = Streams
            .stream(ancestors)
            .map(Object::toString)
            .collect(Collectors.joining("\n"));

        return result;
    }

    public void refreshAllNew() {
        RefreshScope refreshScope = cxt.getBean(RefreshScope.class);
        refreshScope.refreshAll();
    }

    // Auto-wiring happens after object construction
    // So in order to access auto-wired properties we need to use this post-construct init method
//    @PostConstruct
//    public void init() {
//    }

    protected Component getAppContent() {
        VerticalLayout appContent = new VerticalLayout();
        appContent.add(toolbar);

        appContent.add(getNaturalLanguageInterfaceComponent());

        appContent.add(constraintsComponent);

        appContent.add(getFacete3Component());
        return appContent;
    }

    protected Component getNaturalLanguageInterfaceComponent() {
        SearchComponent result = new SearchComponent(this, searchProvider);
        return result;
    }


    protected Component getFacete3Component() {
        SplitLayout component = new SplitLayout();
        component.setSizeFull();
        component.setOrientation(Orientation.HORIZONTAL);
        component.setSplitterPosition(33);
        component.addToPrimary(getFacetComponent());
        component.addToSecondary(getResultsComponent());
        return component;
    }

    protected Component getFacetComponent() {
        SplitLayout facetComponent = new SplitLayout();
        facetComponent.setSizeFull();
        facetComponent.setOrientation(Orientation.VERTICAL);
        facetComponent.addToPrimary(facetCountComponent);
        facetComponent.addToSecondary(facetValueCountComponent);
        VerticalLayout component = new VerticalLayout();
        component.add(facetPathComponent);
//        component.add(constraintsComponent);
        component.add(facetComponent);
        return component;
    }

    protected Component getResultsComponent() {
        SplitLayout component = new SplitLayout();
        component.setOrientation(Orientation.VERTICAL);
        component.addToPrimary(itemComponent);
        component.addToSecondary(resourceComponent);
        return component;
    }

    public void viewNode(Node node) {
        RDFNode rdfNode = facete3.fetchIfResource(node);
        if ( rdfNode != null )
        {  resourceComponent.setNode(rdfNode); }

    }

    public void viewNode(FacetValueCount facetValueCount) {
        viewNode(facetValueCount.getValue());
    }

    public void selectFacet(Node node) {
        facete3.setSelectedFacet(node);
        facetValueCountComponent.refresh();
    }

    public void activateConstraint(FacetValueCount facetValueCount) {
        facete3.activateConstraint(facetValueCount);
        refreshAll();
    }

    public void deactivateConstraint(FacetValueCount facetValueCount) {
        facete3.deactivateConstraint(facetValueCount);
        refreshAll();
    }

    // TODO Why the long class declaration?
    public void setFacetDirection(org.aksw.facete.v3.api.Direction direction) {
        facete3.setFacetDirection(direction);
        facetCountComponent.refresh();
        facetPathComponent.refresh();
    }

    public void resetPath() {
        facete3.resetPath();
        facetCountComponent.refresh();
        facetPathComponent.refresh();
    }

    public void addFacetToPath(FacetCount facet) {
        facete3.addFacetToPath(facet);
        facetCountComponent.refresh();
        facetPathComponent.refresh();
    }

    public void changeFocus(FacetNode facet) {
        facete3.changeFocus(facet);
        facetCountComponent.refresh();
        facetPathComponent.refresh();
    }

    public void handleSearchResponse(RDFNodeSpec rdfNodeSpec) {
        UnaryRelation baseConcept = ConceptUtils.createConceptFromRdfNodes(rdfNodeSpec.getCollection());
        facete3.setBaseConcept(baseConcept);

        if (searchSensitiveRdfConnectionTransform != null) {
            RDFConnectionTransform connXform = searchSensitiveRdfConnectionTransform.create(rdfNodeSpec);
            RDFConnection effectiveDataConnection = connXform.apply(baseDataConnection);
            facete3.getFacetedQuery().connection(effectiveDataConnection);
        }

        refreshAll();
    }

    public void deactivateConstraint(HLFacetConstraint<?> constraint) {
        constraint.deactivate();
        refreshAll();
    }
//
//    protected List<String> getPaperIds(NliResponse response) {
//        List<Paper> papers = response.getResults();
//        List<String> ids = new LinkedList<String>();
//        for (Paper paper : papers) {
//            ids.addAll(paper.getId());
//        }
//        return ids;
//    }

    private Concept createConcept(List<String> ids) {
        List<Node> baseConcepts = new LinkedList<Node>();
        for (String id : ids) {
            Node baseConcept = NodeFactory.createURI(id);
            baseConcepts.add(baseConcept);
        }
        return ConceptUtils.createConcept(baseConcepts);
    }

    protected void refreshAll() {
        refreshAllNew();
//        facetCountComponent.refresh();
//        facetValueCountComponent.refresh();
//        itemComponent.refresh();
//        resourceComponent.refesh();
//        constraintsComponent.refresh();
    }

}
