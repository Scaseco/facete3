package org.aksw.facete.v3.impl;

import java.util.function.Supplier;

import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;


public class FacetedQueryImpl
	implements FacetedQuery
{
	// The actual state is stored in a model rooted in a certain resource
	protected SparqlQueryConnection conn;
	protected Supplier<? extends UnaryRelation> conceptSupplier;
	
	protected Resource modelRoot;
	protected FacetNode root;
	protected FacetNode focus;
	
	
	public FacetedQueryImpl() {
		this.modelRoot = ModelFactory.createDefaultModel().createResource();
		
		Resource rootSubject = modelRoot.getModel().createResource();
		
		this.modelRoot.addProperty(Vocab.property("root"), rootSubject);
		
		this.root = new FacetNodeImpl(null, rootSubject);
		
		
		this.focus = this.root;
	}
	
	@Override
	public FacetNode root() {
		return root;
	}

	@Override
	public FacetNode focus() {
		return focus;
	}

	@Override
	public void focus(FacetNode facetNode) {
		// Ensure this is the right impl
		FacetNodeImpl impl = (FacetNodeImpl)facetNode;
		this.focus = impl;
	}

	@Override
	public Concept toConcept() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FacetedQuery baseConcept(Supplier<? extends UnaryRelation> conceptSupplier) {
		this.conceptSupplier = conceptSupplier;
		return this;
	}

	@Override
	public FacetedQuery baseConcept(UnaryRelation concept) {
		return baseConcept(() -> concept);
	}

	@Override
	public FacetedQuery connection(SparqlQueryConnection conn) {
		this.conn = conn;
		return this;
	}

}
