package org.aksw.jena_sparql_api.changeset;

import org.aksw.jena_sparql_api.changeset.util.RdfChangeTrackerWrapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.RdfChangeTrackerWrapperImpl;
import org.junit.Assert;
import org.junit.Test;


public class TestChangeSetTracker {
	
	@Test
	public void testCanUndo() {
		Model baseModel = ModelFactory.createDefaultModel();
		Model changeModel = ModelFactory.createDefaultModel();

		//RdfChangeTrackerWrapper
		RdfChangeTrackerWrapper changeTracker = RdfChangeTrackerWrapperImpl.create(changeModel, baseModel);		
		
		Model dataModel = changeTracker.getDataModel();
		Assert.assertFalse(changeTracker.canUndo());
		
		dataModel.createResource().addProperty(RDF.type, OWL.Class);
		Assert.assertFalse(changeTracker.canUndo());

		changeTracker.commitChanges();
		Assert.assertTrue(changeTracker.canUndo());

		changeTracker.undo();
		Assert.assertFalse(changeTracker.canUndo());


	}

	@Test
	public void testChangeTracker() {
		Model baseModel = ModelFactory.createDefaultModel();
		Model changeModel = ModelFactory.createDefaultModel();

		//RdfChangeTrackerWrapper
		RdfChangeTrackerWrapper changeTracker = RdfChangeTrackerWrapperImpl.create(changeModel, baseModel);		
		
		Model dataModel = changeTracker.getDataModel();
		
		dataModel.createResource().addProperty(RDF.type, OWL.Class);
		changeTracker.commitChanges();
		changeTracker.undo();
		
		dataModel.createResource().addProperty(RDFS.label, "Clazz");
		changeTracker.commitChanges();
		changeTracker.commitChanges();
		changeTracker.commitChanges();
		changeTracker.commitChanges();
		
		changeTracker.undo();
		changeTracker.undo();
		changeTracker.undo();
		changeTracker.undo();
		changeTracker.clearRedo();
//		
//		dataModel.createResource().addProperty(RDFS.label, "Class");
//		changeTracker.commitChanges();
//		
//		changeTracker.commitChanges();
//		changeTracker.commitChanges();
//		changeTracker.commitChanges();
//		
//		changeTracker.undo();
//		changeTracker.undo();
//		changeTracker.undo();
//		changeTracker.undo();

		System.out.println("STATUS-----------------------------------------------");
		RDFDataMgr.write(System.out, changeModel, RDFFormat.NTRIPLES);
	}
}
