package org.aksw.facete3.app.vaadin;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.HLFacetConstraint;
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
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@PWA(name = "Vaadin Application", shortName = "Vaadin App",
        description = "This is an example Vaadin application.", enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends AppLayout {

    private FacetCountComponent facetCountComponent;
    private FacetValueCountComponent facetValueCountComponent;
    private FacetPathComponent facetPathComponent;
    private ItemComponent itemComponent;
    private ResourceComponent resourceComponent;
    private QueryConf queryConf;
    private Config config;
    private static final long serialVersionUID = 7851055480070074549L;

    @Autowired
    public MainView(Config config) {
        this.config = config;
        queryConf = new QueryConf(config);
        facetCountComponent = new FacetCountComponent(this, new FacetCountProvider(queryConf));
        facetValueCountComponent =
                new FacetValueCountComponent(this, new FacetValueCountProvider(queryConf));
        facetPathComponent = new FacetPathComponent(this, queryConf);
        itemComponent = new ItemComponent(this, new ItemProvider(queryConf));
        resourceComponent = new ResourceComponent();
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

    public void selectResource(Node node) {
        RDFNode rdfNode = fetchIfResource(node);
        resourceComponent.setNode(rdfNode);
    }

    public void selectFacet(Node node) {
        queryConf.setSelectedFacet(node);
        facetValueCountComponent.refresh();
    }

    public void setConstraints(Set<FacetValueCount> enable, Set<FacetValueCount> disable) {
        setConstraints(enable, true);
        setConstraints(disable, false);
        itemComponent.refresh();
        // refresh constraintscomponent
    }

    public void handleNliResponse(NliResponse response) {
        List<String> ids = getPaperIds(response);
        Concept baseConcepts = createConcept(ids);
        queryConf.setBaseConcept(baseConcepts);
        refreshAll();
    };

    private List<String> getPaperIds(NliResponse response) {
        List<Paper> papers = response.getResults();
        List<String> ids = new LinkedList<String>();
        for (Paper paper : papers) {
            ids.addAll(paper.getId());
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
    }

    private void setConstraints(Set<FacetValueCount> facetValueCount, boolean isEnabled) {
        for (FacetValueCount facet : facetValueCount) {
            Node v = facet.getValue();
            HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> tmp =
                    queryConf.getFacetDirNode()
                            .via(facet.getPredicate())
                            .one()
                            .constraints()
                            .eq(v);
            tmp.setActive(isEnabled);
        }
    }

    private RDFNode fetchIfResource(Node node) {
        Query query = QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }");
        UnaryRelation filter = ConceptUtils.createFilterConcept(node);
        query.setQueryPattern(RelationImpl.create(query.getQueryPattern(), Vars.s)
                .joinOn(Vars.s)
                .with(filter)
                .getElement());
        Model model = queryConf.getFacetedQuery()
                .connection()
                .queryConstruct(query);
        RDFNode result = model.asRDFNode(node);
        return result;
    }
}
