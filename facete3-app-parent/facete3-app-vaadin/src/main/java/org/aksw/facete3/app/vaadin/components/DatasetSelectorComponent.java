package org.aksw.facete3.app.vaadin.components;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.CollectionChangedEvent;
import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.path.core.Path;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.facete3.app.shared.label.LabelUtils;
import org.aksw.facete3.app.vaadin.components.rdf.editor.RdfTermEditor;
import org.aksw.facete3.app.vaadin.plugin.ManagedComponentSimple;
import org.aksw.jena_sparql_api.collection.GraphChange;
import org.aksw.jena_sparql_api.collection.RdfField;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceMetamodel;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceFromList;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.MapServiceFromListService;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePPath;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.ResourceCache;
import org.aksw.jena_sparql_api.schema.ResourceExplorer;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.jena_sparql_api.utils.ModelUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.vaadin.datashape.form.ShaclForm;
import org.aksw.vaadin.datashape.provider.HierarchicalDataProviderForShacl;
import org.aksw.vaadin.datashape.provider.HierarchicalDataProviderForShacl.NodeState;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.PathWriter;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;





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
            model.setNsPrefixes(DefaultPrefixes.prefixes);
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


public class DatasetSelectorComponent extends PreconfiguredTabs {

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

        ShaclForm shaclForm = new ShaclForm();

        Model shaclModel = RDFDataMgr.loadModel("dcat-ap_2.0.0_shacl_shapes.ttl");
        NodeSchema schema = shaclModel.createResource("http://data.europa.eu/r5r#Dataset_Shape").as(NodeSchemaFromNodeShape.class);

        // Delete the dcat:distribution property shape

        Node datasetNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");
        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        ResourceCache resourceCache = new ResourceCache();
        SparqlQueryConnection conn = RDFConnectionFactory.connect(ds);
        ShapedNode sn = ShapedNode.create(datasetNode, schema, resourceCache, conn);
        LookupService<Node, ResourceMetamodel> metaDataService = ResourceExplorer.createMetamodelLookup(conn);

        Multimap<NodeSchema, Node> mm = HashMultimap.create();
        mm.put(schema, datasetNode);

        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
        dataFetcher.sync(mm, conn, metaDataService, resourceCache);

        ListService<Concept, ShapedNode> ls = new ListServiceFromList<>(Collections.singletonList(sn), (k, v) -> true);
        MapServiceFromListService<Concept, ShapedNode, Node, ShapedNode> ms = new MapServiceFromListService<>(ls, ShapedNode::getSourceNode, x -> x);


        Model prefixes = RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
        LookupService<Node, String> labelService =
                LabelUtils.createLookupServiceForLabels(LabelUtils.getLabelLookupService(conn, RDFS.label, prefixes), prefixes, prefixes).cache();


        TreeGrid<Path<Node>> treeGrid = new TreeGrid<>();
        GraphChange graphEditorModel = new GraphChange();


        // HierarchicalDataProvider<Path<Node>, String> dataProvider = new HierarchicalDataProviderForShacl(ms, graphEditorModel);

        HierarchicalDataProviderForShacl dataProvider = new HierarchicalDataProviderForShacl(ms, graphEditorModel);

        Set<Path<Node>> expandedPaths = Collections.synchronizedSet(new HashSet<>());
        treeGrid.addExpandListener(ev -> expandedPaths.addAll(ev.getItems()));
        treeGrid.addCollapseListener(ev -> expandedPaths.addAll(ev.getItems()));

        graphEditorModel.getAdditionGraph().asSet().addPropertyChangeListener(ev -> {
            CollectionChangedEvent<Triple> e = (CollectionChangedEvent<Triple>)ev;

            Set<Node> affectedNodes = Stream.concat(e.getAdditions().stream(), e.getDeletions().stream())
                    .flatMap(t -> Stream.of(t.getSubject(), t.getObject()))
                    .collect(Collectors.toSet());

            Set<Path<Node>> affectedPaths = expandedPaths.stream()
                    .filter(ep -> {
                        Node a = ep.getFileName().toSegment();
                        Node b = Optional.ofNullable(ep.getParent()).map(Path::getFileName).map(Path::toSegment).orElse(null);

                        return affectedNodes.contains(a) || affectedNodes.contains(b);
                    })
                    .collect(Collectors.toSet());

            affectedPaths.forEach(p -> dataProvider.refreshItem(p, true));
            // treeGrid.
            // paths.forEach(p -> dataProvider.refreshItem(p, true));
        });


//        HierarchicalDataProviderForShacl.NodeState.adept(graphEditorModel.getAdditionGraph(), nodes -> {
//            treeGrid.
//            // paths.forEach(p -> dataProvider.refreshItem(p, true));
//        });

//        graphEditorModel.getAdditionGraph().addPropertyChangeListener(ev -> {
//            dataProvider.refreshAll();
//        });
//
//        graphEditorModel.addPropertyChangeListener(ev -> {
//            dataProvider.refreshAll();
//        });


