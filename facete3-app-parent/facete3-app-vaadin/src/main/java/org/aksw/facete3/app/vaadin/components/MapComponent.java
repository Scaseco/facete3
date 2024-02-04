package org.aksw.facete3.app.vaadin.components;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.LookupServiceSparqlQuery;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.DataServiceBBoxCache;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.GeoConstraintFactory;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.GeoConstraintFactoryGeoSparql;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.GeometryWrapperUtils;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.MapServiceBBox;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinderSystemBasic;
import org.aksw.jenax.arq.util.node.PathUtils;
import org.aksw.jenax.arq.util.node.ReverseRenameUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.aksw.jenax.sparql.path.SimplePath;
import org.aksw.vaadin.common.component.util.NotificationUtils;
import org.aksw.vaadin.jena.geo.Leaflet4VaadinJtsUtils;
import org.aksw.vaadin.jena.geo.Leaflet4VaadinUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.locationtech.jts.geom.Envelope;

import com.google.common.collect.Streams;
import com.vaadin.addon.leaflet4vaadin.LeafletMap;
import com.vaadin.addon.leaflet4vaadin.layer.events.types.DragEventType;
import com.vaadin.addon.leaflet4vaadin.layer.groups.FeatureGroup;
import com.vaadin.addon.leaflet4vaadin.layer.groups.LayerGroup;
import com.vaadin.addon.leaflet4vaadin.layer.map.options.DefaultMapOptions;
import com.vaadin.addon.leaflet4vaadin.layer.map.options.MapOptions;
import com.vaadin.addon.leaflet4vaadin.types.LatLng;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class MapComponent
    extends VerticalLayout
{
    protected FacetedBrowserView mainView;
    protected LeafletMap leafletMap;
    protected DataServiceBBoxCache<Node, Node> service;
    protected LayerGroup group;

    protected SimplePath path;

    protected Node spatialProperty;

    public MapComponent(FacetedBrowserView mainView) {
        this.mainView = mainView;

        spatialProperty = NodeFactory.createURI("http://www.w3.org/ns/locn#geometry");
        setSizeFull();
//		// Create the registry which is needed so that components can be reused and their methods invoked
//		// Note: You normally don't need to invoke any methods of the registry and just hand it over to the components
//		final LComponentManagementRegistry reg = new LDefaultComponentManagementRegistry(this);
//
//		// Create and add the MapContainer (which contains the map) to the UI
//		final MapContainer mapContainer = new MapContainer(reg);
//		mapContainer.setSizeFull();
//		this.add(mapContainer);
//
//		final LMap map = mapContainer.getlMap();
//
//		// Add a (default) TileLayer so that we can see something on the map
//		map.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(reg));
//
//		// Set what part of the world should be shown
//		map.setView(new LLatLng(reg, 49.6751, 12.1607), 17);
//
//		// Create a new marker
//		new LMarker(reg, new LLatLng(reg, 49.6756, 12.1610))
//			// Bind a popup which is displayed when clicking the marker
//			.bindPopup("XDEV Software")
//			// Add it to the map
//			.addTo(map);

        ComboBox<SimplePath> geoPathComboBox = new ComboBox<>();
        geoPathComboBox.setRenderer(new ComponentRenderer<>(path -> new Span("" + path)));
        geoPathComboBox.setWidthFull();

        Button searchForGeoPathsBtn = new Button("Search spatial relations");
        geoPathComboBox.addValueChangeListener(ev -> {
            path = ev.getValue();
            refresh();
        });

        searchForGeoPathsBtn.addClickListener(ev -> {
            RdfDataSource dataSource = mainView.getFacetedSearchSession().getFacetedQuery().dataSource();

            ConceptPathFinderSystem sys = new ConceptPathFinderSystemBasic();
            Model dataSummary = sys.computeDataSummary(dataSource).blockingGet();
            ConceptPathFinder pathFinder = sys.newPathFinderBuilder()
                .setDataSummary(dataSummary)
                .setDataSource(dataSource)
                .build();

            Fragment1 sourceConcept = mainView.getFacetedSearchSession().getFacetedQuery().root().availableValues().baseRelation().toFragment1();
            Fragment2 f = GeoConstraintFactoryGeoSparql.create(spatialProperty).getFragment();
            // Fragment2 f = GeoConstraintFactoryGeoSparql.create("http://www.w3.org/ns/locn#geometry").getFragment();
            Fragment1 targetConcept = f.project(f.getSourceVar()).toFragment1();

            List<SimplePath> paths = pathFinder.createSearch(sourceConcept, targetConcept).exec().toList().blockingGet();

            NotificationUtils.success("Found " + paths.size() + " paths");
            // System.out.println("GeoPaths:");
            geoPathComboBox.setItems(paths);
            // geoPathComboBox.setItems();

//            for (SimplePath path : paths) {
//                System.out.println("Path: " + path);
//            }
        });


        this.add(searchForGeoPathsBtn);;


        this.add(geoPathComboBox);




        MapOptions options = new DefaultMapOptions();
        options.setCenter(new LatLng(47.070121823, 19.204101562500004));
        options.setZoom(7);
        leafletMap = new LeafletMap(options);
        leafletMap.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");

        group = new FeatureGroup();
        group.addTo(leafletMap);

        leafletMap.addEventListener(DragEventType.dragend, ev -> {
            refresh();
        });

        this.add(leafletMap);
      // tabs.newTab("map", "Map", new ManagedComponentSimple(leafletMap));
    }


    /** Update the map */
    public void refresh() {

        Fragment1 concept = mainView.getFacetedSearchSession().getFacetedQuery().root().availableValues().baseRelation().toFragment1();

        RdfDataSource dataSource = mainView.getFacetedSearchSession().getFacetedQuery().dataSource();

        Path pp = SimplePath.toPropertyPath(path);
        Path fullPath = PathFactory.pathSeq(pp, PathFactory.pathLink(spatialProperty));

        // Create a binary relation from the source concept to the spatial data
        TriplePath triplePath = new TriplePath(Vars.s, fullPath, Vars.o);

        BasicPattern bgp = PathUtils.flattenOrNull(triplePath);
        // bgp = NodeTransformLib.transform(ReverseRenameUtils::effectiveNode, bgp);

        // Turn blank node vars into ordinary vars
        // (Real blank nodes conflict with union default graph rewriting because it results in
        // GRAPH ?v1 { _:b0 ...}) GRAPH ?v2 { :_b0 ... } # Error because same bnode id used in different scopes
        // Note, this could be resolved at a different layer but it seems easier to handle it here where
        // the issue occurs
        bgp = NodeTransformLib.transform(n -> Var.isBlankNodeVar(n)
                ? Var.alloc("__" + ReverseRenameUtils.plainVarName(n))
                : n , bgp);

        // Fragment2 relation = Fragment2Impl.create(fullPath);
        Fragment2 relation = new Fragment2Impl(new ElementTriplesBlock(bgp), (Var)triplePath.getSubject(), (Var)triplePath.getObject());
        GeoConstraintFactory gcf = GeoConstraintFactoryGeoSparql.of(relation);


        // GeoConstraintFactory gcf = GeoConstraintFactoryWgs.of("https://schema.coypu.org/global#hasLatitude", "https://schema.coypu.org/global#hasLongitude", XSD.xfloat.getURI());
        MapService<Envelope, Node, Node> mapService = new MapServiceBBox(dataSource, concept, gcf);
        service = new DataServiceBBoxCache<>(mapService, 1000, 100, 2);

        Fragment2 f = gcf.getFragment().toFragment2();

        LookupService<Node, Table> lookupService = new LookupServiceSparqlQuery(dataSource.asQef(), f.toQuery(), f.getSourceVar())
                // .mapValues(t -> t.rows().next())
                //.mapValues(b -> b.get(f.getTargetVar()))
                //.mapValues(n -> GeometryWrapper.extract(n))
                ;

        group.clearLayers();

        leafletMap.getBounds().whenComplete((bounds, throwable) -> {
            Envelope envelope = Leaflet4VaadinJtsUtils.convert(bounds);
            service.runWorkflow(envelope).forEach(cell -> {
                if (cell.isLoaded()) {
                    Set<Node> nodes = cell.getData();

                    Map<Node, Table> map = lookupService.fetchMap(nodes);
                    Collection<Binding> bindings = map.values().stream().flatMap(t -> Streams.stream(t.rows())).collect(Collectors.toList());
                    // Leaflet4VaadinUtils.addAndFly(leafletMap, group, bindings);
                    Leaflet4VaadinUtils.addBindingsToLayer(leafletMap, group, bindings);
                } else {
                    Envelope cellBounds = cell.getBounds();
                    Node node = GeometryWrapperUtils.toWrapperWkt(cellBounds).asNode();
                    Binding cellBinding = BindingFactory.binding(Vars.x, node);
                    Leaflet4VaadinUtils.addBindingsToLayer(leafletMap, group, cellBinding);
                }
                // System.out.println(map);
                // leafletMap.add();
//                for (Entry<Node, Table> e : map.entrySet()) {
//                    Leaflet4VaadinUtils.addAndFly(leafletMap, group, bindings);
//                }

                System.out.println(cell);
                System.out.println(cell.getData());
            });

        });
        // Envelope bounds = new Envelope(1, 65, 1, 75);
//        MapPaginator<Node, Node> paginator = mapService.createPaginator(bounds);
//        System.out.println("Count: " + paginator.fetchCount(null, null).blockingGet());
//        Map<?, ?> map = paginator.fetchMap();
//        for (Object item : map.keySet()) {
//            System.out.println(item);
//        }
//        System.out.println("Item count: " + map.keySet().size());


    }
}
