package org.hobbit.faceted_browsing.action;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.impl.FacetConstraintImpl;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Dimension;
import org.hobbit.benchmark.faceted_browsing.v2.domain.DimensionImpl;
import org.hobbit.benchmark.faceted_browsing.v2.domain.SPath;
import org.hobbit.benchmark.faceted_browsing.v2.domain.SPathImpl;

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
    }
}
