package org.aksw.jena_sparql_api.changeset.util;

import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroup;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroupState;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetState;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
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
				ChangeSetUtils.applyRedo(cs, dataModel);
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
		// If the pointer is placed before the first csg, we now set it to null		
		ChangeSet removeStart = subjectState.getLatestChangeSet();

		if(removeStart != null) {
			if(subjectState.isUndone()) {
				subjectState.setLatestChangeSet(removeStart.getPrecedingChangeSet());
				subjectState.setUndone(false);
			} else {
				removeStart = ChangeSetUtils.getSuccessor(removeStart);
			}
		}

		clear(removeStart);
	}

	public void clear(ChangeSet cs) {
		while(cs != null) {
			ChangeSet next = ChangeSetUtils.getSuccessor(cs);
			
			clearImmediate(cs);
			
			cs = next;
		};
	}
	
	
	public static void clearImmediate(ChangeSet cs) {
		//System.out.println("Clearing properties of cs " + cs);
		for(Resource r : cs.additions()) {
			r.removeProperties();
		}

		for(Resource r : cs.removals()) {
			r.removeProperties();
		}
		
		cs.removeProperties();
	}
}
