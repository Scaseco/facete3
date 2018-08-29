package org.aksw.facete.v3.model.bgp.impl;

import org.aksw.facete.v3.bgp.api.BgpNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class TestBgpApi {
	@Test
	public void testBgpApi() {
		Model model = ModelFactory.createDefaultModel();
		
		BgpNode root = model.createResource().as(BgpNode.class);
		BgpNode target = root.fwd(RDF.type).one();

		BgpNode target2 = root.bwd(RDFS.label).one();

	
		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
	}
}
