package trash.org.hobbit.faceted_browsing.action;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDFS;


class Vocab {
	public static final String ns = "http://www.example.org/";
	public static final Property width = ResourceFactory.createProperty(ns + "width"); 
	public static final Property height = ResourceFactory.createProperty(ns + "height"); 

	public static final Property lat = ResourceFactory.createProperty(ns + "lat"); 
	public static final Property lon = ResourceFactory.createProperty(ns + "lon");
	public static final Property zoom = ResourceFactory.createProperty(ns + "zoom");
	

	public static final Property dataCrs = ResourceFactory.createProperty(ns + "dataCrs"); 
	public static final Property displayCrs = ResourceFactory.createProperty(ns + "displayCrs"); 
	
}

public class ViewportImpl
	extends ResourceImpl
	implements Viewport
{
	
	public ViewportImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public BigDecimal getWidth() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.width, BigDecimal.class);
	}

	@Override
	public void setWidth(BigDecimal width) {
		ResourceUtils.setLiteralProperty(this, Vocab.width, width);
	}

	@Override
	public BigDecimal getHeight() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.height, BigDecimal.class);
	}

	@Override
	public void setHeight(BigDecimal height) {
		ResourceUtils.setLiteralProperty(this, Vocab.height, height);
	}

	@Override
	public String getDataCrs() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.dataCrs, String.class);
	}

	@Override
	public void setDataCrs(String crs) {
		ResourceUtils.setLiteralProperty(this, Vocab.dataCrs, crs);
	}

	@Override
	public String getDisplayCrs() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.displayCrs, String.class);
	}

	@Override
	public void setDisplayCrs(String crs) {
		ResourceUtils.setLiteralProperty(this, Vocab.displayCrs, crs);
	}

	@Override
	public BigDecimal getCenterX() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.lon, BigDecimal.class);
	}

	@Override
	public void setCenterX(BigDecimal val) {
		ResourceUtils.setLiteralProperty(this, Vocab.lon, val);		
	}

	@Override
	public BigDecimal getCenterY() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.lat, BigDecimal.class);
	}

	@Override
	public void setCenterY(BigDecimal val) {
		ResourceUtils.setLiteralProperty(this, Vocab.lat, val);		
	}

	@Override
	public BigDecimal getZoom() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.zoom, BigDecimal.class);
	}

	@Override
	public void setZoom(BigDecimal zoom) {
		ResourceUtils.setLiteralProperty(this, Vocab.zoom, zoom);		
	}
}
