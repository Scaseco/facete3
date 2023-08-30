package org.aksw.facete3.app.vaadin.components;

import java.io.InputStream;
import java.util.function.Supplier;

import org.aksw.commons.util.io.in.InputStreamUtils;
import org.aksw.facete.v4.impl.TreeDataUtils;
import org.aksw.facete3.app.vaadin.plugin.view.ViewManager;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderNodeQuery;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataRetriever;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.vaadin.component.grid.shacl.VaadinShaclGridUtils;
import org.aksw.jenax.vaadin.component.grid.sparql.TableMapperComponent;
import org.aksw.jenax.vaadin.component.grid.sparql.TableMapperState;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.vaadin.common.component.tab.TabSheet;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.engine.binding.Binding;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.Style;

/**
 * The view component for values matching the given facet constraints
 *
 * @author raven
 */
public class ItemComponent extends TabSheet {
    private static final long serialVersionUID = 1848553144669545835L;

    protected DataProviderNodeQuery dataProvider;
    protected LabelService<Node, String> labelService;

    protected ViewManager viewManager;
    protected TableContext tableContext;

    protected TextField searchField = new TextField();
    protected FacetedBrowserView facetedBrowserView;
    protected Grid<RDFNode> grid = new Grid<>(RDFNode.class);

    protected VerticalLayout tableDiv = new VerticalLayout();


    protected TableMapperState tableMapperState = TableMapperState.ofRoot();


    /** Refresh the grid, especially updating the columns. Also, a cache is used to remember components in cells. */
    public void refreshGrid() {
//        DataProvider<EnrichedItem, Void> effectiveDataProvider = DataProviderWithConversion.wrapWithBulkConvert(
//                itemProvider, this::enrich, ei -> (RDFNode)ei.getItem());


        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);


        grid.removeAllColumns();
        VaadinShaclGridUtils.configureGrid(grid, dataProvider, labelService);
        // DataProviderUtils.wrapWithErrorHandler(grid);


        // grid.getClassNames().add("compact");
//        grid.getColumns()
//                .forEach(grid::removeColumn);
       // grid.addColumn(new ComponentRenderer<>(item -> {
       // 	Anchor anchor = new Anchor();
       // 	anchor.setText(FacetProvider.getLabel(item));
            //anchor.setHref(item.asResource().getProperty(linkProperty).getString());
       // 	anchor.setHref("");
       // 	return anchor;
       // 	})).setSortProperty("value").setHeader(searchField);
//        Column<?> col = grid.addColumn(
//                new ComponentRenderer<Component, EnrichedItem>(enrichedItem -> {
//                    RDFNode item = (RDFNode)enrichedItem.getItem();
//                    Node node = item.asNode();
////                    Component r = viewManager.getComponents(Collections.singleton(node)).get(node);
//                    Component r = (Component)enrichedItem.getClassToInstanceMap().getInstance(Component.class);
//                    if (r == null) {
//                        String str = LabelUtils.getOrDeriveLabel(item);
//                        r = new Span(str);
//                    }
//
//                    HorizontalLayout card = new HorizontalLayout();
//                    card.setWidthFull();
//                    card.addClassName("card");
//                    card.setSpacing(false);
//                    card.getThemeList().add("spacing-s");
//                    card.add(r);
//
//                    return card;
//                // item -> FacetProvider.getLabel(item)
//                //item -> LabelUtils.getOrDeriveLabel(item)
//                }))
//                .setSortProperty("value")
//                .setHeader("Items");

        HeaderRow filterRow = grid.appendHeaderRow();
        // filterRow.getCell(col).setComponent(searchField);



