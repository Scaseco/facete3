package org.aksw.facete3.app.vaadin.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.CollectionChangedEvent;
import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.path.core.Path;
import org.aksw.facete3.app.vaadin.components.rdf.editor.RdfTermEditor;
import org.aksw.jena_sparql_api.collection.GraphChange;
import org.aksw.jena_sparql_api.collection.RdfField;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceFromList;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.MapServiceFromListService;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePPath;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.vaadin.datashape.form.ShaclForm;
import org.aksw.vaadin.datashape.provider.HierarchicalDataProviderForShacl;
import org.aksw.vaadin.datashape.provider.HierarchicalDataProviderForShacl.NodeState;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.PathWriter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;

public class ShaclForm2 {
    public static TreeGrid<Path<Node>> createShaclEditor(
            GraphChange graphEditorModel,
            List<ShapedNode> rootNodes,
            LookupService<Node, String> labelService) {
        ListService<Concept, ShapedNode> ls = new ListServiceFromList<>(rootNodes, (k, v) -> true);
        MapServiceFromListService<Concept, ShapedNode, Node, ShapedNode> ms = new MapServiceFromListService<>(ls, ShapedNode::getSourceNode, x -> x);


        TreeGrid<Path<Node>> treeGrid = new TreeGrid<>();

        Set<Path<Node>> expandedPaths = Collections.synchronizedSet(new HashSet<>());
        treeGrid.addExpandListener(ev -> expandedPaths.addAll(ev.getItems()));
        treeGrid.addCollapseListener(ev -> expandedPaths.addAll(ev.getItems()));


        GridContextMenu<Path<Node>> contextMenu = treeGrid.addContextMenu();
        contextMenu.setDynamicContentHandler(path -> {
            contextMenu.removeAll();

            Collection<Path<Node>> paths = path != null ? Collections.singleton(path) : Collections.emptySet();

            if (!paths.isEmpty()) {
                contextMenu.addItem(path.toString()).setEnabled(false);
                contextMenu.addItem(new Hr());
                contextMenu.addItem("Collapse Tree", ev2 -> treeGrid.collapseRecursively(paths, Integer.MAX_VALUE));
                contextMenu.addItem("Expand 3 Levels", ev2 -> treeGrid.expandRecursively(paths, 3));
            }
            return true;
        });


        HierarchicalDataProviderForShacl dataProvider = new HierarchicalDataProviderForShacl(ms, graphEditorModel);


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

        Column<?> hierarchyColumn;



        // hierarchyColumn = createViewerHierarchyColumn(labelService, treeGrid);
        treeGrid.setSizeFull();
        hierarchyColumn = createEditorHierarchyColumn(graphEditorModel, treeGrid, dataProvider.getNodeState(), labelService);
        // hierarchyColumn.setWidth("100%");
        // hierarchyColumn.setFlexGrow(1);
        hierarchyColumn.setWidth("100%");

        return treeGrid;
    }



