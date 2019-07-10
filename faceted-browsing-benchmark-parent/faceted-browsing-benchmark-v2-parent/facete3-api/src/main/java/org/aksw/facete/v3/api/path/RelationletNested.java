package org.aksw.facete.v3.api.path;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class RelationletNested
	extends RelationletElementImpl
{
	protected NestedVarMap varMap;
	protected Map<String, RelationletNested> aliasToMember;
//	protected Map<Var, Var> exposedVarToElementVar;

	public RelationletNested(
			Element el,
			Map<Var, Var> varMap,
			Set<Var> fixedVars) {
		this(el, new NestedVarMap(varMap, fixedVars), Collections.emptyMap());
	}
	
	public RelationletNested(
			Element el,
			NestedVarMap varMap,
			Map<String, RelationletNested> aliasToMember) {
		super(el); 
		this.varMap = varMap;
		this.aliasToMember = aliasToMember;
//		this.aliasToMember = aliasToMember;
//		this.exposedVarToElementVar = exposedVarToElementVar;
	}
	
	public NestedVarMap getNestedVarMap() {
		return varMap;
	}
	
//
//	@Override
//	public RelationletNested getMember(String alias) {
//		return null;
//		//return aliasToMember.get(alias);
//	}
//
//	@Override
//	public Var getInternalVar(Var var) {		
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Collection<Var> getExposedVars() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Set<Var> getVarsMentioned() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
	@Override
	public Set<Var> getFixedVars() {
		return varMap.getFixedFinalVars();
	}
//
	@Override
	public Relationlet setVarFixed(Var var, boolean onOrOff) {
		throw new UnsupportedOperationException("Cannot mark vars as fixed on this object");
	}
}