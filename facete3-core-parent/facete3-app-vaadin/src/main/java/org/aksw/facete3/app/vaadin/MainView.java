package org.aksw.facete3.app.vaadin;

import java.util.LinkedList;
import java.util.List;

import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.facete3.app.vaadin.components.ConstraintsComponent;
import org.aksw.facete3.app.vaadin.components.FacetCountComponent;
import org.aksw.facete3.app.vaadin.components.FacetPathComponent;
import org.aksw.facete3.app.vaadin.components.FacetValueCountComponent;
import org.aksw.facete3.app.vaadin.components.ItemComponent;
import org.aksw.facete3.app.vaadin.components.ResourceComponent;
import org.aksw.facete3.app.vaadin.components.SearchComponent;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountProvider;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.aksw.facete3.app.vaadin.providers.SearchProvider;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.connection.RDFConnectionTransform;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

@Route("")
@PWA(name = "Vaadin Application", shortName = "Vaadin App",
        description = "This is an example Vaadin application.", enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/vaadin-grid-styles.css", themeFor = "vaadin-grid")
public class MainView extends AppLayout {

    protected static final long serialVersionUID = 7851055480070074549L;
//    protected Config config;
    protected ConstraintsComponent constraintsComponent;
    protected FacetCountComponent facetCountComponent;
    protected FacetPathComponent facetPathComponent;
    protected FacetValueCountComponent facetValueCountComponent;
    protected Facete3Wrapper facete3;
    protected ItemComponent itemComponent;
    protected ResourceComponent resourceComponent;

//    @Autowired
    protected RDFConnection baseDataConnection;

    protected SearchProvider searchProvider;

    @Autowired
    protected SearchSensitiveRDFConnectionTransform searchSensitiveRdfConnectionTransform;

    @Autowired
    public MainView(
            RDFConnection baseDataConnection,
            SearchProvider searchProvider) {
        this.baseDataConnection = baseDataConnection;
        this.searchProvider = searchProvider;

        facete3 = new Facete3Wrapper(baseDataConnection);
        LabelService labelService = new LabelService(baseDataConnection);

        FacetCountProvider facetCountProvider = new FacetCountProvider(facete3, labelService);
        FacetValueCountProvider facetValueCountProvider =
                new FacetValueCountProvider(facete3, labelService);
        ItemProvider itemProvider = new ItemProvider(facete3, labelService);


        facetCountComponent = new FacetCountComponent(this, facetCountProvider);
        facetValueCountComponent = new FacetValueCountComponent(this, facetValueCountProvider);
        facetPathComponent = new FacetPathComponent(this, facete3);
        itemComponent = new ItemComponent(this, itemProvider);
        resourceComponent = new ResourceComponent();
        constraintsComponent = new ConstraintsComponent(this, facete3, labelService);
        constraintsComponent.setMaxHeight("40px");


        HorizontalLayout navbarLayout = new HorizontalLayout();
        navbarLayout.setWidthFull();
        navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        navbarLayout.add(new Button(new Icon(VaadinIcon.COG)));

        addToNavbar(navbarLayout);
        setContent(getAppContent());
    }

    // Auto-wiring happens after object construction
    // So in order to access auto-wired properties we need to use this post-construct init method
//    @PostConstruct
//    public void init() {
//    }

    protected Component getAppContent() {
        VerticalLayout appContent = new VerticalLayout();
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
        resourceComponent.setNode(rdfNode);
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

    protected Concept createConcept(List<String> ids) {
        List<Node> baseConcepts = new LinkedList<Node>();
        for (String id : ids) {
            Node baseConcept = NodeFactory.createURI(id);
            baseConcepts.add(baseConcept);
        }
        return ConceptUtils.createConcept(baseConcepts);
    }

    protected void refreshAll() {
        facetCountComponent.refresh();
        facetValueCountComponent.refresh();
        itemComponent.refresh();
        resourceComponent.refesh();
        constraintsComponent.refresh();
    }


}
