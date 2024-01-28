package org.aksw.facete3.app.vaadin.components;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.facete3.app.vaadin.ConfigFaceteVaadin;
import org.aksw.facete3.app.vaadin.ConfigFacetedBrowserView;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.facete3.app.vaadin.ResourceHolder;
import org.aksw.facete3.app.vaadin.SearchSensitiveRDFConnectionTransform;
import org.aksw.facete3.app.vaadin.component.facet.FacetValueCountBox;
import org.aksw.facete3.app.vaadin.components.sparql.wizard.SparqlConnectionWizard;
import org.aksw.facete3.app.vaadin.plugin.search.SearchPlugin;
import org.aksw.facete3.app.vaadin.plugin.view.ViewFactory;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.facete3.app.vaadin.providers.FacetCountProvider;
import org.aksw.facete3.app.vaadin.providers.FacetValueCountDataProvider;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnionDefaultGraph;
import org.aksw.jena_sparql_api.rx.entity.model.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderNodeQuery;
import org.aksw.jena_sparql_api.vaadin.util.VaadinStyleUtils;
import org.aksw.jenax.analytics.core.RootedQuery;
import org.aksw.jenax.arq.aggregation.BestLiteralConfig;
import org.aksw.jenax.arq.datashape.viewselector.ViewTemplate;
import org.aksw.jenax.arq.datashape.viewselector.ViewTemplateImpl;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionTransform;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.aksw.jenax.vaadin.component.breadcrumb.Breadcrumb;
import org.aksw.jenax.vaadin.component.grid.sparql.SparqlGridComponent;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.LabelServiceSwitchable;
import org.aksw.vaadin.common.component.tab.TabSheet;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.Query;

