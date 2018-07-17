package org.hobbit.faceted_browsing.action;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDFS;

public class MapStateImpl
	extends ResourceImpl
	implements MapState
{
	
	public MapStateImpl(Node n, EnhGraph m) {
		super(n, m);
	}
	
	@Override
	public BigDecimal getCenterX() {
		return ResourceUtils.getLiteralPropertyValue(this, RDFS.domain, BigDecimal.class)
				.orElse(null);
	}

	@Override
	public void setCenterX(BigDecimal val) {
		ResourceUtils.setLiteralProperty(this, RDFS.domain, val);		
	}

	@Override
	public BigDecimal getCenterY() {
		return ResourceUtils.getLiteralPropertyValue(this, RDFS.range, BigDecimal.class)
				.orElse(null);
	}

	@Override
	public void setCenterY(BigDecimal val) {
		ResourceUtils.setLiteralProperty(this, RDFS.range, val);		
	}

	@Override
	public BigDecimal getZoom() {
		return ResourceUtils.getLiteralPropertyValue(this, RDFS.isDefinedBy, BigDecimal.class)
				.orElse(null);
	}

	@Override
	public void setZoom(BigDecimal zoom) {
		ResourceUtils.setLiteralProperty(this, RDFS.isDefinedBy, zoom);		
	}
}

