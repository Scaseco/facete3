package org.aksw.facete3.app.vaadin;

import java.util.LinkedList;
import java.util.List;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete3.app.vaadin.components.ConstraintsComponent;
import org.aksw.facete3.app.vaadin.components.FacetCountComponent;
import org.aksw.facete3.app.vaadin.components.FacetPathComponent;
import org.aksw.facete3.app.vaadin.components.FacetValueCountComponent;
import org.aksw.facete3.app.vaadin.components.ItemComponent;
import org.aksw.facete3.app.vaadin.components.ResourceComponent;
import org.aksw.facete3.app.vaadin.components.SearchComponent;
import org.aksw.facete3.app.vaadin.domain.NliResponse;
import org.aksw.facete3.app.vaadin.domain.Paper;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountProvider;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@PWA(name = "Vaadin Application", shortName = "Vaadin App",
        description = "This is an example Vaadin application.", enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends AppLayout {

    private static final long serialVersionUID = 7851055480070074549L;
    private Config config;
    private ConstraintsComponent constraintsComponent;
    private FacetCountComponent facetCountComponent;
    private FacetPathComponent facetPathComponent;
    private FacetValueCountComponent facetValueCountComponent;
    private Facete3Wrapper facete3;
    private ItemComponent itemComponent;
    private ResourceComponent resourceComponent;

    @Autowired
    public MainView(Config config) {
        this.config = config;
        RDFConnectionBuilder rdfConnectionBuilder = new RDFConnectionBuilder(config);
        RDFConnection rdfConnection = rdfConnectionBuilder.getRDFConnection();
        facete3 = new Facete3Wrapper(rdfConnection);
        LabelService labelService = new LabelService(rdfConnection);
        LabelService titleService = new LabelService(rdfConnection,config.getAlternativeLabel());
        TransformService transformService = new TransformService(config.getPrefixFile());
        FacetCountProvider facetCountProvider = new FacetCountProvider(facete3, labelService);
        FacetValueCountProvider facetValueCountProvider =
                new FacetValueCountProvider(facete3, labelService);
        ItemProvider itemProvider = new ItemProvider(facete3,titleService);
        facetCountComponent = new FacetCountComponent(this, facetCountProvider);
        facetValueCountComponent = new FacetValueCountComponent(this, facetValueCountProvider);
        facetPathComponent = new FacetPathComponent(this, facete3);
        itemComponent = new ItemComponent(this, itemProvider);
        resourceComponent = new ResourceComponent(transformService);
        constraintsComponent = new ConstraintsComponent(this, facete3, labelService);
        setContent(getAppContent());
    }

    private Component getAppContent() {
        VerticalLayout appContent = new VerticalLayout();
        appContent.add(getNaturalLanguageInterfaceComponent());
        appContent.add(getFacete3Component());
        return appContent;
    }

    private Component getNaturalLanguageInterfaceComponent() {
        return new SearchComponent(this, config);
    }
   

    private Component getFacete3Component() {
        SplitLayout component = new SplitLayout();
        component.setSizeFull();
        component.setOrientation(Orientation.HORIZONTAL);
        component.setSplitterPosition(33);
        component.addToPrimary(getFacetComponent());
        component.addToSecondary(getResultsComponent());
        return component;
    }

    private Component getFacetComponent() {
        SplitLayout facetComponent = new SplitLayout();
        facetComponent.setSizeFull();
        facetComponent.setOrientation(Orientation.VERTICAL);
        facetComponent.addToPrimary(facetCountComponent);
        facetComponent.addToSecondary(facetValueCountComponent);
        VerticalLayout component = new VerticalLayout();
        component.add(facetPathComponent);
        component.add(constraintsComponent);
        component.add(facetComponent);
        return component;
    }

    private Component getResultsComponent() {
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

    public void handleNliResponse(NliResponse response) {
        List<String> ids = getPaperIds(response);
        Concept baseConcepts = createConcept(ids);
        facete3.setBaseConcept(baseConcepts);
        refreshAll();
    }

    public void deactivateConstraint(HLFacetConstraint<?> constraint) {
        constraint.deactivate();
        refreshAll();
    }
    
    public Config getConfig() {
    	return this.config;
    }

    // Gets currently only one id for each paper to avoid duplicates
    private List<String> getPaperIds(NliResponse response) {
        List<Paper> papers = response.getResults();
        List<String> ids = new LinkedList<String>();
        for (Paper paper : papers) {
            ids.add(paper.getIds().get(0));
        }
        return ids;
    }

    private Concept createConcept(List<String> ids) {
        List<Node> baseConcepts = new LinkedList<Node>();
        for (String id : ids) {
            Node baseConcept = NodeFactory.createURI(id);
            baseConcepts.add(baseConcept);
        }
        return ConceptUtils.createConcept(baseConcepts);
    }

    private void refreshAll() {
        facetCountComponent.refresh();
        facetValueCountComponent.refresh();
        itemComponent.refresh();
        resourceComponent.refesh();
        constraintsComponent.refresh();
    }


}