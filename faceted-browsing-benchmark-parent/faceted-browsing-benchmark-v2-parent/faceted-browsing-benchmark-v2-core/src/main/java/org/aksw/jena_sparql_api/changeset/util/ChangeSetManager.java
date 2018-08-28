package org.aksw.jena_sparql_api.changeset.util;

import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetState;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ReifierStd;

public class ChangeSetManager {
	//protected Model changeModel;
	protected ChangeSetState subjectState;
	protected Model dataModel;

	public ChangeSetManager(ChangeSetState subjectState, Model dataModel) {
		super();
		this.subjectState = subjectState;
		this.dataModel = dataModel;
	}

//	public ChangeSetState getState(Resource subject) {
//		ChangeSetState result = subject.inModel(subject).as(ChangeSetState.class);
//		return result;
//	}
	
	public boolean undo() {
		boolean result = false;
		ChangeSet cs = subjectState.getLatestChangeSet();
		if(cs != null) {
			if(subjectState.isUndone()) {
				cs = cs.getPrecedingChangeSet();
			}
			
			if(cs != null) {
				ChangeSetUtils.applyUndo(cs, dataModel);
				result = true;
			}
			
			subjectState.setLatestChangeSet(cs);
			subjectState.setUndone(true);
		}
		
		return result;
	}
	
	public boolean redo() {
		boolean result = false;
		
		ChangeSet cs = subjectState.getLatestChangeSet();
		if(cs != null) {
			if(!subjectState.isUndone()) {
				cs = ChangeSetUtils.getSuccessor(cs);
			}
			
			if(cs != null) {
				ChangeSetUtils.applyUndo(cs, dataModel);
				result = true;
			}
			
			subjectState.setLatestChangeSet(cs);
			subjectState.setUndone(false);
		}
		
		return result;
	}
	
	public boolean canRedo() {
		return true;
	}
	
	public boolean canUndo() {
		return true;
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
