package org.aksw.facete.v3.api;

import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Property;

public interface DataNode {
	Map<Property, DataNode> getDeclaredOutProperties();
	
	
	
	// Get the outgoing property
	DataNode out(Property property);
	
}