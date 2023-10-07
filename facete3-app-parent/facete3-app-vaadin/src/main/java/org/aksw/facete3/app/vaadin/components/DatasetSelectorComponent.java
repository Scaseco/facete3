package org.aksw.facete3.app.vaadin.components;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.facete3.app.vaadin.ConfigFacetedBrowserView;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceMetamodel;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.ResourceCache;
import org.aksw.jena_sparql_api.schema.ResourceExplorer;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.jenax.arq.util.triple.ModelUtils;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryDataset;
import org.aksw.vaadin.common.component.managed.ManagedComponentSimple;
import org.aksw.vaadin.common.component.tab.TabSheet;
import org.aksw.vaadin.component.rdf_term_editor.RdfTermEditor;
import org.aksw.vaadin.shacl.ShaclTreeGrid;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.dom.Element;





/**
 *
 *
 * @author raven
 *
 */
class DatasetCreatorForm
    extends FormLayout
{
    protected ComboBox<String> distributionType = new ComboBox<String>();
    protected TextField serviceUrl = new TextField();
    protected Pre rdf = new Pre();


    public DatasetCreatorForm() {

        DcatDataset dcatDataset = ModelFactory.createDefaultModel().createResource().as(DcatDataset.class);

        Binder<Resource> binder = new Binder<>(Resource.class);
        binder.setBean(dcatDataset);
//        binder.bindInstanceFields(dcatDataset);

        setResponsiveSteps(
                   new ResponsiveStep("25em", 1),
                   new ResponsiveStep("32em", 2),
                   new ResponsiveStep("40em", 3));

        MavenEntity mavenEntity = dcatDataset.as(MavenEntity.class);


        serviceUrl.setWidthFull();

//        serviceUrl.addValueChangeListener(event -> {
//            String value = event.getValue();
//            //DcatDataset dcatDataset = DcatDatasetCreation.fromDownloadUrl(value);
//
//            Model model = dcatDataset.getModel();
//            model.setNsPrefixes(DefaultPrefixes.prefixes);
//            ModelUtils.optimizePrefixes(model);
//
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            RDFDataMgr.write(out, dcatDataset.getModel(), RDFFormat.TURTLE_PRETTY);
//            String str = new String(out.toByteArray(), StandardCharsets.UTF_8);
//            rdf.setText(str);
//        });

        add(new H2("Dataset"), 3);
//
//        TextField groupIdField = new TextField();
//        TextField artifactIdField = new TextField();
//        TextField versionField = new TextField();
//        FormItem formItemX = addFormItem(groupIdField, "Group ID");
//        FormItem formItemY = addFormItem(artifactIdField, "Artifact ID");
//        FormItem formItemZ = addFormItem(versionField, "Version ID");
//
//
//        ComboBox<String> iriModeComboBox = new ComboBox<String>();
//        iriModeComboBox.setDataProvider(new ListDataProvider<>(Arrays.asList("Absolute", "Prefix", "Relative")));
//        FormItem iriModeFormItem = addFormItem(iriModeComboBox, "URL Mode");
//
//        TextField iriTextBox = new TextField();
//        FormItem iriFormItem = addFormItem(iriTextBox, "URL Mode");


        ComponentBundleMaven cbm = new ComponentBundleMaven.Installer().install(this);

//        TextField groupIdField = new TextField();
        binder.forField(cbm.getGroupIdTextField()).bind(r -> r.as(MavenEntity.class).getGroupId(), (r, v) -> r.as(MavenEntity.class).setGroupId(v));
        binder.forField(cbm.getArtifactIdTextField()).bind(r -> r.as(MavenEntity.class).getArtifactId(), (r, v) -> r.as(MavenEntity.class).setArtifactId(v));
        binder.forField(cbm.getVersionTextField()).bind(r -> r.as(MavenEntity.class).getVersion(), (r, v) -> r.as(MavenEntity.class).setVersion(v));

        //binder.forField(versionField).
//        binder.forMemberField(versionField).

        getElement().appendChild(new Element("hr"));

        add(new H2("Distributions"), 3);

        binder.addValueChangeListener(event -> {
            Model model = dcatDataset.getModel();
            model.setNsPrefixes(DefaultPrefixes.get());
            ModelUtils.optimizePrefixes(model);


            ByteArrayOutputStream out = new ByteArrayOutputStream();
            RDFDataMgr.write(out, dcatDataset.getModel(), RDFFormat.TURTLE_PRETTY);
            String str = new String(out.toByteArray(), StandardCharsets.UTF_8);
            rdf.setText(str);

        });

//        rdf.setReadOnly(true);
        add(new Icon(VaadinIcon.PLUS_SQUARE_O));
        distributionType.setWidthFull();
        distributionType.setDataProvider(new ListDataProvider<>(Arrays.asList("Download", "Git", "DCAT Link")));
        FormItem formItem1 = addFormItem(distributionType, "DistributionType");


        //setColspan(formItem1, 3);


        rdf.setWidthFull();
        FormItem formItem2 = addFormItem(serviceUrl, "Sparql Endpoint URL");
        setColspan(formItem2, 3);

        FormItem formItem3 = addFormItem(rdf, "Generated RDF");
        setColspan(formItem3, 3);



        RdfTermEditor rdfTermEditor = new RdfTermEditor();
        add(rdfTermEditor);
        // FormItem formItem4 = addFormItem()
        setColspan(rdfTermEditor, 3);


        Span span = new Span("Value");

        rdfTermEditor.addValueChangeListener(ev -> {
            span.setText("Value: " + Objects.toString(ev.getValue()));
        });

        add(span);
        //rdfTermEditor.addToComponent(this);

//        this.add(rdfTermEditor);

//        FormItem formItem = new FormItem();
//        Label label = new Label("Sparql Endpoint URL");
//        label.getElement().setAttribute("slot", "label");
//        formItem.add(label);
//        formItem.add(serviceUrl);
//        add(formItem, 3);

//        add(serviceUrl, 3);

//        DataRefSparqlEndpoint bean = ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class);
//        bean.setServiceUrl("http://");


//        binder.setBean(bean);
//        binder.bindInstanceFields(this);
    }


}


