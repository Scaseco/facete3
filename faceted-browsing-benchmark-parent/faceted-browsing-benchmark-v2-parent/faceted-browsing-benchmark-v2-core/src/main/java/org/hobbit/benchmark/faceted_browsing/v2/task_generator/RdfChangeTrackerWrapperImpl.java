package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.Objects;

import org.aksw.jena_sparql_api.changeset.util.ChangeApi;
import org.aksw.jena_sparql_api.changeset.util.ChangeSetGroupManager;
import org.aksw.jena_sparql_api.changeset.util.ChangeSetUtils;
import org.aksw.jena_sparql_api.changeset.util.RdfChangeTrackerWrapper;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.Delta;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class RdfChangeTrackerWrapperImpl
	implements RdfChangeTrackerWrapper
{
	protected Model changeModel;

	protected Model baseModel;
	protected Delta deltaGraph;	
	// the change-aware model
	protected Model dataModel;
	
	protected ChangeApi changeTracker;
	
	public RdfChangeTrackerWrapperImpl(Model changeModel, Model baseModel, Delta deltaGraph, Model dataModel, ChangeApi changeTracker) {
		super();
		this.changeModel = changeModel;
		this.baseModel = baseModel;
		this.deltaGraph = deltaGraph;
		this.dataModel = dataModel;
		this.changeTracker = changeTracker;
	}

	public Model getBaseModel() {
		return baseModel;
	}
	
	@Override
	public Model getDataModel() {
		return dataModel;
	}
	
	@Override
	public Model getChangeModel() {
		return changeModel;
	}

	public static RdfChangeTrackerWrapperImpl create(Model changeModel, Model baseModel) {
		Graph baseGraph = Objects.requireNonNull(baseModel.getGraph());
		Delta deltaGraph = new Delta(baseGraph);
		Model dataModel = ModelFactory.createModelForGraph(deltaGraph);

		ChangeApi changeTracker = new ChangeSetGroupManager(changeModel, baseModel);

		RdfChangeTrackerWrapperImpl result = new RdfChangeTrackerWrapperImpl(changeModel, baseModel, deltaGraph, dataModel, changeTracker);
		
		return result;
	}
	
	@Override
	public void discardChanges() {
		ChangeSetUtils.clearChanges(deltaGraph);
	}
	
	/**
	 * Commit pending changes - will create an 'empty' entry if there are no changes.
	 * 
	 * 
	 * 
	 */
	@Override
	public void commitChanges() {
		System.out.println("COMMITING DELTAS - " + changeModel.size() + " - " + baseModel.size() + " - "  + deltaGraph.size());
		
		RDFDataMgr.write(System.out, changeModel, RDFFormat.TURTLE_PRETTY);
		
		ChangeSetUtils.trackAndApplyChanges(changeModel, baseModel, deltaGraph);
//		ChangeSetUtils.trackAndApplyChanges(
//				changeModel,
//				baseModel,
//				deltaGraph.getAdditions(),
//				deltaGraph.getDeletions());
	}
	
	@Override
	public void commitChangesWithoutTracking() {
//		Graph baseGraph = Objects.requireNonNull(baseModel.getGraph());
		ChangeSetUtils.applyDelta(deltaGraph);
	}

	
	@Override
	public void undo() {
		if(!ChangeSetUtils.hasEmptyChanges(deltaGraph)) {
			throw new RuntimeException("Cannot undo while there are pending changes; commit or discard them first");
		}

		changeTracker.undo();
	}

	@Override
	public void redo() {
		if(!ChangeSetUtils.hasEmptyChanges(deltaGraph)) {
			throw new RuntimeException("Cannot undo while there are pending changes");
		}

		changeTracker.redo();
	}

	@Override
	public boolean canUndo() {
		return changeTracker.canUndo();
	}

	@Override
	public boolean canRedo() {
		return changeTracker.canRedo();
	}

	@Override
	public void clearRedo() {
		changeTracker.clearRedo();
	}

}
