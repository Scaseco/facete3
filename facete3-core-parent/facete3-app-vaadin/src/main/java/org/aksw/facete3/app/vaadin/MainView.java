package org.aksw.facete3.app.vaadin;

import java.util.Set;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.plugin.JenaPluginFacete3;
import org.aksw.facete3.app.vaadin.components.FacetComponent;
import org.aksw.facete3.app.vaadin.components.FacetValueComponent;
import org.aksw.facete3.app.vaadin.components.ItemComponent;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

@Route("")
@PWA(name = "Vaadin Application", shortName = "Vaadin App", description = "This is an example Vaadin application.",
        enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends AppLayout {

    private final QueryConf queryConf;
    public final DataProvider<FacetCount, String> facetProvider;
    public final DataProvider<FacetValueCount, String> facetValueProvider;
    public final DataProvider<RDFNode, String> ItemProvider;

    private static final long serialVersionUID = 155L;

    public RDFNode resourceViewActiveNode = null;

    public MainView() {
        FacetedQuery facetedQuery = getFaceteQuery();
        queryConf = new QueryConf(facetedQuery.focus().fwd(), facetedQuery, RDF.type.asNode());
        facetProvider = new FacetCountProvider(queryConf);
        facetValueProvider = new FacetValueCountProvider(queryConf);
        ItemProvider = new ItemProvider(queryConf);

        HorizontalLayout mainPanel = new HorizontalLayout();
        setContent(mainPanel);

        mainPanel.add(new FacetComponent(this, queryConf));
        mainPanel.add(new FacetValueComponent(this, queryConf));
        mainPanel.add(new ItemComponent(this, queryConf));
    }

    private FacetedQuery getFaceteQuery() {
        JenaSystem.init();
        JenaPluginFacete3.init();
        RDFConnection conn = RDFConnectionRemote
                .create()
                .destination("https://databus.dbpedia.org/repo/sparql")
                .acceptHeaderQuery(WebContent.contentTypeResultsXML)
                .build();
        Model dataModel = ModelFactory.createDefaultModel();
        XFacetedQuery xFacetedQuery = dataModel.createResource().as(XFacetedQuery.class);
        FacetedQueryImpl.initResource(xFacetedQuery);
        return FacetedQueryImpl.create(xFacetedQuery, conn);
    }
}
