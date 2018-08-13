package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.Maps;

import io.reactivex.Flowable;

public class MapFromBinaryRelation
	extends AbstractMap<RDFNode, RDFNode>
{
	protected Model model;
	protected BinaryRelation relation;
	
	@Override
	public RDFNode get(Object key) {
		RDFNode result = null;
		if(key instanceof RDFNode) {
			RDFNode k = (RDFNode)key;
			BinaryRelation br = relation.joinOn(relation.getSourceVar()).with(ConceptUtils.createFilterConcept(k.asNode())).toBinaryRelation();
		
			result = Optional.ofNullable(fetch(model, br).blockingFirst()).map(Entry::getValue).orElse(null);
		}
		
		return result;
	}
	
	@Override
	public boolean containsKey(Object key) {
		RDFNode item = get(key);
		boolean result = item != null;
		return result;
	}
	
	@Override
	public Set<Entry<RDFNode, RDFNode>> entrySet() {
		
		List<Entry<RDFNode, RDFNode>> list = fetch(model, relation)
			.toList()
			.blockingGet();
	
		Set<Entry<RDFNode, RDFNode>> result = new HashSet<>(list);
	
		return result;
	}

	public static Flowable<Entry<RDFNode, RDFNode>> fetch(Model model, BinaryRelation relation) {
		Query query = RelationUtils.createQuery(relation);
		
		Flowable<Entry<RDFNode, RDFNode>> result = ReactiveSparqlUtils.execSelectQs(() -> QueryExecutionFactory.create(query, model))
			.map(qs -> Maps.immutableEntry(
					qs.get(relation.getSourceVar().getName()),
					qs.get(relation.getTargetVar().getName())));

		return result;
	}
}
