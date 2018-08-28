package org.aksw.jena_sparql_api.changeset.util;

import java.util.Objects;
import java.util.Optional;

import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.aksw.jena_sparql_api.changeset.ex.api.CSX;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroup;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroupState;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetState;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ChangeSetGroupManager {
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

	public ChangeSetGroupState getState() {
		ChangeSetGroupState result = CSX.redoPointer.inModel(changeModel).as(ChangeSetGroupState.class);
		return result;
	}
	
	public boolean undo() {
		boolean result = false;
		
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
					
					ChangeSetState subjectState = subjectOfChange.as(ChangeSetState.class);
					ChangeSet latestChangeSet = subjectState.getLatestChangeSet();
					if(subjectState.isUndone()) {
						latestChangeSet = latestChangeSet.getPrecedingChangeSet();
					}
					
					Node latestChangeSetNode = Optional.ofNullable(latestChangeSet).map(RDFNode::asNode).orElse(null);
					Node csNode = cs.asNode();
					
					// Sanity check - the member of the change set group must be equal to the latest
					// changeset of the subject
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
		
		return result;
	}
	
	
	public void redo() {
		ChangeSetGroupState state = getState();
		
		ChangeSetGroup csg = state.getLatestChangeSetGroup();

		if(csg != null) {
			ChangeSetGroup successor = ChangeSetUtils.getSuccessor(csg);

			if(!state.isUndone()) {
				csg = successor;
			}
			
			for(ChangeSet cs : csg.members()) {
				Resource subjectOfChange = cs.getSubjectOfChange();
				ChangeSetState subjectState = subjectOfChange.as(ChangeSetState.class);
				
				// Sanity check - the member of the change set group must be equal to the latest
				// changeset of the subject
				boolean sanityCheckPassed = subjectState.getLatestChangeSet().asNode().equals(cs.asNode());
			
				if(!sanityCheckPassed) {
					throw new IllegalStateException("Inconsistent modification state of resource " + subjectOfChange);
				}
				
				ChangeSetManager csm = new ChangeSetManager(subjectState, dataModel);
				csm.redo();
			}

			state.setLatestChangeSetGroup(csg);
			state.setUndone(false);
		}
	}
	
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
	
	
	public void clearRedo() {
//		ChangeSetGroup current = ChangeSetUtils.getLatestChangeSetGroup(changeModel);
//		ChangeSetGroup succ;
//
//		while((succ = ChangeSetUtils.getSuccessor(current)) != null) {
//			ChangeSetUtils.clearChangeSetGroup(succ);
//			current = succ;
//		}
	}
}