        grid.setDataProvider(DataProviderUtils.wrapWithErrorHandler(dataProvider));
        grid.asSingleSelect()
                .addValueChangeListener(event -> {
//                    Node node = event.getValue().asNode();
                    Node node = ((RDFNode)event.getValue()).asNode();
                    facetedBrowserView.viewNode(node);
                });

    }

    public ItemComponent(
            FacetedBrowserView facetedBrowserView,
            DataProviderNodeQuery dataProvider,
            ViewManager viewManager,
            LabelService<Node, String> labelService) {

        this.facetedBrowserView = facetedBrowserView;
        this.dataProvider = dataProvider;
        this.viewManager = viewManager;
        this.labelService = labelService;

        Button configureShaclBtn = new Button(VaadinIcon.COG.create()); //"Available columns");
        configureShaclBtn.addClickListener(ev -> {
            showShaclUploadDialog();
        });


        Button configureTableBtn = new Button(VaadinIcon.COG.create()); //"Available columns");
        configureTableBtn.addClickListener(ev -> {
            showTableMapperDialog();
        });
//
//        btn.addClickListener(event -> {
//            List<RDFNode> nodes = facetedBrowserView.getFacetedSearchSession().getFacetedQuery()
//                    .focus()
//                    .availableValues()
//                    .toFacetedQuery()
//                    .focus().fwd().facetCounts()
//                    .exec()
//                    .map(x -> (RDFNode)x)
//                    .toList()
//                    .blockingGet();
//
//            MultiSelectListBox<RDFNode> lb = new MultiSelectListBox<>();
//            lb.setItems(nodes);
//
//            Button acceptBtn = new Button("Accept");
//
//
//            acceptBtn.addClickListener(ev -> {
//                // Refresh the table with the current selection of columns
//                // Note that this may have to recursively refresh all child tables
//
//
//            });
//
//            Dialog dialog = new Dialog();
//            dialog.add(lb);
//            dialog.add(acceptBtn);
//
//
//            dialog.add(new Text("Close me with the esc-key or an outside click"));
//            dialog.open();
//        });
        Style tableSettingsStyle = configureTableBtn.getStyle();
        tableSettingsStyle.set("position", "absolute");
        tableSettingsStyle.set("top", "0");
        tableSettingsStyle.set("right", "0");

        // add(btn);


        // add(new Label("Items"));
        searchField.setPlaceholder("Filter Items...");
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            // dataProvider.setFilter(filter);
        });

        VerticalLayout gridDiv = new VerticalLayout();
        gridDiv.setSizeFull();
        gridDiv.getStyle().set("position", "relative");
        gridDiv.add(configureShaclBtn);

        // Grid<EnrichedItem> grid = new Grid<>(EnrichedItem.class);

        refreshGrid();

        // add(grid);
        gridDiv.add(grid);
        // gridDiv.add(btn);

        add(VaadinIcon.LIST.create(), gridDiv);

        VerticalLayout tableDivX = new VerticalLayout();
        tableDivX.setSizeFull();
        // VerticalLayout tableDivX.setSizeFull();
        tableDivX.getStyle().set("position", "relative");
        tableDiv.setSizeFull();
        tableDivX.add(tableDiv, configureTableBtn);

        // tableDiv.setText("Table");
        add(VaadinIcon.TABLE.create(), tableDivX);

        // add(gridDiv);
    }

    public void resetTable() {

    }

    public void showTableMapperDialog() {
        RdfDataSource dataSource = dataProvider.getDataSource();
        Supplier<UnaryRelation> conceptSupplier = dataProvider.getConceptSupplier();
        System.out.println("Concept: " + conceptSupplier.get());
        TableMapperComponent tmc = new TableMapperComponent(dataSource, conceptSupplier.get(), labelService);

        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setSizeFull();
        // dialog.add(lb);
        dialog.add(tmc);


        // dialog.add(new Text("Close me with the esc-key or an outside click"));
        Button acceptBtn = new Button("OK");
        Button cancelBtn = new Button("Cancel");

        acceptBtn.addClickListener(ev -> {
            dialog.close();
            tableMapperState = tmc.getState();
            refreshTable();

        });
        cancelBtn.addClickListener(ev -> dialog.close());

        dialog.add(acceptBtn);
        dialog.add(cancelBtn);

        dialog.open();
    }


    public void refreshTable() {
        RdfDataSource dataSource = dataProvider.getDataSource();
        Supplier<UnaryRelation> conceptSupplier = dataProvider.getConceptSupplier();
        Grid<Binding> table = TableMapperComponent.buildGrid(
                dataSource, conceptSupplier.get(),
                TreeDataUtils.toVaadin(tableMapperState.getFacetTree()),
                TableMapperComponent.toPredicateAbsentAsTrue(tableMapperState.getPathToVisibility()),
                labelService);
        DataProviderUtils.wrapWithErrorHandler(table);
        tableDiv.removeAll();
        tableDiv.add(table);
    }

    public void showShaclUploadDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        // dialog.setSizeFull();

        TextField message = new TextField();
        message.setReadOnly(true);

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        Upload singleFileUpload = new Upload(memoryBuffer);

        Button acceptBtn = new Button("OK");
        Model shaclModel = ModelFactory.createDefaultModel();

        acceptBtn.addClickListener(event -> {
            dialog.close();
            RdfDataSource dataSource = dataProvider.getDataSource();
            DataRetriever dataRetriever = VaadinShaclGridUtils.setupRetriever(dataSource, shaclModel);
            dataProvider.setDataRetriever(dataRetriever);
            System.out.println("TABLE REFRESH");
            dataProvider.refreshAll();
            // Do something with the file data
            // processFile(fileData, fileName, contentLength, mimeType);
        });

        acceptBtn.setEnabled(false);

        singleFileUpload.addStartedListener(evenet -> {
            message.setValue("");
            acceptBtn.setEnabled(false);
        });

        singleFileUpload.addSucceededListener(event -> {
            try {
                // Get information about the uploaded file
                InputStream fileData = memoryBuffer.getInputStream();
                InputStream in = InputStreamUtils.forceMarkSupport(fileData);
                // String fileName = event.getFileName();
                // long contentLength = event.getContentLength();
                // String mimeType = event.getMIMEType();

                shaclModel.removeAll();
                try (TypedInputStream tin = RDFDataMgrEx.probeLang(in, RDFDataMgrEx.DEFAULT_PROBE_LANGS)) {
                    Lang lang = RDFLanguages.contentTypeToLang(tin.getContentType());
                    RDFDataMgr.read(shaclModel, tin, lang);
                }
                message.setValue("OK");
                acceptBtn.setEnabled(true);
            } catch (Exception e) {
                message.setValue(e.toString());
            }
        });



        // dialog.add(lb);
        // dialog.add(tmc);

        dialog.add(singleFileUpload);
        dialog.add(message);

        // dialog.add(new Text("Close me with the esc-key or an outside click"));
        Button cancelBtn = new Button("Cancel");

        cancelBtn.addClickListener(ev -> dialog.close());

        dialog.add(acceptBtn);
        dialog.add(cancelBtn);


        dialog.open();
    }


    public void refresh() {
        dataProvider.refreshAll();
    }
}