public class FacetedBrowserView
    extends VerticalLayout {

    protected FacetedBrowserToolbar toolbar;

    protected ConstraintsComponent constraintsComponent;
    protected SearchComponent searchComponent;
    protected FacetCountComponent facetCountComponent;

    /** This breadcrumb controls the focus, i.e. what items to show. The focus is also the basis for facet (value) counts.  */
    protected Breadcrumb<FacetStep> focusBreadcrumb;

    /** The last part of this breadcrumb is a direction toggle for whether to show forward or backward facets */
    protected FacetPathComponent facetListBreadcrumb;
    protected FacetValueCountBox facetValueCountComponent;

    protected Facete3Wrapper facete3;
    protected ItemComponent itemComponent;

    protected SparqlGridComponent sparqlGridComponent;

    protected Label connectionInfo;

    protected MapComponent mapComponent = new MapComponent();

//    protected ResourceComponent resourceComponent;

    /** The resource browser should eventually supersede the resourceComponent */
    protected ResourceBrowserComponent resourceBrowserComponent;

//  @Autowired
    // protected RDFConnection baseDataConnection;
    protected RdfDataSource dataSource;

//    protected SearchProvider searchProvider;
    protected InMemoryDataProvider<SearchPlugin> searchPluginDataProvider;
    protected SearchPlugin activeSearchPlugin;

//    @Autowired(required = false)
//    protected SearchSensitiveRDFConnectionTransform searchSensitiveRdfConnectionTransform = null;

    @Autowired
    protected ConfigurableApplicationContext cxt;


    protected LabelService<Node, String> labelMgr;

    public LabelService<Node, String> getLabelMgr() {
        return labelMgr;
    }

    public Facete3Wrapper getFacetedSearchSession() {
        return facete3;
    }

    public FacetedBrowserView(
            // RDFConnection baseDataConnection,
            RdfDataSource dataSource,
//            SearchPlugin searchPlugin,
            InMemoryDataProvider<SearchPlugin> searchPluginProvider,
            PrefixMapping prefixMapping,
            Facete3Wrapper facete3,
            FacetCountProvider facetCountProvider,
            FacetValueCountDataProvider facetValueCountProvider,
            // ItemProvider itemProvider,
            DataProviderNodeQuery itemProvider,
            ConfigFaceteVaadin config,
            ViewManager viewManagerFull,
            ViewManager viewManagerDetails,
            BestLiteralConfig bestLabelConfig,
            LabelService<Node, String> labelMgr,
            ExecutorService executorService
            ) {

        this.labelMgr = labelMgr;

        ViewFactory dftViewFactory = new ViewFactory() {

            @Override
            public ViewTemplate getViewTemplate() {

                // FIXME The view template should be static;
                // at present each invocation creates a new one

                EntityQueryImpl attrQuery = new EntityQueryImpl();


                /*
                 * Unfortunately there is no syntax (yet) for entity-centric sparql;
                 * the following is (roughly)
                 *
                 * ENTITY ?x
                 * CONSTRUCT { }
                 * WHERE { }
                 *
                 */
                List<Var> vars = Collections.singletonList(Vars.x);
                EntityGraphFragment fragment = new EntityGraphFragment(
                        vars,
                        new EntityTemplateImpl(Collections.<Node>singletonList(Vars.x), new Template(
                                BasicPattern.wrap(Collections.emptyList()))),
                        // ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o)
                        new ElementGroup()
                        );

                attrQuery.getOptionalJoins().add(new GraphPartitionJoin(fragment));

                ElementUnion union = new ElementUnion();
                union.addElement(ElementUtils.createElementTriple(Vars.x, Vars.p, Vars.o));
                union.addElement(ElementUtils.createElementTriple(Vars.s, Vars.x, Vars.o));
                union.addElement(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.x));

                return new ViewTemplateImpl(
                        // The id of the view
                        ModelFactory.createDefaultModel()
                            .createResource("http://cord19.aksw.org/view/resource-explorer")
                            .addLiteral(RDFS.label, "Resource"),

                        // The condition for which set of resources the view is applicable
                        new Concept(union, Vars.x),

                        // The entity-centric construct query for what information to fetch when applying the view
                        attrQuery
                        );

            }

            @Override
            public Component createComponent(RDFNode data) {
                // ResourceComponentOld result = new ResourceComponentOld(PrefixMapping.Extended, viewManagerFull, labelMgr);
                ResourceViewComponent result = new ResourceViewComponent(PrefixMapping.Extended, viewManagerFull, labelMgr);
                result.setNode(data == null ? null : data.asNode(), dataSource.asQef());
                return result;
            }
        };


        // this.baseDataConnection = baseDataConnection;
        this.dataSource = dataSource;
        this.searchPluginDataProvider = searchPluginProvider;
        this.activeSearchPlugin = searchPluginDataProvider.fetch(new Query<>()).limit(1).findFirst().orElse(null);
        this.facete3 = facete3;

        LookupService<Node, String> labelService = LabelUtils.getLabelLookupService(
                dataSource.asQef(),
                // new QueryExecutionFactoryOverSparqlQueryConnection(baseDataConnection),
                RDFS.label,
                prefixMapping,
                ConfigFacetedBrowserView.DFT_LOOKUPSIZE);

//        TransformService transformService = new TransformService(config.getPrefixFile());

        Function<RDFNode, String> labelFunction = rdfNode ->
            Objects.toString(LabelUtils.getOrDeriveLabel(rdfNode, bestLabelConfig));

        toolbar = new FacetedBrowserToolbar();
        facetCountComponent = new FacetCountComponent(this, facetCountProvider);
        facetValueCountComponent = new FacetValueCountBox(this, facetValueCountProvider);
        facetListBreadcrumb = new FacetPathComponent(this, facete3, labelService);

        focusBreadcrumb = new Breadcrumb<>(FacetPath.newAbsolutePath(), labelMgr, Breadcrumb.labelAssemblerForFacetPath());
        focusBreadcrumb.setWidthFull();
        focusBreadcrumb.getModel().addValueChangeListener(ev -> {
            Path<FacetStep> path = ev.getNewValue();
            FacetNode tgt = facete3.getFacetedQuery().root().resolve(path);
            tgt.chFocus();

            FacetPath focusPath = tgt.facetPath();
            // Reset the facet counts list
            FacetDirNode facetDirNode = facete3.getFocusToFacetDir().getOrDefault(focusPath, tgt.fwd());
            facete3.setFacetDirNode(facetDirNode);


            // TODO Reset the facet values to the state for the focus

            refreshAll();
        });


        itemComponent = new ItemComponent(this, itemProvider, viewManagerDetails, labelMgr) {
            // Whenever refreshing the sparql-table add a listener for viewing the details
            // of the focused node
            public void refreshTable() {
                super.refreshTable();
                tableGrid.addCellFocusListener(ev -> {
                    Column<?> column = ev.getColumn().orElse(null);
                    Binding binding = ev.getItem().orElse(null);
                    String columnKey = column.getKey();
                    Node node = binding.get(columnKey);
                    if (node != null) {
                        viewNode(node);
                    }
                });
            };
        };
        itemComponent.setHeightFull();

        // baseConcept
        sparqlGridComponent = new SparqlGridComponent(dataSource, ConceptUtils.createSubjectConcept(), labelMgr);
        sparqlGridComponent.setPageSize(ConfigFacetedBrowserView.DFT_GRID_PAGESIZE);

        resourceBrowserComponent = new ResourceBrowserComponent(viewManagerFull, labelFunction, dftViewFactory);
        resourceBrowserComponent.setWidthFull();
        resourceBrowserComponent.setHeightFull();

        constraintsComponent = new ConstraintsComponent(this, facete3, labelMgr);
        constraintsComponent.setMaxHeight("40px");
        connectionInfo = new Label();
        connectionInfo.getElement().setAttribute("theme", "badge primary pill");

        searchComponent = new SearchComponent(this, () -> activeSearchPlugin.getSearchProvider());
        toolbar.add(searchComponent);

        Select<SearchPlugin> searchPluginSelect = new Select<>();
        searchPluginSelect.setDataProvider(searchPluginProvider);
        searchPluginSelect.setValue(activeSearchPlugin);
        searchPluginSelect.setTextRenderer(item -> item.getSearchProvider().toString());
        searchPluginSelect.addValueChangeListener(event -> {
            activeSearchPlugin = event.getValue();
            System.out.println("Active search plugin: " + activeSearchPlugin);
        });

        if(System.getProperty("UI.DISABLE.SEARCH.PLUGIN.SELECT") != null) {
            searchPluginSelect.setVisible(false);
        }
        toolbar.add(searchPluginSelect);

        Button changeConnectionBtn = new Button(connectionInfo);
        if (System.getProperty("UI.DISABLE.CONNECTIONINFO") != null) {
            changeConnectionBtn.setVisible(false);
        }
        toolbar.add(changeConnectionBtn);

        connectionInfo.addClassName("no-wrap");
        toolbar.setFlexGrow(1, searchComponent);

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

        layout.add(new SparqlConnectionWizard(executorService) {
            @Override
            public void onWizardCompleted() {
                ResourceHolder opHolder = cxt.getBean(ResourceHolder.class);

                Boolean unionDefaultGraphMode = sparqlEndpointForm.getUnionDefaultGraphMode().getValue();
                Op op = getConjureSpecification(true, unionDefaultGraphMode);

//                if (input.getUnionDefaultGraphMode().isEnabled()) {
//                    op = OpUnionDefaultGraph.create(op);
//                }

                opHolder.set(op);
//                System.out.println("INVOKING REFRESH");
//                System.out.println("Given cxt:\n" + toString(cxt));
//                System.out.println("Updated dataRef " + System.identityHashCode(dataRef));


                Set<Node> selectedTypes = getSelectedTypes();

                Fragment1 initialConcept = selectedTypes.isEmpty()
                        ? ConceptUtils.createSubjectConcept()
                        : Concept.createForTypes(selectedTypes);
                System.err.println("Base concept restricted to types: " + selectedTypes);
                // facete3.getFacetedQuery().baseConcept(baseConcept);
                facete3.setInitialConcept(initialConcept);

                // This line triggers immediate data retrieval on the subsequent refresh
                setSearchUnconstrained();

                refreshAllNew();


                dialog.close();
            }
        });

        // layout.add(input);
        Button applyBtn = new Button("Apply");
        // TODO fix or remove apply button
        // Hack to disable the apply button
        if (System.getProperty("UI.DISABLE.APPLY.BUTTON") == null) {
            layout.add(applyBtn);
        }
        applyBtn.addClickListener(event -> {
            String urlStr = input.getServiceUrl();


            ResourceHolder opHolder = cxt.getBean(ResourceHolder.class);

            RdfDataRefSparqlEndpoint dataRef = ModelFactory.createDefaultModel().createResource().as(RdfDataRefSparqlEndpoint.class);
            dataRef.setServiceUrl(urlStr);

            Op op = OpDataRefResource.from(dataRef);

            if (input.getUnionDefaultGraphMode().isEnabled()) {
                op = OpUnionDefaultGraph.create(op);
            }

            opHolder.set(op);
//            System.out.println("INVOKING REFRESH");
//            System.out.println("Given cxt:\n" + toString(cxt));
//            System.out.println("Updated dataRef " + System.identityHashCode(dataRef));
            refreshAllNew();

            // TODO Now all dataProviders need to refresh
            dialog.close();
        });


        dialog.add(layout);


        Button refreshBtn = new Button(new Icon(VaadinIcon.REFRESH));
        refreshBtn.getElement().setProperty("title", "Refresh all data in this view");
        refreshBtn.addClickListener(event -> {
            refreshAllNew();
        });
        if(System.getProperty("UI.DISABLE.REFRESH.BUTTON") != null) {
            refreshBtn.setVisible(false);
        }
        toolbar.add(refreshBtn);

        Button toggleLabelsBtn = new Button(VaadinIcon.TEXT_LABEL.create(), ev -> {
            LabelServiceSwitchable<Node, String> switchable = (LabelServiceSwitchable<Node, String>)labelMgr;
            switchable.next();
            switchable.refreshAll();
            // LookupService<Node, String> ls1 = LabelUtils.getLabelLookupService(qef, labelProperty, DefaultPrefixes.get());
            //LookupService<Node, String> ls2 = keys -> Flowable.fromIterable(keys).map(k -> Map.entry(k, Objects.toString(k)));
            //VaadinRdfLabelMgrImpl labelMgr = new VaadinRdfLabelMgrImpl(ls1);
        });
        if(System.getProperty("UI.DISABLE.TOGGLE.LABELS.BUTTON") != null) {
            toggleLabelsBtn.setVisible(false);
        }
        toolbar.add(toggleLabelsBtn);

        Button configBtn = new Button(new Icon(VaadinIcon.COG));
        toolbar.add(configBtn);

        changeConnectionBtn.addClickListener(event -> {
            dialog.open();
        });

        configBtn.addClickListener(event -> {
            dialog.open();
//            input.focus();
        });

        add(toolbar);
//        appContent.add(getNaturalLanguageInterfaceComponent());
        add(constraintsComponent);
        add(getFacete3Component());
        // onRefresh();
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

    /**
     * Handler for refresh events
     * Calling this method should be handled by the spring context / config class
     * that creates this component
     *
     */
    @PostConstruct
    public void onRefresh() {
        ResourceHolder configHolder = cxt.getBean(ResourceHolder.class);
        Resource config = configHolder.get();

        String connectionLabel;
//        if (op != null) {
//        	if (op instanceof OpDataR)
//	        String url = Optional.ofNullable(op)
//	                .map(RdfDataRefSparqlEndpoint::getServiceUrl)
//	                .orElse("unknown connection");
//        } else {
//
//        }
        String url = "unknown connection";

        connectionInfo.setText(url);
    }

    /** Remove constraints from the active search plugin */
    public void setSearchUnconstrained() {
        // Search providers do not support null - the empty string means 'unconstrained'.
        RDFNodeSpec searchResult = activeSearchPlugin.getSearchProvider().search("");
        handleSearchResponse(searchResult);
    }

    public void refreshAllNew() {

        if (true) {
            Fragment1 baseConcept = facete3.getFacetedQuery().baseConcept();
            sparqlGridComponent.setBaseConcept(baseConcept);
            sparqlGridComponent.resetGrid();
        }

        // Sync the breadcrumb with the focus
        FacetPath facetPath = facete3.getFacetedQuery().focus().facetPath();
        // List<Directed<FacetNode>> oldPath = facete3.getFacetedQuery().focus().path();


        focusBreadcrumb.setValue(facetPath);

        constraintsComponent.refresh();
        facetValueCountComponent.getDataProvider().refreshAll();
        itemComponent.refreshTable();

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

//        appContent.add(getNaturalLanguageInterfaceComponent());

        appContent.add(constraintsComponent);

        appContent.add(getFacete3Component());
        return appContent;
    }

    protected Component getFacete3Component() {
        SplitLayout component = new SplitLayout();
        component.setSizeFull();
        component.setOrientation(Orientation.HORIZONTAL);
        component.setSplitterPosition(20);


        component.addToPrimary(getFacetComponent());


        component.addToSecondary(getResultsComponent());
        return component;
    }

    protected Component getFacetComponent() {
        // Generic facets
        // SplitLayout facetComponent = new SplitLayout();
        VerticalLayout facetComponent = new VerticalLayout();

        // facetComponent.setWidthFull();
        //facetComponent.setHeight("500px");
//        facetComponent.setSizeFull();
//        facetComponent.setOrientation(Orientation.VERTICAL);
//        facetComponent.addToPrimary(facetCountComponent);
//        facetComponent.addToSecondary(facetValueCountComponent);

        VaadinStyleUtils.setResizeVertical(facetCountComponent.getStyle());
        VaadinStyleUtils.setResizeVertical(facetValueCountComponent.getStyle());

        facetComponent.add(facetListBreadcrumb);
        facetComponent.add(facetCountComponent);
        facetComponent.add(facetValueCountComponent);

        //VerticalLayout component = new VerticalLayout();
        // component.add(facetPathComponent);
//        component.add(constraintsComponent);
        // component.add(facetComponent);

        // facetComponent.setSplitterPosition(20);


        TabSheet tabSheet = new TabSheet();
        // tabSheet.setHeight("500px");
        tabSheet.add(VaadinIcon.FILE_TREE_SUB.create(), facetComponent);

        tabSheet.add(VaadinIcon.ELLIPSIS_V.create(), new Span("Custom facets"));


        return tabSheet;
    }

    protected Component getResultsComponent() {
        SplitLayout component = new SplitLayout();
        component.setOrientation(Orientation.HORIZONTAL);

        VerticalLayout resultViewLayout = new VerticalLayout(focusBreadcrumb, itemComponent);
        resultViewLayout.setSizeFull();
        resultViewLayout.setFlexGrow(1, focusBreadcrumb);
        component.addToPrimary(resultViewLayout);
        // component.addToPrimary(focusBreadcrumb, itemComponent);
        // component.addToPrimary(itemComponent);

        // component.addToPrimary(focusBreadcrumb);
        // component.addToPrimary(itemComponent);
        // component.addToPrimary(sparqlGridComponent);



        VerticalLayout detailsPanel = new VerticalLayout();
        detailsPanel.setSizeFull();
        if (System.getProperty("UI.DISABLE.MAPCOMPONENT") == null) {
            detailsPanel.add(mapComponent);
        }
        detailsPanel.add(resourceBrowserComponent);

        component.addToSecondary(detailsPanel);
        return component;
    }

    public void viewNode(Node node) {
        RDFNode rdfNode = facete3.fetchIfResource(node);
        if ( rdfNode != null )
        {  resourceBrowserComponent.setNode(rdfNode); }

    }

    public void viewNode(FacetValueCount facetValueCount) {
        viewNode(facetValueCount.getValue());
    }

    public void selectFacet(Node node) {
        facetValueCountComponent.getDataProvider().setSelectedFacet(node);
        facetValueCountComponent.getDataProvider().setFacetDirNode(facete3.getFacetDirNode());
        facetValueCountComponent.getDataProvider().refreshAll(); // .refresh();
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
        facetListBreadcrumb.refresh();
    }

    public void resetPath() {
        facete3.resetPath();
        facetCountComponent.refresh();
        facetListBreadcrumb.refresh();
    }

    public void addFacetToPath(FacetCount facet) {
        facete3.addFacetToPath(facet);
        facetCountComponent.refresh();
        facetListBreadcrumb.refresh();
    }

    public void changeFocus(FacetNode facet) {
        facete3.changeFocus(facet);
        facetCountComponent.refresh();
        facetListBreadcrumb.refresh();
    }

    public void handleSearchResponse(RDFNodeSpec rdfNodeSpec) {

        Fragment1 searchConcept;
        if (rdfNodeSpec.isCollection()) {
            searchConcept = ConceptUtils.createConceptFromRdfNodes(rdfNodeSpec.getCollection());
        } else if (rdfNodeSpec.isRootedQuery()) {

            // FIXME Not all possible cases are handled here
            // We just assume that the rooted query's root node is a variable that appears in the element

            RootedQuery rq = rdfNodeSpec.getRootedQuery();
            Var var = (Var)rq.getRootNode();
            Element element = ElementUtils.flatten(rq.getObjectQuery().getRelation().getElement());
            searchConcept = new Concept(element, var);

            // FIXME We need a strategy when to wrap the base concept as a sub query
            if (!searchConcept.isSubjectConcept()) {
                org.apache.jena.query.Query subQuery = searchConcept.toQuery();
                subQuery.setDistinct(true);
                searchConcept = new Concept(new ElementSubQuery(subQuery), searchConcept.getVar());
            }
        } else {
            throw new RuntimeException("Unknown rdfNodeSpec type "  + rdfNodeSpec);
        }

        Fragment1 initialConcept = facete3.getInitialConcept();

        Fragment1 baseConcept = initialConcept.joinOn(initialConcept.getVar()).with(searchConcept).toFragment1();
        System.err.println("Base concept: " + baseConcept);
        facete3.setBaseConcept(baseConcept);


        // RDFConnection effectiveDataConnection = baseDataConnection;

        RdfDataSource effectiveDataSource = () -> {
            RDFConnection effectiveDataConnection = dataSource.getConnection();
            SearchSensitiveRDFConnectionTransform connectionTransform = activeSearchPlugin.getConnectionTransform();
            if (connectionTransform != null) {
                RDFConnectionTransform connXform = connectionTransform.create(rdfNodeSpec);
                effectiveDataConnection = connXform.apply(effectiveDataConnection);
            }
            return effectiveDataConnection;
        };
        facete3.getFacetedQuery().dataSource(effectiveDataSource);

        // facete3.getFacetedQuery().connection(effectiveDataConnection);

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

    private Fragment1 createConcept(List<String> ids) {
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
