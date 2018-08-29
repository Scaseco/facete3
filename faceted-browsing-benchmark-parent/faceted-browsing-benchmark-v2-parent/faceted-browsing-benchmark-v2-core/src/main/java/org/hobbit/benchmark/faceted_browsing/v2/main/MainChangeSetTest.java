package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Objects;

import org.aksw.jena_sparql_api.changeset.util.ChangeSetGroupManager;
import org.aksw.jena_sparql_api.changeset.util.ChangeSetManager;
import org.aksw.jena_sparql_api.changeset.util.ChangeSetUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDFS;

public class MainChangeSetTest {
	public static void main(String[] args) {
		
		
		Model changeModel = ModelFactory.createDefaultModel();
		Model dataModel = ModelFactory.createDefaultModel();

		Resource a = dataModel.createResource("http://www.example.org/a");
		Resource b = dataModel.createResource("http://www.example.org/b");

		ChangeSetGroupManager csgm = new ChangeSetGroupManager(changeModel, dataModel);

		ChangeSetUtils.trackChangesInTxn(changeModel, dataModel, m -> {
			for(int i = 0; i < 2; ++i) {
				a.inModel(m).addLiteral(RDFS.label, "a" + i);
			}
		});

		ChangeSetUtils.trackChangesInTxn(changeModel, dataModel, m -> m.removeAll(null, RDFS.label, null));
		csgm.undo(); // undo removals

		// the next change should clear the redo action
		ChangeSetUtils.trackChangesInTxn(changeModel, dataModel, m -> {
			for(int i = 0; i < 2; ++i) {
				b.inModel(m).addLiteral(RDFS.label, "b" + i);
			}
		});
		
		
		csgm.canRedo(); // should be false
		
		
		System.out.println("Change set information");
		RDFDataMgr.write(System.out, changeModel, RDFFormat.TURTLE_FLAT);
		
		System.out.println("Remaining data");
		RDFDataMgr.write(System.out, ResourceUtils.reachableClosure(a), RDFFormat.TURTLE_FLAT);
		RDFDataMgr.write(System.out, ResourceUtils.reachableClosure(b), RDFFormat.TURTLE_FLAT);
	}


	public static void testBlankNodesEquality() {
		// Test to find out whether blank nodes can be shared between (in-memory)
		// models and still stay equal
		// (of course, this would break after serialization) 
		Model a = ModelFactory.createDefaultModel();
		Model b = ModelFactory.createDefaultModel();
		
		Resource x = a.createResource();
		Resource y = x.inModel(b);
		System.out.println("Is equal: " + Objects.equals(x, y));
	}
}
