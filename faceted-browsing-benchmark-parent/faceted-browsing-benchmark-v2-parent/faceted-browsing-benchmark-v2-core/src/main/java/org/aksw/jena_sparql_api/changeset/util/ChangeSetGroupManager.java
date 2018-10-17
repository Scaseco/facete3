package org.aksw.jena_sparql_api.changeset.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.aksw.jena_sparql_api.changeset.ex.api.CSX;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroup;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroupState;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetState;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class ChangeSetGroupManager
	implements RdfChangeTracker
{
	protected Model changeModel;
	protected Model dataModel;

	public ChangeSetGroupManager(Model model) {
		super();
		this.changeModel = model;
		this.dataModel = model;
	}

	public ChangeSetGroupManager(Model changeModel, Model dataModel) {
		super();
		this.changeModel = changeModel;
		this.dataModel = dataModel;
	}

	@Override
	public Model getDataModel() {
		return dataModel;
	}
	
	@Override
	public Model getChangeModel() {
		return changeModel;
	}

	public ChangeSetGroupState getState() {
		ChangeSetGroupState result = CSX.redoPointer.inModel(changeModel).as(ChangeSetGroupState.class);
		return result;
	}
	
	@Override
	public void undo() {
		//boolean result = false;
		
		ChangeSetGroupState state = getState();
		
		ChangeSetGroup csg = state.getLatestChangeSetGroup();
		if(csg != null) {
			if(state.isUndone()) {
				csg = csg.getPrecedingChangeSetGroup();
			}
			
			if(csg != null) {
				for(ChangeSet cs : csg.members()) {
					Resource subjectOfChange = cs.getSubjectOfChange();
					//System.out.println("Undo of " + subjectOfChange);
					
					//ChangeSetState subjectState = state.resourceStates().get(subjectOfChange);
					
					ChangeSetState subjectState = subjectOfChange.as(ChangeSetState.class);
					
					// Sanity check - the member of the change set group must be equal to the latest
					// changeset of the subject
					ChangeSet latestChangeSet = subjectState.getLatestChangeSet();
					if(subjectState.isUndone()) {
						latestChangeSet = latestChangeSet.getPrecedingChangeSet();
					}
					Node latestChangeSetNode = Optional.ofNullable(latestChangeSet).map(RDFNode::asNode).orElse(null);
					Node csNode = cs.asNode();					
					boolean sanityCheckPassed = Objects.equals(latestChangeSetNode, csNode);;				
					if(!sanityCheckPassed) {
						throw new IllegalStateException("Inconsistent modification state of resource " + subjectOfChange + ": " + latestChangeSetNode + " expected to be " + csNode);
					}
					
					ChangeSetManager csm = new ChangeSetManager(subjectState, dataModel);
					csm.undo();
				}
			}
			
			state.setLatestChangeSetGroup(csg);
			state.setUndone(true);
		}
		
		//return result;
	}
	
	@Override
	public void redo() {
		ChangeSetGroupState state = getState();
		
		ChangeSetGroup csg = state.getLatestChangeSetGroup();

		if(csg != null) {
			if(!state.isUndone()) {
				csg = ChangeSetUtils.getSuccessor(csg);
			}
			
			if(csg != null) {
				for(ChangeSet cs : csg.members()) {
					Resource subjectOfChange = cs.getSubjectOfChange();
					ChangeSetState subjectState = subjectOfChange.as(ChangeSetState.class);
					
					// Sanity check - the member of the change set group must be equal to the latest
					// changeset of the subject
					ChangeSet latestChangeSet = subjectState.getLatestChangeSet();
					if(!subjectState.isUndone()) {
						latestChangeSet = ChangeSetUtils.getSuccessor(latestChangeSet);
					}
					Node latestChangeSetNode = Optional.ofNullable(latestChangeSet).map(RDFNode::asNode).orElse(null);
					Node csNode = cs.asNode();					
					boolean sanityCheckPassed = Objects.equals(latestChangeSetNode, csNode);;				
					if(!sanityCheckPassed) {
						throw new IllegalStateException("Inconsistent modification state of resource " + subjectOfChange + ": " + latestChangeSetNode + " expected to be " + csNode);
					}
	
	//				boolean sanityCheckPassed = subjectState.getLatestChangeSet().asNode().equals(cs.asNode());
	//				if(!sanityCheckPassed) {
	//					throw new IllegalStateException("Inconsistent modification state of resource " + subjectOfChange);
	//				}
	//				
					ChangeSetManager csm = new ChangeSetManager(subjectState, dataModel);
					csm.redo();
				}
			
				state.setLatestChangeSetGroup(csg);
				state.setUndone(false);
			}
		}
	}
	
	@Override
	public boolean canRedo() {
		ChangeSetGroupState state = getState();
		
		ChangeSetGroup csg = state.getLatestChangeSetGroup();
		if(csg != null) {
			ChangeSetGroup successor = ChangeSetUtils.getSuccessor(csg);

			if(!state.isUndone()) {
				csg = successor;
			}
		}
		
		boolean result = csg != null;
		return result;
	}
	
	@Override
	public boolean canUndo() {
		ChangeSetGroupState state = getState();
		
		ChangeSetGroup csg = state.getLatestChangeSetGroup();
		if(csg != null) {
			if(state.isUndone()) {
				csg = csg.getPrecedingChangeSetGroup();
			}
		}
		
		boolean result = csg != null;
		return result;
	}
	
	
	@Override
	public void clearRedo() {
		// If the pointer is placed before the first csg, we now set it to null
		ChangeSetGroupState state = getState();
		
		ChangeSetGroup removeStart = state.getLatestChangeSetGroup();

		if(removeStart != null) {
			if(state.isUndone()) {
				state.setLatestChangeSetGroup(removeStart.getPrecedingChangeSetGroup());
				state.setUndone(false);
			} else {
				removeStart = ChangeSetUtils.getSuccessor(removeStart);
			}
		}

		clear(removeStart);
	}
	
	public void clear(ChangeSetGroup csg) {
		while(csg != null) {
			ChangeSetGroup next = ChangeSetUtils.getSuccessor(csg);
			
			clearImmediate(csg);
			
			csg = next;
		};
	}
	
	public void clearImmediate(ChangeSetGroup csg) {
		//System.out.println("Clearing group " + csg);
		for(ChangeSet cs : csg.members()) {
			Resource subjectOfChange = cs.getSubjectOfChange();
			
			// Note clearing redo of a subject may clear changesets referenced by successor csgs
			// just skip these cases
			if(subjectOfChange != null) {
				//System.out.println("  Encountered member cs " + cs);
				ChangeSetState subjectState = subjectOfChange.as(ChangeSetState.class);
				
				ChangeSetManager csm = new ChangeSetManager(subjectState, null);
				csm.clearRedo();
//				csm.clear(cs);
			}
		}
		
		csg.removeProperties();
		
//		System.out.println("START");
//		RDFDataMgr.write(System.out, csg.getModel(), RDFFormat.TURTLE_PRETTY);
//		System.out.println("END");
	}
	
//	@Override
//	public void trackChanges(Consumer<Model> action) {
//		ChangeSetUtils.trackChangesInTxn(changeModel, dataModel, action);
//	}
}