        treeGrid.setDataProvider(dataProvider);
//        treeGrid.setHeight("500px");
//        treeGrid.setWidth("1000px");
        treeGrid.setSizeFull();

        Column<?> hierarchyColumn;



        // hierarchyColumn = createViewerHierarchyColumn(labelService, treeGrid);
        hierarchyColumn = createEditorHierarchyColumn(graphEditorModel, treeGrid, dataProvider.getNodeState(), labelService);
        // hierarchyColumn.setWidth("100%");
        hierarchyColumn.setFlexGrow(1);
        hierarchyColumn.setResizable(true);
        hierarchyColumn.setFrozen(true);

        VerticalLayout v = new VerticalLayout();
        v.setSizeFull();
        v.add(treeGrid);
        treeGrid.setSelectionMode(SelectionMode.SINGLE);
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
        this.newTab("new-dataset", "New Dataset", new ManagedComponentSimple(shaclForm));



        VerticalLayout catalogMgmtPanel = new VerticalLayout();
        catalogMgmtPanel.setSizeFull();
        Button showAddCatalogBtn = new Button("Add Catalog");


        Dialog addCatalogDialog = new Dialog();


        ShaclForm addCatalogForm = new ShaclForm();
        addCatalogDialog.add(addCatalogForm);
        addCatalogDialog.setSizeFull();

        showAddCatalogBtn.addClickListener(event -> {
            addCatalogDialog.open();
//            input.focus();
        });

        catalogMgmtPanel.add(showAddCatalogBtn);