    public static Column<?> createEditorHierarchyColumn(
            GraphChange graphEditorModel,
            TreeGrid<Path<Node>> treeGrid,
            NodeState nodeState,
            LookupService<Node, String> labelService) {



        Column<?> hierarchyColumn;

        ValueProvider<Path<Node>, Component> componentProvider = path -> {
//            System.out.println("REFRESH");


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
            r.setWidthFull();

            List<Component> renameComponents = new ArrayList<>();
            List<Component> suffixComponents = new ArrayList<>();


            if (isResourcePath) {
                Node valueNode = path.getFileName().toSegment();

                if (valueNode.isURI()) {
                    Button editIriBtn = new Button(new Icon(VaadinIcon.EDIT));
                    editIriBtn.addClassName("parent-hover-show");
                    editIriBtn.setThemeName("tertiary-inline");
                    renameComponents.add(editIriBtn);


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

                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();

                r.add(row);

                RdfTermEditor rdfTermEditor = new RdfTermEditor();
                rdfTermEditor.setWidthFull();

                // ShaclForm
                // RdfTermEditor.s
                // rdfTermEditor.setValue(valueNode);


                if (pathLength >= 3) {
                    Node srcNode = pathLength < 3 ? null : segments.get(pathLength - 3);

                    Node pNode = segments.get(pathLength - 2);
                    p0 = (P_Path0)RDFDatatypePPath.extractPath(pNode);


                    RdfField rdfField = graphEditorModel.createSetField(srcNode, p0.getNode(), p0.isForward());
                    ObservableCollection<Node> addedValues = rdfField.getAddedAsSet();

                    if (addedValues.contains(valueNode)) {
                        Button btn = new Button(new Icon(VaadinIcon.TRASH));
                        btn.addClassName("parent-hover-show");
                        btn.setThemeName("tertiary-inline");

                        // r.add(btn);
                        suffixComponents.add(btn);
                        btn.addClickListener(ev -> {
                            // Removing a newly created node also clears all information about it
                            // This comprises renames and manually added triples

                            // TODO Ask for confirmation in case data has been manually added

                            // newValueRenameRegistration.remove();

                            addedValues.remove(valueNode);
                            graphEditorModel.getRenamedNodes().remove(valueNode);
                        });
                        Triple t = TripleUtils.create(srcNode, p0.getNode(), valueNode, p0.isForward());
                        int component = p0.isForward() ? 2 : 0;
                        //ObservableValue<Node> value = graphEditorModel.createFieldForExistingTriple(t, component);
                        ObservableValue<Node> remapped = graphEditorModel.getRenamedNodes().observeKey(valueNode, valueNode);

                        // Registration newValueRenameRegistration = bind(ed, remapped);

                        ShaclForm.bind(rdfTermEditor, remapped);

                    } else {
                        Triple t = TripleUtils.create(srcNode, p0.getNode(), valueNode, p0.isForward());
                        int component = p0.isForward() ? 2 : 0;
                        ObservableValue<Node> value = graphEditorModel.createFieldForExistingTriple(t, component);


                        Button resetValueBtn = new Button(new Icon(VaadinIcon.ROTATE_LEFT));
                        resetValueBtn.getElement().setProperty("title", "Reset this field to its original value");

                        suffixComponents.add(resetValueBtn);

                        Checkbox markAsDeleted = new Checkbox(false);
                        markAsDeleted.getElement().setProperty("title", "Mark the original value as deleted");
                        //target.addFormItem(markAsDeleted, "Delete");

                        suffixComponents.add(markAsDeleted);

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
//                Button showFilterPanelBtn = new Button(new Icon(VaadinIcon.FILTER));
//                showFilterPanelBtn.addClassName("parent-hover-show");
//                showFilterPanelBtn.getElement().setProperty("title", "Show filter options for the children of this item");
//                showFilterPanelBtn.setThemeName("tertiary-inline");
//
//                row.add(showFilterPanelBtn);

                row.add(rdfTermEditor);
                row.setFlexGrow(1, rdfTermEditor);


                row.add(suffixComponents.toArray(new Component[0]));
                row.add(renameComponents.toArray(new Component[0]));

                HorizontalLayout filterPanel = new HorizontalLayout();
                TextField propertyFilter = new TextField();
                propertyFilter.setPlaceholder("Filter");
                propertyFilter.setValueChangeMode(ValueChangeMode.LAZY);
                // propertyFilter.setId(path.toString() + " filter");

                ObservableValue<String> filterField = nodeState.getFilter(path);
//                String filterValue = filterField.get();
//                if (filterValue == null) {
//                    filterField.set("");
//                }
                boolean isFilterSet = !filterField.get().isBlank();

                ShaclForm.bind(propertyFilter, filterField);

                Select<Integer> itemsPerPage = new Select<>();
                itemsPerPage.setItems(5, 10, 25, 50, 100);

                filterPanel.add(propertyFilter, itemsPerPage);
                filterPanel.setVisible(treeGrid.isExpanded(path) || isFilterSet);
                r.add(filterPanel);

//                showFilterPanelBtn.addClickListener(ev -> {
//                    filterPanel.setVisible(!filterPanel.isVisible());
//                });


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
        };


        Map<Path<Node>, Component> map = new ConcurrentHashMap<>();

        hierarchyColumn = treeGrid.addComponentHierarchyColumn(
                path -> {
                    Component r = map.computeIfAbsent(path, p -> componentProvider.apply(p));
                    return r;
                });

//        treeGrid.addExpandListener(ev -> {
//        	ev.get
//        });

        return hierarchyColumn;
    }

    public static Column<?> createViewerHierarchyColumn(LookupService<Node, String> labelService,
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
