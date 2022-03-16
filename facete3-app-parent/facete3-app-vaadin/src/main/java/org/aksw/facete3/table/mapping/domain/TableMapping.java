package org.aksw.facete3.table.mapping.domain;

import java.util.List;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;


@ResourceView
// @Namespaces(@Namespace(prefix = "eg", value = "http://www.example.org/"))
@Namespace(prefix = "eg", value = "http://www.example.org/")
public interface TableMapping
	extends Resource
{
	@IriNs("eg")
	List<ColumnMapping> getColumnMappings();
	// ColumnMapping getRootColumnMapping();
	// TableMapping setRootColumnMapping(ColumnMapping root);
	
	
	/**
	 * Return a now column mapping with a given path or create one and append it.
	 * 
	 * @param path
	 * @return
	 */
	default ColumnMapping getOrCreateColumnMapping(List<String> path) {
		ColumnMapping result = null;
		
		List<ColumnMapping> cms = getColumnMappings();

		for (ColumnMapping cm : cms) {
			List<String> candPath = cm.getPath();
			if (path.equals(candPath)) {
				result = cm;
				break;
			}
		}
		
		if (result == null) {
			result = getModel().createResource().as(ColumnMapping.class);
			result.getPath().addAll(path);
			cms.add(result);
		}
		
		return result;
	}
}
