package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.jena_sparql_api.changeset.util.ChangeSetUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDFS;

public class MainChangeSetTest {
	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel();
		
		ChangeSetUtils.trackChangesInTxn(model, m -> {
			Resource s = m.createResource();
			for(int i = 0; i < 2; ++i) {
				s.addLiteral(RDFS.label, "a" + i);
			}
		});

		ChangeSetUtils.trackChangesInTxn(model, m -> {
			Resource s = m.createResource();
			for(int i = 0; i < 2; ++i) {
				s.addLiteral(RDFS.label, "b" + i);
			}
		});
		
		ChangeSetUtils.trackChangesInTxn(model, m -> m.removeAll(null, RDFS.label, null));

		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
	}
}
