package org.aksw.facete.v3.api.path;

import java.util.List;

public class Join {
	protected List<VarRef> lhs;
	protected List<VarRef> rhs;
	
	public Join(List<VarRef> lhs, List<VarRef> rhs) {
		super();
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public List<VarRef> getLhs() {
		return lhs;
	}

	public List<VarRef> getRhs() {
		return rhs;
	}
}