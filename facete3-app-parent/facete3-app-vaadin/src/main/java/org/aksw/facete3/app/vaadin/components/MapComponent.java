package org.aksw.facete3.app.vaadin.components;

import com.vaadin.addon.leaflet4vaadin.LeafletMap;
import com.vaadin.addon.leaflet4vaadin.layer.map.options.DefaultMapOptions;
import com.vaadin.addon.leaflet4vaadin.layer.map.options.MapOptions;
import com.vaadin.addon.leaflet4vaadin.types.LatLng;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MapComponent
    extends VerticalLayout
{
    public MapComponent() {
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

      MapOptions options = new DefaultMapOptions();
      options.setCenter(new LatLng(47.070121823, 19.204101562500004));
      options.setZoom(7);
      LeafletMap leafletMap = new LeafletMap(options);
      leafletMap.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");

      this.add(leafletMap);
      // tabs.newTab("map", "Map", new ManagedComponentSimple(leafletMap));


    }
}