        this.newTab("manage-catalog", "Manage Catalogs", new ManagedComponentSimple(catalogMgmtPanel));
    }

    public Column<?> createEditorHierarchyColumn(
            GraphChange graphEditorModel,
            TreeGrid<Path<Node>> treeGrid,
            NodeState nodeState,
            LookupService<Node, String> labelService) {

        Column<?> hierarchyColumn;
        hierarchyColumn = treeGrid.addComponentHierarchyColumn(path -> {
            System.out.println("REFRESH");

            List<Node> segments = path.getSegments();
            int pathLength = path.getNameCount();

            boolean isResourcePath = pathLength % 2 == 1;
            boolean isPropertyPath = !isResourcePath;

            P_Path0 p0 = null;

            // System.out.println(path);
            // return "" + Optional.ofNullable(path).map(Path::getFileName).map(Object::toString).orElse("");
            Node node = path.getFileName().toSegment();
            String str = null;
            org.apache.jena.sparql.path.Path p = RDFDatatypePPath.extractPath(node);
            if (p != null) {
                if (p instanceof P_Path0) {
                    p0 = (P_Path0)p;
                    Node n = p0.getNode();
                    boolean isFwd = p0.isForward();
                    str = (isFwd ? "" : "^") + labelService.fetchItem(n);

                } else {
                    str = PathWriter.asString(p);
                }
            }

            if (str == null) {
                // r = LabelUtils.deriveLabelFromNode(node, null, null);
                str = labelService.fetchItem(node);
            }


            VerticalLayout r = new VerticalLayout();

            if (isResourcePath) {
                Node valueNode = path.getFileName().toSegment();

                if (valueNode.isURI()) {
                    Button editIriBtn = new Button(new Icon(VaadinIcon.EDIT));
                    editIriBtn.setThemeName("tertiary-inline");
                    r.add(editIriBtn);


                    // Create the area for changing the IRI
                    TextField nodeIdTextField = new TextField();
                    nodeIdTextField.setValueChangeMode(ValueChangeMode.LAZY);
                    nodeIdTextField.setPrefixComponent(new Span("IRI"));
                    //FormItem nodeIdFormItem = target.addFormItem(nodeIdTextField, "IRI");

                    // FIXME Do we have to pre-register a mapping for th node being edited in order for the system to work???
                    // graphEditorModel.getRenamedNodes().put(root, root);

                    Button resetNodeIdButton = new Button("Reset");
                    resetNodeIdButton.addClickListener(ev -> {
                        graphEditorModel.putRename(valueNode, valueNode);
                    });

                    nodeIdTextField.setSuffixComponent(resetNodeIdButton);

                    ShaclForm.bind(nodeIdTextField, graphEditorModel
                            .getRenamedNodes()
                            .observeKey(valueNode, valueNode)
                            .convert(NodeMappers.uriString.asConverter()));

                    editIriBtn.addClickListener(ev -> {
                        nodeIdTextField.setVisible(!nodeIdTextField.isVisible());
                    });


                    r.add(nodeIdTextField);
                    nodeIdTextField.setVisible(false);
                    // Fill up the row
                    //target.setColspan(resetNodeIdButton, 2);
                } else {
                    //target.setColspan(nodeSpan, maxCols);
                }

                // RdfField rdfField = graphEditorModel.createSetField(node, ps.getPredicate(), true);

//                ObservableCollection<Node> existingValues = rdfField.getBaseAsSet();
//                ObservableCollection<Node> addedValues = rdfField.getAddedAsSet();

                RdfTermEditor rdfTermEditor = new RdfTermEditor();
                rdfTermEditor.setWidthFull();
                // ShaclForm

                // RdfTermEditor.s
                // rdfTermEditor.setValue(valueNode);
                r.add(rdfTermEditor);


                if (pathLength >= 3) {
                    Node srcNode = pathLength < 3 ? null : segments.get(pathLength - 3);

                    Node pNode = segments.get(pathLength - 2);
                    p0 = (P_Path0)RDFDatatypePPath.extractPath(pNode);


                    RdfField rdfField = graphEditorModel.createSetField(srcNode, p0.getNode(), p0.isForward());
                    ObservableCollection<Node> addedValues = rdfField.getAddedAsSet();

                    if (addedValues.contains(valueNode)) {
                        Button btn = new Button("Remove");
                        r.add(btn);
                        btn.addClickListener(ev -> {
                            // Removing a newly created node also clears all information about it
                            // This comprises renames and manually added triples

                            // TODO Ask for confirmation in case data has been manually added

                            // newValueRenameRegistration.remove();

                            graphEditorModel.getRenamedNodes().remove(valueNode);
                            addedValues.remove(valueNode);
                        });

                    } else {
                        Triple t = TripleUtils.create(srcNode, p0.getNode(), valueNode, p0.isForward());


                        Button resetValueBtn = new Button(new Icon(VaadinIcon.ROTATE_LEFT));
                        resetValueBtn.getElement().setProperty("title", "Reset this field to its original value");

                        r.add(resetValueBtn);

                        Checkbox markAsDeleted = new Checkbox(false);
                        markAsDeleted.getElement().setProperty("title", "Mark the original value as deleted");
                        //target.addFormItem(markAsDeleted, "Delete");

                        r.add(markAsDeleted);

                        ObservableValue<Boolean> isDeleted = graphEditorModel.getDeletionGraph().asSet()
                                .filter(c -> c.equals(t))
                                .mapToValue(c -> !c.isEmpty(), b -> b ? null : t);


                        //newC.add(bind(markAsDeleted, isDeleted));


                        // Red background for resources marked as deleted
                        isDeleted.addValueChangeListener(ev -> {
                            if (Boolean.TRUE.equals(ev.getNewValue())) {
                                rdfTermEditor.setEnabled(false);
                                r.getStyle().set("background-color", "var(--lumo-error-color-50pct)");
                            } else {
                                rdfTermEditor.setEnabled(true);
                                r.getStyle().set("background-color", null);
                            }
                        });

                        markAsDeleted.addValueChangeListener(event -> {
                            Boolean state = event.getValue();
                            if (Boolean.TRUE.equals(state)) {
                                graphEditorModel.getDeletionGraph().add(t);
                            } else {
                                graphEditorModel.getDeletionGraph().delete(t);
                            }
                        });

                        // graphEditorModel.getDeletionGraph().tr

                        int component = p0.isForward() ? 2 : 0;
                        ObservableValue<Node> value = graphEditorModel.createFieldForExistingTriple(t, component);

                        Node originalValue = value.get();
                        resetValueBtn.setVisible(false);
                        value.addValueChangeListener(ev -> {
                            boolean newValueDiffersFromOriginal = !Objects.equals(originalValue, ev.getNewValue());

                            resetValueBtn.setVisible(newValueDiffersFromOriginal);
                            markAsDeleted.setVisible(!newValueDiffersFromOriginal);
                        });
                        ShaclForm.bind(rdfTermEditor, value);


                        resetValueBtn.addClickListener(ev -> {
                            value.set(originalValue);
                        });
                    }
                } else {
                    rdfTermEditor.setValue(valueNode);
                }

                // Show the filter / paginator controls
                HorizontalLayout filterPanel = new HorizontalLayout();
                TextField propertyFilter = new TextField();
                propertyFilter.setPlaceholder("Filter");
                propertyFilter.setValueChangeMode(ValueChangeMode.LAZY);

                ObservableValue<String> filterField = nodeState.getFilter(path);
                String filterValue = filterField.get();
                if (filterValue == null) {
                    filterField.set("");
                }
                boolean isFilterSet = !filterField.get().isBlank();

                ShaclForm.bind(propertyFilter, filterField);

                Select<Integer> itemsPerPage = new Select<>();
                itemsPerPage.setItems(5, 10, 25, 50, 100);

                filterPanel.add(propertyFilter, itemsPerPage);
                filterPanel.setVisible(treeGrid.isExpanded(path) || isFilterSet);
                r.add(new Hr());
                r.add(filterPanel);

                Registration exp = treeGrid.addExpandListener(ev -> {
                    if (ev.getItems().contains(path)) {
                        filterPanel.setVisible(true);
                    }
                });
                Registration col = treeGrid.addCollapseListener(ev -> {
                    if (ev.getItems().contains(path)) {
                        filterPanel.setVisible(false);
                    }
                });
//
//                r.addDetachListener(ev -> {
//
//                });
            } else if (isPropertyPath && p0 != null) {
                Node srcNode = segments.get(pathLength - 2);

                // Node propertyNode = p0.getNode();

                RdfField rdfField = graphEditorModel.createSetField(srcNode, p0.getNode(), p0.isForward());
                ObservableCollection<Node> addedValues = rdfField.getAddedAsSet();


                HorizontalLayout hl = new HorizontalLayout();
                hl.add(str);
                Button addValueButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE_O));
                addValueButton.addClassName("parent-hover-show");
                addValueButton.getElement().setProperty("title", "Add a new value to this property");
                addValueButton.setThemeName("tertiary-inline");
                hl.add(addValueButton);


                addValueButton.addClickListener(ev -> {
                    // Node newNode = NodeFactory.createBlankNode();
                    Node newNode = graphEditorModel.freshNode();
                    addedValues.add(newNode);

                    treeGrid.expand(Collections.singleton(path));
                });

                r.add(hl);

            } else {
                r.add(str);
            }

            return r;
            // return path.toString();
        });

