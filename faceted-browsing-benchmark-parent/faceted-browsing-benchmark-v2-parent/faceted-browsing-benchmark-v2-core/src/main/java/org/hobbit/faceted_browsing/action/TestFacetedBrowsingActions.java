package org.hobbit.faceted_browsing.action;

import java.math.BigDecimal;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

public class TestFacetedBrowsingActions {
	public static void main(String[] args) throws MismatchedDimensionException, NoSuchAuthorityCodeException, FactoryException, TransformException {
		JenaSystem.init();
		JenaPluginFacetedBrowsing.init();
		
		Model m = ModelFactory.createDefaultModel();
		
		Viewport v = m.createResource().as(Viewport.class);
		v.setWidth(BigDecimal.valueOf(1920));
		v.setHeight(BigDecimal.valueOf(1080));
		v.setDataCrs("EPSG:4326");
		v.setDisplayCrs("EPSG:900913");
		RDFDataMgr.write(System.out, v.getModel(), RDFFormat.TURTLE_PRETTY);

		
//		MapState s = m.createResource().as(MapState.class);
		
		v.setCenterX(new BigDecimal("1.234"));
		v.setCenterY(new BigDecimal("5.678"));
//		s.setZoom(BigDecimal.valueOf(5));
		
//		RDFDataMgr.write(System.out, s.getModel(), RDFFormat.TURTLE_PRETTY);
		
		Viewport.transform(v);
	}
}
