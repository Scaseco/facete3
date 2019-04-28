package org.hobbit.faceted_browsing.action;

import java.awt.geom.AffineTransform;
import java.math.BigDecimal;

import org.apache.jena.rdf.model.Resource;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public interface Viewport
	extends Resource
{
	BigDecimal getWidth();
	void setWidth(BigDecimal width);
	
	BigDecimal getHeight();
	void setHeight(BigDecimal height);
	
	String getDataCrs();
	void setDataCrs(String crs);
	
	String getDisplayCrs();
	void setDisplayCrs(String crs);

	BigDecimal getCenterX();
	void setCenterX(BigDecimal val);
	
	BigDecimal getCenterY();
	void setCenterY(BigDecimal val);
	
	BigDecimal getZoom();
	void setZoom(BigDecimal zoom);

	
	public static MathTransform getTransformDataToScreen(Viewport viewport) {
		MathTransform result;

		try {
		    MathTransformFactory mathTransformFactory = new DefaultMathTransformFactory();
		    
			CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
			CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:900913");
	
			MathTransform dataToWorld = CRS.findMathTransform(sourceCRS, targetCRS);
	
			AffineTransform worldToScreenBuilder = new AffineTransform();
			
			double scx = 0.5 * viewport.getWidth().doubleValue();
			double scy = 0.5 * viewport.getHeight().doubleValue();
			worldToScreenBuilder.translate(scx, scy);
			worldToScreenBuilder.scale( 1.0, -1.0 ); // flip to match screen
			
			MathTransform worldToScreen = new AffineTransform2D(worldToScreenBuilder);
			
			result = mathTransformFactory.createConcatenatedTransform(dataToWorld, worldToScreen);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}
	
	// http://docs.geotools.org/latest/userguide/library/referencing/axis.html
	public static void transform(Viewport viewport) throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {
		
	    GeometryFactory geometryFactory = new GeometryFactory();

	    MathTransform dataToScreen = getTransformDataToScreen(viewport);
		MathTransform screenToData = dataToScreen.inverse();
		
	    // Convert the center to screen space, then convert the screen corners back to world space
	    
	    
	    
	    //geometryFactory.create
		
		
		//MathTransform data2world = CRS.findMathTransform( crs, DefaultGeographicCRS.WGS84 );
//		
//		AffineTransform2D world2screen = new AffineTransform2D(); // start with identity transform
//		world2screen.translate( viewport.minLong, viewport.maxLat ); // relocate to the viewport
//		world2screen.scale( viewport.getWidth() / screen.width, viewport.getHeight() / screen.height ); // scale to fit
//		world2screen.scale( 1.0, -1.0 ); // flip to match screen
//		
//
//		Geometry screenGeometry = geometry.transform( screenCRS, transform );
//
//		
//		System.out.println(JTS.transform(geometryFactory.createPoint(new Coordinate(0, 0)), dataToScreen));
//		System.out.println(JTS.transform(geometryFactory.createPoint(new Coordinate(0, 0)), screenToData));
//		System.out.println(JTS.transform(geometryFactory.createPoint(new Coordinate(viewport.getWidth().doubleValue(), viewport.getHeight().doubleValue())), screenToData));
	}
		
}

