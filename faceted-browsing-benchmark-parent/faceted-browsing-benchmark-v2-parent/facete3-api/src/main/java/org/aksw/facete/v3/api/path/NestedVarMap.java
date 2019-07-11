package org.aksw.facete.v3.api.path;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.collectors.CollectorUtils;
import org.apache.jena.sparql.core.Var;

public class NestedVarMap {
	protected Map<Var, Var> localToFinalVarMap;
	protected Map<String, NestedVarMap> memberVarMap;
	protected Set<Var> fixedFinalVars;
	
	
	public NestedVarMap(Map<Var, Var> localToFinalVarMap, Set<Var> fixedFinalVars) {
		this(localToFinalVarMap, fixedFinalVars, Collections.emptyMap());
	}

	public NestedVarMap(Map<Var, Var> localToFinalVarMap, Set<Var> fixedFinalVars, Map<String, NestedVarMap> memberVarMap) {
		super();
		this.localToFinalVarMap = localToFinalVarMap;
		this.memberVarMap = memberVarMap;
		this.fixedFinalVars = fixedFinalVars;
	}
	
	public Set<Var> getVarsMentioned() {
		Set<Var> result = Stream.concat(localToFinalVarMap.values().stream(),
				memberVarMap.values().stream().flatMap(x -> x.getVarsMentioned().stream()))
				.collect(Collectors.toSet());
		return result;
				
		//Stream.coconcat(localToFinalVarMap.values().stream()
			
	}
	
//	public NestedVarMap(Map<Var, Var> localToFinalVarMap, Map<String, NestedVarMap> memberVarMap,
//			Set<Var> fixedFinalVars) {
//		super();
//		this.localToFinalVarMap = localToFinalVarMap;
//		this.memberVarMap = memberVarMap;
//		this.fixedFinalVars = fixedFinalVars;
//	}

	public NestedVarMap get(List<String> aliases) {
		NestedVarMap result;
		if(aliases.isEmpty()) {
			result = this;
		} else {
			String alias = aliases.iterator().next();
			
			List<String> sublist = aliases.subList(1, aliases.size());
			result = memberVarMap.get(alias).get(sublist);
		}
		
		return result;
	}
	
	public boolean isFixed(Var var) {
		boolean result = fixedFinalVars.contains(var);
		return result;
	}
	
	public boolean isFixed(VarRefStatic varRef) {
		List<String> labels = varRef.getLabels();
		NestedVarMap nvm = get(labels);
		Var v = varRef.getV();
		boolean result = nvm.isFixed(v);
		return result;
	}
	
	public Set<Var> getFixedFinalVars() {
		return fixedFinalVars;
	}

	public Map<Var, Var> getLocalToFinalVarMap() {
		return localToFinalVarMap;
	}

	public Map<String, NestedVarMap> getMemberVarMap() {
		return memberVarMap;
	}
	
	public void transformValues(Function<? super Var, ? extends Var> fn) {
		for(Entry<Var, Var> e : localToFinalVarMap.entrySet()) {
			Var before = e.getValue();
			Var after = fn.apply(before);
			e.setValue(after);
		}
		
		for(NestedVarMap child : memberVarMap.values()) {
			child.transformValues(fn);
		}
		
		// Update fixed vars
		// Note: Fixed vars typically should not be remapped in the first place
		Collection<Var> tmp = fixedFinalVars.stream().map(fn::apply).collect(Collectors.toList());
		
//		if(!tmp.equals(fixedFinalVars)) {
//			System.out.println("DEBUG POINT");
//		}
		fixedFinalVars.clear();
		fixedFinalVars.addAll(tmp);
		
	}
	
	public NestedVarMap clone() {
		Map<Var, Var> cp1 = new LinkedHashMap<>(localToFinalVarMap);
		Set<Var> cp2 = new LinkedHashSet<>(fixedFinalVars);
		Map<String, NestedVarMap> cp3 = memberVarMap.entrySet().stream()
				.collect(CollectorUtils.toLinkedHashMap(Entry::getKey, e -> e.getValue().clone()));
		
		NestedVarMap result = new NestedVarMap(cp1, cp2, cp3);
		return result;
	}

	@Override
	public String toString() {
		return "NestedVarMap [localToFinalVarMap=" + localToFinalVarMap + ", memberVarMap=" + memberVarMap + "]";
	}
}