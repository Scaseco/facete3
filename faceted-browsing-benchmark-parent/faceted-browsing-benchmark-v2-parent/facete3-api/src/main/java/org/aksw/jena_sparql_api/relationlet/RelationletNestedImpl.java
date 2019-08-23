package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.facete.v3.api.path.NestedVarMap;
import org.aksw.facete.v3.api.path.NestedVarMapImpl;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class RelationletNestedImpl
	extends RelationletBase
	implements RelationletElement
{
	protected Element el;
	protected NestedVarMapImpl varMap;
	protected Map<String, RelationletNestedImpl> aliasToMember;
//	protected Map<Var, Var> exposedVarToElementVar;

	public RelationletNestedImpl(
			Element el,
			Map<Var, Var> varMap,
			Set<Var> fixedVars) {
		this(el, new NestedVarMapImpl(varMap, fixedVars), Collections.emptyMap());
	}
	
	public RelationletNestedImpl(
			Element el,
			NestedVarMapImpl varMap,
			Map<String, RelationletNestedImpl> aliasToMember) {
		super(); //el); 
		this.el = el;
		this.varMap = varMap;
		this.aliasToMember = aliasToMember;
//		this.aliasToMember = aliasToMember;
//		this.exposedVarToElementVar = exposedVarToElementVar;
	}
	
	public Var resolve(VarRefStatic varRef) {
		List<String> labels = varRef.getLabels();
		Var v = varRef.getV();
		
		NestedVarMap src = getNestedVarMap();
		NestedVarMap tgt = src.get(labels);
		Map<Var, Var> map = tgt.getLocalToFinalVarMap();
		
		Var result =  map.get(v);
		return result;
	}
	
	//@Override
	public Set<Var> getVarsMentionedCore() {
		Element el = getElement();
		Set<Var> result = ElementUtils.getVarsMentioned(el);
		return result;
	}

	@Override
	public Set<Var> getVarsMentioned() {
		Set<Var> result = new HashSet<>(getVarsMentionedCore());
		Set<Var> mappedVars = varMap.getVarsMentioned();
		result.addAll(mappedVars);

		return result;
	}
	
	@Override
	public NestedVarMapImpl getNestedVarMap() {
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
	public Relationlet setFixedVar(Var var, boolean onOrOff) {
		throw new UnsupportedOperationException("Cannot mark vars as fixed on this object");
	}

	@Override
	public Relationlet getMember(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Var getInternalVar(Var var) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Var> getExposedVars() {
		return null;
	}

	@Override
	public RelationletNestedImpl materialize() {
		return this;
	}

	@Override
	public Element getElement() {
		return el;
	}
}