//        treeGrid.addExpandListener(ev -> {
//        	ev.get
//        });

        return hierarchyColumn;
    }

    public Column<?> createViewerHierarchyColumn(LookupService<Node, String> labelService,
            TreeGrid<Path<Node>> treeGrid) {
        Column<?> hierarchyColumn;
        hierarchyColumn = treeGrid.addHierarchyColumn(path -> {
            // System.out.println(path);
            // return "" + Optional.ofNullable(path).map(Path::getFileName).map(Object::toString).orElse("");
            Node node = path.getFileName().toSegment();
            String r = null;
            if (node.isLiteral()) {
                Object o = node.getLiteralValue();
                if (o instanceof org.apache.jena.sparql.path.Path) {
                    org.apache.jena.sparql.path.Path p = (org.apache.jena.sparql.path.Path)o;

                    if (p instanceof P_Path0) {
                        P_Path0 p0 = (P_Path0)p;
                        Node n = p0.getNode();
                        boolean isFwd = p0.isForward();
                        r = (isFwd ? "" : "^") + labelService.fetchItem(n);

                    } else {
                        r = PathWriter.asString(p);
                    }
                }
            }

            if (r == null) {
                // r = LabelUtils.deriveLabelFromNode(node, null, null);
                r = labelService.fetchItem(node);
            }

            return r;
            // return path.toString();
        });
        return hierarchyColumn;
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

