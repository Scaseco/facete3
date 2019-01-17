package org.aksw.facete.v3.model.bgp.impl;

import org.aksw.facete.v3.bgp.api.BgpNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

import org.junit.Assert;

public class TestBgpApi {
	@Test
	public void testBgpApi() {
		Model model = ModelFactory.createDefaultModel();
		
		BgpNode root = model.createResource().as(BgpNode.class);
		BgpNode node1 = root.fwd(RDF.type).one();
		BgpNode node2 = node1.bwd(RDF.type).one();
		
		Assert.assertEquals(node1.parent().parent(), root);
		Assert.assertEquals(node2.parent().parent(), node1);

		
		BgpNode target2 = root.bwd(RDFS.label).one();
	
		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
	}
}
