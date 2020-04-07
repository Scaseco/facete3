package org.aksw.facete3.app.vaadin;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.plugin.JenaPluginFacete3;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and use @Route annotation to announce
 * it in a URL as a Spring managed bean. Use the @PWA annotation make the application installable on
 * phones, tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every browser tab/window.
 */
@SuppressWarnings("serial")
@Route
@PWA(name = "Vaadin Application", shortName = "Vaadin App", description = "This is an example Vaadin application.",
        enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends AppLayout {

    public static final char CHAR_UPWARDS_ARROW = '\u2191';
    public static final char CHAR_DOWNWARDS_ARROW = '\u2193';

    public static final String[] sortModeLabel = {"A-Z", "0-9"};

    // Mapping of ui sort directions to ui labels
    public static final String[] sortDirLabel =
            {Character.toString(CHAR_DOWNWARDS_ARROW), Character.toString(CHAR_UPWARDS_ARROW)};

    // Mapping of ui sort directions to jena query sort directions
    public static final int[] sortDirMapJena = {Query.ORDER_DESCENDING, Query.ORDER_ASCENDING};

    protected PrefixMapping globalPrefixes = new PrefixMappingImpl();

    protected boolean showLabels = true;

    public RDFNode resourceViewActiveNode = null;

    protected FacetedQuery fq;

    public void init() {
        JenaSystem.init();
        JenaPluginFacete3.init();

        RDFConnection conn = RDFConnectionRemote
                .create()
                .destination("https://databus.dbpedia.org/repo/sparql")
                // .destination("http://cord19.aksw.org/sparql")
                .acceptHeaderQuery(WebContent.contentTypeResultsXML)
                .build();

        Model dataModel = ModelFactory.createDefaultModel();
        XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
        FacetedQueryImpl.initResource(facetedQuery);

        fq = FacetedQueryImpl.create(facetedQuery, conn);

        // fq.root()
        // .fwd(RDF.type).one()
        // .constraints()
        // .eq(DCTerms.BibliographicResource).activate();
    }

    public List<String> randomItems() {
        System.out.println("Complex Query");
        List<FacetValueCount> fc =
                // -- Faceted Browsing API
                fq
                        .focus()
                        .fwd()
                        .facetValueCounts()
                        .exclude(RDF.type)
                        // --- DataQuery API
                        // .sample()
                        .randomOrder()
                        .limit(10)
                        .peek(x -> System.out.println("GOT: " + x.toConstructQuery()))
                        .exec()
                        // --- RxJava API
                        // .firstElement()
                        .toList()
                        .timeout(60, TimeUnit.SECONDS)
                        .blockingGet();
        List<String> items = fc
                .stream()
                .map(x -> (x.getValue() + ": " + x.getFocusCount()).substring(0, 16))
                .collect(Collectors.toList());

        return items;
    }

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     * @param service The message service. Automatically injected Spring managed bean.
     */
    public MainView(@Autowired GreetService service) {
        init();

        ListBox<String> facetList = new ListBox<>();
        // ListBox<String> facetValueList = new ListBox<>();

        facetList.setItems(randomItems());

        // VerticalLayout mainPanel = new VerticalLayout();

        VerticalLayout facetPanel = new VerticalLayout();
        // VerticalLayout facetValuePanel = new VerticalLayout();
        VerticalLayout resultPanel = new VerticalLayout();

        // SplitLayout mainSplit = new SplitLayout();
        VerticalLayout mainPanel = new VerticalLayout();
        // SplitLayout facetAndValuesSplit = new SplitLayout();

        // mainSplit.setOrientation(Orientation.VERTICAL);

        // facetAndValuesSplit.addToPrimary(facetPanel);
        // facetAndValuesSplit.addToSecondary(facetValuePanel);

        TextField facetSearchField = new TextField();
        facetSearchField.setWidthFull();

        facetPanel.add(new Label("Facets"));
        facetPanel.add(facetSearchField);
        facetPanel.add(facetList);

        // facetValuePanel.add(new Label("Facet Values"));
        // facetValuePanel.add(facetValueList);

        TextField searchField = new TextField();
        searchField.setWidthFull();
        // searchField.setLabel("Find");

        resultPanel.add("Results go here");

        // mainPanel.setHeightFull();
        // mainPanel.setWidthFull();
        // mainSplit.addToPrimary(facetAndValuesSplit);
        // mainSplit.addToSecondary(resultPanel);

        mainPanel.add(searchField);
        mainPanel.add(resultPanel);

        addToNavbar(mainPanel);
        addToDrawer(facetPanel);

        setPrimarySection(Section.DRAWER);

        // addToNavbar(list);

        // Use TextField for standard text input
        // TextField textField = new TextField("Your name ");
        //
        // // Button click listeners can be defined as lambda expressions
        // Button button = new Button("Say hello", e -> {
        // facetList.setItems(randomItems());
        // Notification.show(service.greet(textField.getValue()));
        // });
        //
        // // Theme variants give you predefined extra styles for components.
        // // Example: Primary button is more prominent look.
        // button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        //
        // // You can specify keyboard shortcuts for buttons.
        // // Example: Pressing enter in this view clicks the Button.
        // button.addClickShortcut(Key.ENTER);
        //
        // // Use custom CSS classes to apply styling. This is defined in
        // shared-styles.css.
        // //addClassName("centered-content");
        //
        // // addToNavbar(textField, button);
    }

}
