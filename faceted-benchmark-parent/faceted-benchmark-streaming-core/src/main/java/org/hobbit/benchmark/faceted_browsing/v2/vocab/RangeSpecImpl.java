package org.hobbit.benchmark.faceted_browsing.v2.vocab;

import org.aksw.facete.v3.impl.ResourceBase;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class RangeSpecImpl
	extends ResourceBase
	implements RangeSpec
{
	public RangeSpecImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public RDFNode getMin() {
		return ResourceUtils.getPropertyValue(this, Vocab.min, RDFNode.class).orElse(null);
	}

	@Override
	public void setMin(RDFNode min) {
		ResourceUtils.setProperty(this, Vocab.min, min);
	}

	@Override
	public boolean isMinInclusive() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.minInclusive, Boolean.class).orElse(true);
	}

	@Override
	public void setMinInclusive(boolean onOrOff) {
		 ResourceUtils.setLiteralProperty(this, Vocab.minInclusive, onOrOff ? true : null);
	}

	@Override
	public RDFNode getMax() {
		return ResourceUtils.getPropertyValue(this, Vocab.max, RDFNode.class).orElse(null);
	}

	@Override
	public void setMax(RDFNode max) {
		ResourceUtils.setProperty(this, Vocab.max, max);
	}

	@Override
	public boolean isMaxInclusive() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.maxInclusive, Boolean.class).orElse(true);
	}

	@Override
	public void setMaxInclusive(boolean onOrOff) {
		 ResourceUtils.setLiteralProperty(this, Vocab.maxInclusive, onOrOff ? true : null);
	}
}
