package org.hobbit.faceted_browsing.action;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.impl.FacetConstraintImpl;
import org.aksw.facete.v3.impl.FacetCountImpl;
import org.aksw.facete.v3.impl.FacetValueCountImpl;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Dimension;
import org.hobbit.benchmark.faceted_browsing.v2.domain.DimensionImpl;
import org.hobbit.benchmark.faceted_browsing.v2.domain.SPath;
import org.hobbit.benchmark.faceted_browsing.v2.domain.SPathImpl;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.RangeSpec;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.RangeSpecImpl;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.SetSummary;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.SetSummaryImpl;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.Stack;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.StackImpl;

public class JenaPluginFacetedBrowsing {
	public static void init() {
		init(BuiltinPersonalities.model);		
	}
	
	public static void init(Personality<RDFNode> p) {
		
		p.add(FacetConstraint.class, new SimpleImplementation(FacetConstraintImpl::new));
		
		p.add(SPath.class, new SimpleImplementation(SPathImpl::new));
		p.add(Dimension.class, new SimpleImplementation(DimensionImpl::new));

		
    	p.add(MapState.class, new SimpleImplementation(MapStateImpl::new));
    	p.add(Viewport.class, new SimpleImplementation(ViewportImpl::new));

	
		p.add(FacetCount.class, new SimpleImplementation(FacetCountImpl::new));
		p.add(FacetValueCount.class, new SimpleImplementation(FacetValueCountImpl::new));
		

		// TODO Make an interface
		p.add(SetSummary.class, new SimpleImplementation(SetSummaryImpl::new));
		
		p.add(RangeSpec.class, new SimpleImplementation(RangeSpecImpl::new));
		
		p.add(Stack.class, new SimpleImplementation(StackImpl::new));
	}
}
