package org.aksw.facete3.app.vaadin;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.core.io.ClassPathResource;

public class TransformService {
	
	private Model prefixes;
	
	public TransformService(String prefixFile) {
		//this.prefixes = configPrefixes;
		String path = new ClassPathResource(prefixFile).getPath();
    	this.prefixes = RDFDataMgr.loadModel(path);
	}

	public Model getPrefixFile () {
		return this.prefixes;
	}
	
	public String handleResource(String uri) {
		return prefixes.shortForm(uri);
	}
	
	public String handleObject(RDFNode node) {
		String printName = node.isResource() ? handleResource(node.toString())
				: node.toString();
		return printName;
		
	}
	
}
