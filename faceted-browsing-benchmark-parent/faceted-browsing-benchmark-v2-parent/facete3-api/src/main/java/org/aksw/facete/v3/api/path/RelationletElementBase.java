package org.aksw.facete.v3.api.path;

import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.collectors.CollectorUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public abstract class RelationletElementBase
	extends RelationletBase
	implements RelationletElement
{
	
	@Override
	public Set<Var> getVarsMentioned() {
		Element el = getElement();
		Set<Var> result = ElementUtils.getVarsMentioned(el);
		return result;
	}

	
	@Override
	public RelationletNested materialize() {
		Map<Var, Var> identityMap = getVarsMentioned().stream()
				.collect(CollectorUtils.toLinkedHashMap(x -> x, x -> x));

		Element el = getElement();
		RelationletNested result = new RelationletNested(el, identityMap, fixedVars);
		return result;
	}
	
}