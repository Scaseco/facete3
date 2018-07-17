package org.hobbit.faceted_browsing.action;

import java.math.BigDecimal;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;

public class TestFacetedBrowsingActions {
	public static void main(String[] args) {
		JenaSystem.init();
		JenaPluginFacetedBrowsing.init();
		
		Model m = ModelFactory.createDefaultModel();
		
		Viewport v = m.createResource().as(Viewport.class);
		v.setWidth(BigDecimal.valueOf(1920));
		v.setHeight(BigDecimal.valueOf(1080));
		v.setDataCrs("EPSG:4326");
		v.setDisplayCrs("EPSG:900913");
		RDFDataMgr.write(System.out, v.getModel(), RDFFormat.TURTLE_PRETTY);

		
		MapState s = m.createResource().as(MapState.class);
		
		s.setCenterX(new BigDecimal("1.234"));
		s.setCenterY(new BigDecimal("5.678"));
		s.setZoom(BigDecimal.valueOf(5));
		
		RDFDataMgr.write(System.out, s.getModel(), RDFFormat.TURTLE_PRETTY);
	}
}
