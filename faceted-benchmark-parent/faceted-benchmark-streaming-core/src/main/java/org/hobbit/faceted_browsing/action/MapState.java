package org.hobbit.faceted_browsing.action;

import java.math.BigDecimal;

import org.apache.jena.rdf.model.Resource;

public interface MapState
	extends Resource
{
	BigDecimal getCenterX();
	void setCenterX(BigDecimal val);
	
	BigDecimal getCenterY();
	void setCenterY(BigDecimal val);
	
	BigDecimal getZoom();
	void setZoom(BigDecimal zoom);
}
