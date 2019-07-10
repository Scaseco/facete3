package org.aksw.facete.v3.api.path;

import org.apache.jena.sparql.core.Var;

public class PathletSimple
	extends RelationletForwarding
	implements Pathlet
{
	protected Var srcVar;
	protected Var tgtVar;
	protected Relationlet relationlet;
	
	public PathletSimple(Var srcVar, Var tgtVar, Relationlet relationlet) {
		super();
		this.srcVar = srcVar;
		this.tgtVar = tgtVar;
		this.relationlet = relationlet;
	}

	@Override
	protected Relationlet getRelationlet() {
		return relationlet;
	}

	@Override
	public Var getSrcVar() {
		return srcVar;
	}

	@Override
	public Var getTgtVar() {
		return tgtVar;
	}

	@Override
	public String toString() {
		return "PathletSimple [srcVar=" + srcVar + ", tgtVar=" + tgtVar + ", relationlet=" + relationlet + "]";
	}
}