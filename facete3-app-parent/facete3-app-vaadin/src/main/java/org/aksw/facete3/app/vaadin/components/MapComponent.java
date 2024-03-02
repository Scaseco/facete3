package org.aksw.facete3.app.vaadin.components;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import org.aksw.vaadin.jena.geo.leafletflow.JtsToLMapConverter;
import org.aksw.vaadin.jena.geo.leafletflow.ResultSetMapRendererL;
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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import com.google.common.collect.Streams;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import elemental.json.JsonObject;
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.basictypes.LIcon;
import software.xdev.vaadin.maps.leaflet.basictypes.LIconOptions;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.LLayerGroup;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;

public class MapComponent
    extends VerticalLayout
{
    protected FacetedBrowserView mainView;
    protected LMap leafletMap;
    protected JtsToLMapConverter converter;
    protected Envelope mapBounds;

    protected DataServiceBBoxCache<Node, Node> service;
    protected LLayerGroup group;

    protected LComponentManagementRegistry reg = new LDefaultComponentManagementRegistry(this);


    protected SimplePath path;

    protected Node spatialProperty;


    public MapComponent(FacetedBrowserView mainView) {
        this.mainView = mainView;

        spatialProperty = NodeFactory.createURI("http://www.w3.org/ns/locn#geometry");
        // spatialProperty = Geo.AS_WKT_NODE;
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


        MapContainer mapContainer = new MapContainer(reg);
        converter = new JtsToLMapConverter(reg) {
            @Override
            public LMarker convertPoint(org.locationtech.jts.geom.Point point) {
                LMarker r = super.convertPoint(point);
                r.setIcon(new LIcon(reg, new LIconOptions()
                        .withIconUrl("/marker-icon.png")));
                return r;
            };
        };

        mapContainer.setSizeFull();

        LMap map = mapContainer.getlMap();
        group = new LLayerGroup(reg);
        // Add a (default) TileLayer so that we can see something on the map
        map.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(reg));
        group.addTo(map);

        // Set what part of the world should be shown
        map.setView(new LLatLng(reg, 49.6751, 12.1607), 17);

        this.add(mapContainer);

        String moveUpdateFunction = """
                function updateBoundingBox() {
                    document.getElementById('%s').$server.updateBoundingBox(%s.getBounds());
                }
            """;
        this.setId("facete-dynId-" + UUID.randomUUID());
        map.on("moveend", String.format(moveUpdateFunction, this.getId().orElseThrow(), map.clientComponentJsAccessor()));

        // Create a new marker
//        new LMarker(reg, new LLatLng(reg, 49.6756, 12.1610))
//            // Bind a popup which is displayed when clicking the marker
//            .bindPopup("XDEV Software")
//            // Add it to the map
//            .addTo(map);

        // map.

//
//        MapOptions options = new DefaultMapOptions();
//        options.setCenter(new LatLng(47.070121823, 19.204101562500004));
//        options.setZoom(7);
//        leafletMap = new LeafletMap(options);
//        leafletMap.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
//
//        group = new FeatureGroup();
//        group.addTo(leafletMap);
//
//        leafletMap.addEventListener(DragEventType.dragend, ev -> {
//            refresh();
//        });
//
//        this.add(leafletMap);
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

//        leafletMap.getBounds().whenComplete((bounds, throwable) -> {
//            Envelope envelope = Leaflet4VaadinJtsUtils.convert(bounds);
//
//        });

        if (mapBounds != null) {
            LookupService<Node, Table> lookupService = new LookupServiceSparqlQuery(dataSource.asQef(), f.toQuery(), f.getSourceVar())
                    // .mapValues(t -> t.rows().next())
                    //.mapValues(b -> b.get(f.getTargetVar()))
                    //.mapValues(n -> GeometryWrapper.extract(n))
                    ;

            group.clearLayers();

            service.runWorkflow(mapBounds).forEach(cell -> {
                if (cell.isLoaded()) {
                    Set<Node> nodes = cell.getData();

                    Map<Node, Table> map = lookupService.fetchMap(nodes);
                    Collection<Binding> bindings = map.values().stream().flatMap(t -> Streams.stream(t.rows())).collect(Collectors.toList());
                    // Leaflet4VaadinUtils.addAndFly(leafletMap, group, bindings);
                    ResultSetMapRendererL.addBindingsToLayer(converter, group, bindings);
                } else {
                    Envelope cellBounds = cell.getBounds();
                    Node node = GeometryWrapperUtils.toWrapperWkt(cellBounds).asNode();
                    Binding cellBinding = BindingFactory.binding(Vars.x, node);
                    ResultSetMapRendererL.addBindingsToLayer(converter, group, cellBinding);
                }
                // System.out.println(map);
                // leafletMap.add();
//                for (Entry<Node, Table> e : map.entrySet()) {
//                    Leaflet4VaadinUtils.addAndFly(leafletMap, group, bindings);
//                }

                System.out.println(cell);
                System.out.println(cell.getData());
            });        }

        // Envelope bounds = new Envelope(1, 65, 1, 75);
//        MapPaginator<Node, Node> paginator = mapService.createPaginator(bounds);
//        System.out.println("Count: " + paginator.fetchCount(null, null).blockingGet());
//        Map<?, ?> map = paginator.fetchMap();
//        for (Object item : map.keySet()) {
//            System.out.println(item);
//        }
//        System.out.println("Item count: " + map.keySet().size());


    }


    @ClientCallable
    public void updateBoundingBox(JsonObject json) {
        // {"_southWest":{"lat":49.67390472450039,"lng":12.158346176147463},"_northEast":{"lat":49.67648051949675,"lng":12.165791988372803}}
        Coordinate sw = parse(json.get("_southWest"));
        Coordinate ne = parse(json.get("_northEast"));

        this.mapBounds = new Envelope(sw, ne);
        refresh();
        // System.out.println(json);
        // this.boundingBox = parseBoundingBoxJson(json.toJson());
    }

    public static Coordinate parse(JsonObject json) {
        double lat = json.getNumber("lat");
        double lng = json.getNumber("lng");
        return new Coordinate(lng, lat);
    }
}
