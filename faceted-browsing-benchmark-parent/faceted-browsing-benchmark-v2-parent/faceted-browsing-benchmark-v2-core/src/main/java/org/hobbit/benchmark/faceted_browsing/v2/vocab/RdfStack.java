package org.hobbit.benchmark.faceted_browsing.v2.vocab;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface RdfStack
	extends Resource, Iterable<RDFNode>
{
	void push(RDFNode item);
	RDFNode pop();
	
	//Iterator<RDFNode> iterator();
}
