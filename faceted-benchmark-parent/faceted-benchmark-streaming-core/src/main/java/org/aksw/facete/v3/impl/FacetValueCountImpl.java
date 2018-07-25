package org.aksw.facete.v3.impl;

import java.util.Optional;

import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.jena_sparql_api.utils.CountInfo;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.engine.CountUtils;

import com.google.common.collect.Range;

public class FacetValueCountImpl
	extends ResourceBase
	implements FacetValueCount
{

	public FacetValueCountImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public Node getValue() {
		return getProperty(RDF.object).getObject().asNode();
	}

	@Override
	public CountInfo getFocusCount() {
		Long min = Optional.ofNullable(getProperty(OWL.minCardinality)).map(Statement::getLong).orElse(null);
		Long max = Optional.ofNullable(getProperty(OWL.maxCardinality)).map(Statement::getLong).orElse(null);

		Range<Long> range;
		if (min == null) {
			throw new RuntimeException("Should not happen");
		} else {
			range = max == null ? Range.atLeast(min) : Range.closed(min, max);
		}

		CountInfo result = CountUtils.toCountInfo(range);
		return result;
	}
}
