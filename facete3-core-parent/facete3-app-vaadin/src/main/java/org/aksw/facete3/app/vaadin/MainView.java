package org.aksw.facete3.app.vaadin;

import java.util.Set;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete3.app.vaadin.components.FacetCountComponent;
import org.aksw.facete3.app.vaadin.components.FacetValueCountComponent;
import org.aksw.facete3.app.vaadin.components.ItemComponent;
import org.aksw.facete3.app.vaadin.components.ResourceComponent;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountProvider;
import org.aksw.facete3.app.vaadin.providers.ItemProvider;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

@Route("")
@PWA(name = "Vaadin Application", shortName = "Vaadin App",
        description = "This is an example Vaadin application.", enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends AppLayout {

    private FacetCountComponent facetComponent;
    private FacetValueCountComponent facetValueCountComponent;
    private ItemComponent itemComponent;
    private ResourceComponent resourceComponent;
    private QueryConf queryConf;
    private static final long serialVersionUID = 7851055480070074549L;

    public MainView() {
        queryConf = new QueryConf();
        SplitLayout mainPanel = new SplitLayout();
        mainPanel.setOrientation(Orientation.VERTICAL);
        setContent(mainPanel);
        SplitLayout facetsPanel = new SplitLayout();
        facetsPanel.setOrientation(Orientation.HORIZONTAL);
        SplitLayout resultsPanel = new SplitLayout();
        resultsPanel.setOrientation(Orientation.HORIZONTAL);
        mainPanel.addToPrimary(facetsPanel);
        mainPanel.addToSecondary(resultsPanel);
        facetComponent = new FacetCountComponent(this, new FacetCountProvider(queryConf));
        facetValueCountComponent =
                new FacetValueCountComponent(this, new FacetValueCountProvider(queryConf));
        itemComponent = new ItemComponent(this, new ItemProvider(queryConf));
        resourceComponent = new ResourceComponent();
        facetsPanel.addToPrimary(facetComponent);
        facetsPanel.addToSecondary(facetValueCountComponent);
        resultsPanel.addToPrimary(itemComponent);
        resultsPanel.addToSecondary(resourceComponent);
    }

    public void selectResource(Node node) {
        RDFNode rdfNode = fetchIfResource(node);
        resourceComponent.setNode(rdfNode);
    }

    public void selectFacet(Node node) {
        queryConf.setSelectedFacet(node);
        facetValueCountComponent.refresh(node);
    }

    public void setConstraints(Set<FacetValueCount> enable, Set<FacetValueCount> disable) {
        setConstraints(enable, true);
        setConstraints(disable, false);
        itemComponent.refresh();
        // refresh constraintspanel
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
