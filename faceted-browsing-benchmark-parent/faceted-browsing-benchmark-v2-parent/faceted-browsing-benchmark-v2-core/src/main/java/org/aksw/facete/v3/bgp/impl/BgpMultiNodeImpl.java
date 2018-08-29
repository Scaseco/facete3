package org.aksw.facete.v3.bgp.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.impl.ResourceBase;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class BgpMultiNodeImpl
	extends ResourceBase
	implements BgpMultiNode
{
	public BgpMultiNodeImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	public static <T> Optional<T> toMaybe(Iterable<T> i) {
		Iterator<T> it = i.iterator();

		T first = it.hasNext() ? it.next() : null;
		
		if(it.hasNext()) {
			throw new RuntimeException("More than 1 item found");
		}
		
		Optional<T> result = Optional.ofNullable(first);
		return result;
	}
	
	public static <T> T chainAdd(Collection<? super T> c, T item) {
		c.add(item);
		return item;
	}
	
	@Override
	public BgpNode one() {
		Set<BgpNode> set = new SetFromPropertyValues<>(this, Vocab.one, BgpNode.class);

		BgpNode result = toMaybe(set).orElse(chainAdd(set, getModel().createResource().as(BgpNode.class)));
		
		return result;
	}

	@Override
	public boolean contains(BgpNode facetNode) {
		// TODO Auto-generated method stub
		return false;
	}

}
