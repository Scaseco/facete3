package org.hobbit.faceted_browsing.action;

import java.math.BigDecimal;

import org.apache.jena.rdf.model.Resource;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

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

	public static void transform() throws NoSuchAuthorityCodeException, FactoryException {
//		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
//		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:900913");

		//MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		//Geometry targetGeometry = JTS.transform( sourceGeometry, transform);	
	}
}
