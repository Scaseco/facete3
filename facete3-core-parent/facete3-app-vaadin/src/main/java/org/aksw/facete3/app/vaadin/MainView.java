package org.aksw.facete3.app.vaadin;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.plugin.JenaPluginFacete3;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.impl.NodePathletPath;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.aksw.jena_sparql_api.utils.CountInfo;
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
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@PWA(name = "Vaadin Application", shortName = "Vaadin App", description = "This is an example Vaadin application.",
        enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends AppLayout {

    private static final long serialVersionUID = 1L;
    protected PrefixMapping globalPrefixes = new PrefixMappingImpl();
    public RDFNode resourceViewActiveNode = null;
    protected FacetedQuery fq;

    public void init() {
        JenaSystem.init();
        JenaPluginFacete3.init();

        RDFConnection conn = RDFConnectionRemote
                .create()
                .destination("https://databus.dbpedia.org/repo/sparql")
                .acceptHeaderQuery(WebContent.contentTypeResultsXML)
                .build();

        Model dataModel = ModelFactory.createDefaultModel();
        XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
        FacetedQueryImpl.initResource(facetedQuery);
        fq = FacetedQueryImpl.create(facetedQuery, conn);
    }

    public List<FacetValueCount> randomItems() {
        System.out.println("Complex Query");
        List<FacetValueCount> fc = fq
                .focus()
                .fwd()
                .facetValueCounts()
                .exclude(RDF.type)
                .randomOrder()
                .limit(10)
                .peek(x -> System.out.println("GOT: " + x.toConstructQuery()))
                .exec()
                .toList()
                .timeout(60, TimeUnit.SECONDS)
                .blockingGet();
        return fc;
    }

    public int getFacetValueCountCount() {
        long count = fq
                .focus()
                .fwd()
                .facetValueCounts()
                .exclude(RDF.type)
                .count()
                .timeout(60, TimeUnit.SECONDS)
                .blockingGet()
                .getCount();
        System.out.println(count);
        int countAsInt = (int) Math.min(count, Integer.MAX_VALUE);
        return countAsInt;
    }

    public Stream<FacetValueCount> fetchFacetValueCount(Query<FacetValueCount, Void> query) {
        Stream<FacetValueCount> fvc = fq
                .focus()
                .fwd()
                .facetValueCounts()
                .exclude(RDF.type)
                .randomOrder()
                .limit(query.getLimit() - 1)
                .offset(query.getOffset())
                .exec()
                .toList()
                .timeout(60, TimeUnit.SECONDS)
                .blockingGet()
                .stream();
        System.out.println(fvc);
        return fvc;
    }

    public int getFacetCountCount(Query<FacetCount, String> query) {
        DataQuery<FacetCount> dataQuery = fq.focus().fwd().facetCounts().exclude(RDF.type);
        // Add filter to query
        String filterText = query.getFilter().orElse("");
        System.out.println("Text: " + filterText);
        if (!filterText.isEmpty()) {
            UnaryRelation filter = KeywordSearchUtils
                    .createConceptRegexIncludeSubject(BinaryRelationImpl.create(RDFS.label), filterText);
            dataQuery.filter(filter);
        }
        long count = dataQuery.count().timeout(60, TimeUnit.SECONDS).blockingGet().getCount();
        int countAsInt = (int) Math.min(count, Integer.MAX_VALUE);
        System.out.println(countAsInt);
        return countAsInt;
    }

    public Stream<FacetCount> fetchFacetCount(Query<FacetCount, String> query) {

        DataQuery<FacetCount> dataQuery = fq.focus().fwd().facetCounts().exclude(RDF.type);
        // Add filter to query
        String filterText = query.getFilter().orElse("");
        System.out.println("Text: " + filterText);
        if (!filterText.isEmpty()) {
            UnaryRelation filter = KeywordSearchUtils
                    .createConceptRegexIncludeSubject(BinaryRelationImpl.create(RDFS.label), filterText);
            dataQuery.filter(filter);
        }

        List<QuerySortOrder> sortOrders = query.getSortOrders();
        int limit = query.getLimit() - 1;
        if (sortOrders.isEmpty()) {
            // dataQuery.randomOrder();
        } else {
            // 0 = Ascending, 1 = Descending
            SortDirection vaadiDirection = sortOrders.get(0).getDirection();
            int sortDir = vaadiDirection == SortDirection.ASCENDING ? org.apache.jena.query.Query.ORDER_ASCENDING
                    : org.apache.jena.query.Query.ORDER_DESCENDING;
            System.out.println(sortDir);
            dataQuery.addOrderBy(new NodePathletPath(Path.newPath()), sortDir);
            // lol?
            limit += limit;
        }
        List<FacetCount> facetCountsList = dataQuery
                .limit(limit)
                .offset(query.getOffset())
                .exec()
                .toList()
                .timeout(60, TimeUnit.SECONDS)
                .blockingGet();
        System.out.println(facetCountsList.size() + " " + query.getLimit());
        Stream<FacetCount> facetCounts = facetCountsList.stream();
        return facetCounts;
    }

    public MainView() {
        init();

        HorizontalLayout mainPanel = new HorizontalLayout();
        setContent(mainPanel);

        VerticalLayout facetPanel = new VerticalLayout();
        mainPanel.add(facetPanel);
        facetPanel.add(new Label("Facets"));

        TextField facetSearchField = new TextField();
        facetPanel.add(facetSearchField);
        facetSearchField.setWidthFull();
        CallbackDataProvider<FacetCount, String> provider = DataProvider
                .fromFilteringCallbacks(query -> fetchFacetCount(query), query -> getFacetCountCount(query));
        ConfigurableFilterDataProvider<FacetCount, Void, String> wrapper = provider.withConfigurableFilter();

        Grid<FacetCount> grid = new Grid<>(FacetCount.class);
        facetPanel.add(grid);
        // Necessary because vaadin adds all getX methods as columns
        grid.getColumns().forEach(grid::removeColumn);
        grid.addColumn(FacetCount::getPredicate).setSortProperty("predicate");
        // How to do nested ::?
        grid.addColumn("distinctValueCount.count");
        HeaderRow filterRow = grid.appendHeaderRow();
        facetSearchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            if (filter.trim().isEmpty()) {
                // null disables filtering
                filter = null;
            }

            System.out.println("Text2: " + filter);
            wrapper.setFilter(filter);
        });
        grid.setDataProvider(wrapper);
    }
}
