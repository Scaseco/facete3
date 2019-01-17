package org.hobbit.benchmark.faceted_browsing.v2;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.RdfStack;
import org.junit.Test;

public class TestRdfStack {

	@Test(expected=EmptyStackException.class)
	public void testStack() {
		Model m = ModelFactory.createDefaultModel();
		RdfStack s = m.createResource().as(RdfStack.class);

		List<String> ops = Arrays.asList("a", "b", "c", null, "d", null, null, null, null);

		for(String op : ops) {
			if(op != null) {
				s.push(m.createLiteral(op));
			} else {
				s.pop();
			}
			RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
		}
	}
}
