package org.aksw.facete.v3.impl;

import java.util.Collection;
import java.util.function.Supplier;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;


public class FacetedQueryImpl
	implements FacetedQueryResource
{
	// The actual state is stored in a model rooted in a certain resource
	protected SparqlQueryConnection conn;
	protected Supplier<? extends UnaryRelation> conceptSupplier;
	
//	protected Function<? super Resource, ? extends UnaryRelation> conceptParser;
	
	protected XFacetedQuery modelRoot;
	protected FacetNodeResource root;
	protected FacetNodeResource focus;

	
	
	public FacetedQueryImpl() {
		this.modelRoot = ModelFactory.createDefaultModel().createResource().as(XFacetedQuery.class);
		
		
		this.modelRoot.setBgpRoot(modelRoot.getModel().createResource()
				.addProperty(RDF.type, Vocab.BgpNode)
				.as(BgpNode.class)
		);
		
		this.root = new FacetNodeImpl(this, modelRoot.getBgpRoot());
		this.focus = this.root;
	}
	
	@Override
	public XFacetedQuery modelRoot() {
		return modelRoot;
	}
	
	@Override
	public FacetNodeResource root() {
		return root;
	}

	@Override
	public FacetNodeResource focus() {
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

	@Override
	public SparqlQueryConnection connection() {
		return conn;
	}
	
	@Override
	public Collection<FacetConstraint> constraints() {
		return modelRoot.constraints();
//		Collection<FacetConstraint> result = new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);
//		return result;
	}

//	@Override
//	public FacetNode find(Object id) {
//		FacetNode result = id instanceof FacetNode
//				? (FacetNode)id
//				: id instanceof Node ? : null;
//		
//		if(id instanceof FacetNode) {
//		if(id instanceof Node) {
//			node = 
//		}
//	}

}
