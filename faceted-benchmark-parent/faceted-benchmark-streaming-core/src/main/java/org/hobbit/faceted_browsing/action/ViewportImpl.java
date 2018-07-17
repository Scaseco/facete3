package org.hobbit.faceted_browsing.action;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDFS;

public class ViewportImpl
	extends ResourceImpl
	implements Viewport
{
	
	public ViewportImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public BigDecimal getWidth() {
		return ResourceUtils.getLiteralPropertyValue(this, RDFS.domain, BigDecimal.class)
				.orElse(null);
	}

	@Override
	public void setWidth(BigDecimal width) {
		ResourceUtils.setLiteralProperty(this, RDFS.domain, width);
	}

	@Override
	public BigDecimal getHeight() {
		return ResourceUtils.getLiteralPropertyValue(this, RDFS.range, BigDecimal.class)
				.orElse(null);
	}

	@Override
	public void setHeight(BigDecimal height) {
		ResourceUtils.setLiteralProperty(this, RDFS.range, height);
	}

	@Override
	public String getDataCrs() {
		return ResourceUtils.getLiteralPropertyValue(this, RDFS.isDefinedBy, String.class)
				.orElse(null);
	}

	@Override
	public void setDataCrs(String crs) {
		ResourceUtils.setLiteralProperty(this, RDFS.isDefinedBy, crs);
	}

	@Override
	public String getDisplayCrs() {
		return ResourceUtils.getLiteralPropertyValue(this, RDFS.seeAlso, String.class)
				.orElse(null);
	}

	@Override
	public void setDisplayCrs(String crs) {
		ResourceUtils.setLiteralProperty(this, RDFS.seeAlso, crs);
	}

}
