package org.aksw.facete3.table.mapping.domain;

import java.util.List;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ColumnMapping
	extends Resource
{
//	Node getColumnId();
//	ColumnMapping setColumnId(Node columnId);

	@IriNs("eg")
	String getLabel();
	ColumnMapping setLabel(String label);
	
	@IriType
	@IriNs("eg")
	List<String> getPath();
	
	
	@IriNs("eg")
	List<ColumnMapping> getSubColumns();
}