public class DatasetSelectorComponent extends TabSheet {

    public DatasetSelectorComponent() {
        super(new HorizontalLayout());
        this.getTabsComponent().setOrientation(Orientation.VERTICAL);
//        super(new VerticalLayout());
//        this.getTabsComponent().setOrientation(Orientation.HORIZONTAL);

        this.setSizeFull();
//        DatasetCreatorForm datasetCreator = new DatasetCreatorForm();
//        datasetCreator.setMinWidth("300px");
//        datasetCreator.setMinHeight("300px");
//        datasetCreator.add(new Span("World"));

        // ShaclFormOld shaclForm = new ShaclFormOld();

        Model shaclModel = RDFDataMgr.loadModel("dcat-ap_2.0.0_shacl_shapes.ttl");
        NodeSchema schema = shaclModel.createResource("http://data.europa.eu/r5r#Dataset_Shape").as(NodeSchemaFromNodeShape.class);

        // Delete the dcat:distribution property shape

        Node datasetNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");
        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        ResourceCache resourceCache = new ResourceCache();

        QueryExecutionFactory qef = new QueryExecutionFactoryDataset(ds);
        // SparqlQueryConnection conn = RDFConnectionFactory.connect(ds);
        ShapedNode sn = ShapedNode.create(datasetNode, schema, resourceCache, qef);
        LookupService<Node, ResourceMetamodel> metaDataService = ResourceExplorer.createMetamodelLookup(qef);

        Multimap<NodeSchema, Node> mm = HashMultimap.create();
        mm.put(schema, datasetNode);

        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
        dataFetcher.sync(mm, qef, metaDataService, resourceCache);

        List<ShapedNode> rootNodes = Collections.singletonList(sn);

        Model prefixes = RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
        LookupService<Node, String> labelService =
                LabelUtils.createLookupServiceForLabels(LabelUtils.getLabelLookupService(qef, RDFS.label, prefixes, ConfigFacetedBrowserView.DFT_LOOKUPSIZE), prefixes, prefixes).cache();



        GraphChange graphEditorModel = new GraphChange();


        TextArea status = new TextArea();
        status.setWidthFull();
        // FormItem statusFormItem = addFormItem(status, "Status");
        // setColspan(statusFormItem, 3);

        Runnable update = () -> {
                Model additions = ModelFactory.createModelForGraph(graphEditorModel.getAdditionGraph());
                Model deletions = ModelFactory.createModelForGraph(graphEditorModel.getDeletionGraph());

                String renameStr = graphEditorModel.getRenamedNodes().entrySet().stream()
                        .map(e -> e.getKey() + " -> " + e.getValue())
                        .collect(Collectors.joining("\n"));

                String replaceStr = graphEditorModel.getTripleReplacements().entrySet().stream()
                        .map(e -> e.getKey() + " -> " + e.getValue())
                        .collect(Collectors.joining("\n"));

//                String str = "Added:\n" + ShaclFormOld.toString(additions, RDFFormat.TURTLE_BLOCKS) + "\n"
//                        + "Removed:\n" + ShaclFormOld.toString(deletions, RDFFormat.TURTLE_BLOCKS) + "\n"
//                        + "Renamed:\n" + renameStr + "\n"
//                        + "Replaced:\n" + replaceStr + "\n";


                // status.setValue(str);
        };

        graphEditorModel.getAdditionGraph().addPropertyChangeListener(ev -> {
            update.run();
        });

        graphEditorModel.getDeletionGraph().addPropertyChangeListener(ev -> {
            update.run();
        });

        graphEditorModel.getRenamedNodes().addPropertyChangeListener(ev -> {
            update.run();
        });

        graphEditorModel.getTripleReplacements().addPropertyChangeListener(ev -> {
            update.run();
        });

        // HierarchicalDataProvider<Path<Node>, String> dataProvider = new HierarchicalDataProviderForShacl(ms, graphEditorModel);

        TreeGrid<Path<Node>> treeGrid = ShaclTreeGrid.createShaclEditor(
                graphEditorModel, rootNodes, labelService);

        // hierarchyColumn.setFlexGrow(1);
        // hierarchyColumn.setAutoWidth(true);

        // treeGrid.recalculateColumnWidths();
        // hierarchyColumn.setResizable(true);
        // hierarchyColumn.setFrozen(true);

        VerticalLayout v = new VerticalLayout();
        v.setSizeFull();
        v.add(treeGrid);
        v.add(status);
        // treeGrid.setSelectionMode(SelectionMode.SINGLE);
        Button expandAllBtn = new Button("Expand all");
        expandAllBtn.addClickListener(ev -> {
            // PathOpsNode.newAbsolutePath()
            Set<Path<Node>> selectedItems = treeGrid.getSelectedItems();
            System.out.println("Selected items: " + selectedItems);
            treeGrid.expandRecursively(selectedItems, 5);
        });

        v.add(expandAllBtn);


        this.newTab("catalog", "Browse Catalog", new ManagedComponentSimple(new Span("Hello")));
        this.newTab("test", "Test", new ManagedComponentSimple(v));
        // this.newTab("new-dataset", "New Dataset", new ManagedComponentSimple(datasetCreator));
        // this.newTab("new-dataset", "New Dataset", new ManagedComponentSimple(shaclForm));



        VerticalLayout catalogMgmtPanel = new VerticalLayout();
        catalogMgmtPanel.setSizeFull();
        Button showAddCatalogBtn = new Button("Add Catalog");


        Dialog addCatalogDialog = new Dialog();


//        ShaclFormOld addCatalogForm = new ShaclFormOld();
//        addCatalogDialog.add(addCatalogForm);
//        addCatalogDialog.setSizeFull();
//
//        showAddCatalogBtn.addClickListener(event -> {
//            addCatalogDialog.open();
////            input.focus();
//        });
//
//        catalogMgmtPanel.add(showAddCatalogBtn);


        this.newTab("manage-catalog", "Manage Catalogs", new ManagedComponentSimple(catalogMgmtPanel));
    }



}

