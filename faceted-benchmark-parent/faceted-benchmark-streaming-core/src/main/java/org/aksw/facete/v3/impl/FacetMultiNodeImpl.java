package org.aksw.facete.v3.impl;

import java.util.Set;

import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.aksw.jena_sparql_api.utils.model.SetFromResourceAndInverseProperty;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;


public class FacetMultiNodeImpl
	implements FacetMultiNode
{
	protected FacetNodeResource parent;
	protected Property property;
	protected boolean isFwd;

	
	public FacetMultiNodeImpl(FacetNodeResource parent, Property property, boolean isFwd) {
		super();
		this.parent = parent;
		this.property = property;
		this.isFwd = isFwd;
	}

	public Set<Resource> liveBackingSet() {
		Set<Resource> result = isFwd
				? new SetFromPropertyValues<>(parent.state(), property, Resource.class)
				: new SetFromResourceAndInverseProperty<>(parent.state(), property, Resource.class);
				
		return result;
	}
	
//	@Override
//	public Set<FacetNode> children() {
//		return new CollectionFromConverter(
//			new SetFromPropertyValues<>(parent.state(), property, Resource.class),
//			Converter.from(FacetNodeImpl::new, FacetNode::state);
//		);
//	}

	
	@Override
	public boolean hasMultipleReferencedAliases() {
		return false;
	}

	/**
	 * Gets or creates a single successor
	 * 
	 */
	@Override
	public FacetNode one() {
		// TODO We could use .children as well
		Set<Resource> set = liveBackingSet();
		
		FacetNode result;
		Resource r;
		if(set.isEmpty()) {
			r = ResourceFactory.createResource();
			set.add(r);
		}
		
		if(set.size() == 1) {
			result = new FacetNodeImpl(parent, set.iterator().next());
		} else {
			throw new RuntimeException("Multiple aliases defined");
		}

		return result;
	}

	@Override
	public void setConjunctive() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void remainingValues() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void availableValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(FacetNode facetNode) {
		boolean result;
		if(!(facetNode instanceof FacetNodeResource)) {
			result = false;
		} else {
			FacetNodeResource tmp = (FacetNodeResource)facetNode;
			Resource r = tmp.state();
			
			Set<Resource> set = liveBackingSet();
			result = set.contains(r);			
		}
		return result;
	}
	
	
	
}
