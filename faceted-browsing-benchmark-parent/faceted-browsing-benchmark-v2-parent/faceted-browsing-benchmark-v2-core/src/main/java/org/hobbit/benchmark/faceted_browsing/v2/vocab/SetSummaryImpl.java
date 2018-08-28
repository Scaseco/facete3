package org.hobbit.benchmark.faceted_browsing.v2.vocab;

import java.math.BigDecimal;

import org.aksw.facete.v3.impl.ResourceBase;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class SetSummaryImpl
	extends ResourceBase
	implements SetSummary
{
	public SetSummaryImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	public Long getTotalValueCount() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.totalValueCount, Long.class);
	}
	
	public Long getDistinctValueCount() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.distinctValueCount, Long.class);		
	}
	
	public Number getMin() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.min, BigDecimal.class);				
	}
	
	
	public Number getMax() {
		return ResourceUtils.getLiteralPropertyValue(this, Vocab.max, BigDecimal.class);				
	}

	@Override
	public String toString() {
		return "SetSummary [node=" + this.node + ", getTotalValueCount()=" + getTotalValueCount() + ", getDistinctValueCount()="
				+ getDistinctValueCount() + ", getMin()=" + getMin() + ", getMax()=" + getMax() + "]";
	}
}