//public class SearchPluginForm extends FormLayout {
//    protected ComboBox<ServiceStatus> serviceUrl = new ComboBox<>();
////    protected TextField lastName =
////            new TextField("Last name");
////    private ComboBox<Gender> gender =
////            new ComboBox<>("Gender");
//
//    public ComboBox<ServiceStatus> getServiceUrl() {
//        return serviceUrl;
//    }
//
//    public SearchPluginForm() {
//
//        setResponsiveSteps(
//                   new ResponsiveStep("25em", 1),
//                   new ResponsiveStep("32em", 2),
//                   new ResponsiveStep("40em", 3));
//
//        serviceUrl.setDataProvider(new ListDataProvider() {
//            @Override
//            protected void applyFilter(DataQuery<ServiceStatus> dataQuery, String filterText) {
//                UnaryRelation filter = KeywordSearchUtils.createConceptRegexLabelOnly(
//                        BinaryRelationImpl.create(ResourceFactory.createProperty("http://www.w3.org/ns/sparql-service-description#endpoint")), filterText);
//                dataQuery.filter(filter);
//            }
//
//            @Override
//            protected DataQuery<ServiceStatus> getDataQuery() {
//                DataQuery<ServiceStatus> dq = FacetedQueryBuilder.builder()
//                        .configDataConnection()
//                            .setSource(model)
//                        .end()
//                        .create()
//                        .baseConcept(ConceptUtils.createForRdfType("http://www.w3.org/ns/sparql-service-description#Service"))
//                        .focus()
//                        .availableValues()
//                        .as(ServiceStatus.class)
//                        .add(ResourceFactory.createProperty("http://www.w3.org/ns/sparql-service-description#endpoint"))
//                        .addOptional(ResourceFactory.createProperty("https://schema.org/serverStatus"))
//                        .addOptional(ResourceFactory.createProperty("https://schema.org/dateModified"))
//                        ;
//                return dq;
//            }
//        });
//
//        serviceUrl.setItemLabelGenerator(s -> Optional.ofNullable(s.getEndpoint()).orElse("(null)"));
//        serviceUrl.setRenderer(new ComponentRenderer<>(serviceStatus -> {
//            HorizontalLayout layout = new HorizontalLayout();
//
//            XSDDateTime timestamp = serviceStatus.getDateModified();
//            String timeAgo = timestamp == null
//                    ? "-"
//                    : TimeAgo.toDuration(ChronoUnit.MILLIS.between(
//                            timestamp.asCalendar().toInstant(), Instant.now()),
//                            TimeAgo::fmtCompact);
//
//            String statusStr = Optional.ofNullable(serviceStatus.getServerStatus()).orElse("").toLowerCase();
//
//            // Ternary state: online? yes/no/unknown (null)
//            Boolean isOnline = statusStr == null
//                    ? null
//                    : statusStr.contains("online")
//                        ? Boolean.TRUE
//                        : statusStr.contains("offline")
//                            ? Boolean.FALSE
//                            : null;
//
//            String endpointStr = Optional.ofNullable(serviceStatus.getEndpoint()).orElse("(null)");
//
//            // Note: Marking potentially offline endpoints as grey looks better than in rede
//            // Also, the endpoint may be online after all; the server state is just an indicator
//            String styleStr = isOnline == null
//                    ? "var(--lumo-tertiary-text-color)"
//                    : isOnline
//                        ? "var(--lumo-success-text-color)"
//                        : "var(--lumo-tertiary-text-color)";
//                        //: "var(--lumo-error-text-color)";
//
//            Icon icon = VaadinIcon.CIRCLE.create();
//            icon.getStyle()
//                .set("color", styleStr)
//                .set("width", "1em")
//                .set("height", "1em");
//
//            Span urlSpan = new Span(endpointStr);
//
//            Span ago = new Span(timeAgo);
//            ago.addClassName("detail-text");
//
//            layout.add(icon);
//            layout.add(urlSpan);
//            layout.add(ago);
//
//            layout.setFlexGrow(1, urlSpan);
//
//            return layout;
//        }));
//
//        FormItem formItem = addFormItem(serviceUrl, "Sparql Endpoint URL");
//        serviceUrl.setWidthFull();
//        setColspan(formItem, 3);
////        FormItem formItem = new FormItem();
////        Label label = new Label("Sparql Endpoint URL");
////        label.getElement().setAttribute("slot", "label");
////        formItem.add(label);
////        formItem.add(serviceUrl);
////        add(formItem, 3);
//
////        add(serviceUrl, 3);
//
////        DataRefSparqlEndpoint bean = ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class);
////        bean.setServiceUrl("http://");
//
////        Binder<PlainDataRefSparqlEndpoint> binder = new Binder<>(PlainDataRefSparqlEndpoint.class);
////        binder.setBean(bean);
////        binder.bindInstanceFields(this);
//    }
//}

