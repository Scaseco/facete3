package org.aksw.facete.v3.impl;

import java.util.Map;

import org.aksw.facete.v3.api.DataQuery;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.BgpDirNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.Template;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;
import org.hobbit.benchmark.faceted_browsing.v2.main.FacetedQueryGenerator;

public class FacetDirNodeImpl
	implements FacetDirNode
{
//	abstract boolean isFwd();
//
//	@Override
//	public FacetDirNode getParent() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	protected FacetNodeResource parent;

	protected BgpDirNode state;
	
	//protected boolean isFwd;
	//protected Var alias;
	
	public FacetDirNodeImpl(FacetNodeResource parent, BgpDirNode state) {//boolean isFwd) {
		this.parent = parent;
		//this.isFwd = isFwd;
		this.state = state;
	}
	
	@Override
	public FacetNodeResource parent() {
		return parent;
	}

	@Override
	public FacetMultiNode via(Node node) {
		return via(node.getURI());
	}

	@Override
	public FacetMultiNode via(String propertyIRI) {
		return via(ResourceFactory.createProperty(propertyIRI));
	}
	
	@Override
	public FacetMultiNode via(Property property) {
		return new FacetMultiNodeImpl(parent, state.via(property));		
		//return new FacetMultiNodeImpl(parent, property, isFwd);
	}
	
	@Override
	public DataQuery<?> facets() {
		FacetedQueryResource facetedQuery = this.parent().query();
		
		BgpNode bgpRoot = facetedQuery.modelRoot().getBgpRoot();

//		BinaryRelation br = FacetedBrowsingSessionImpl.createQueryFacetsAndCounts(path, isReverse, pConstraint);
		FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(bgpRoot));
		facetedQuery.modelRoot().constraints().forEach(c -> qgen.getConstraints().add(c.expr()));

		UnaryRelation concept = qgen.createConceptFacets(parent.state(), !this.state.isFwd(), false, null);
		
//		BinaryRelation br = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, pConstraint)(relations, null);
//
//		
//		BasicPattern bgp = new BasicPattern();
//		bgp.add(new Triple(br.getSourceVar(), Vocab.facetCount.asNode(), br.getTargetVar()));
//		Template template = new Template(bgp);
//		
		DataQuery<?> result = new DataQueryImpl<>(parent.query().connection(), concept.getVar(), concept.getElement(), null, RDFNode.class);
//
		return result;
	}
	
	@Override
	public DataQuery<FacetCount> facetCounts() {
		FacetedQueryResource facetedQuery = this.parent().query();
		BgpNode bgpRoot = facetedQuery.modelRoot().getBgpRoot();
		
//		BinaryRelation br = FacetedBrowsingSessionImpl.createQueryFacetsAndCounts(path, isReverse, pConstraint);
		FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(bgpRoot));
		facetedQuery.constraints().forEach(c -> qgen.getConstraints().add(c.expr()));

		Map<String, BinaryRelation> relations = qgen.getFacets(parent.state(), !this.state.isFwd(), false);
		
		BinaryRelation br = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, null);

		
		BasicPattern bgp = new BasicPattern();
		bgp.add(new Triple(br.getSourceVar(), Vocab.facetCount.asNode(), br.getTargetVar()));
		Template template = new Template(bgp);
		
		DataQuery<FacetCount> result = new DataQueryImpl<>(parent.query().connection(), br.getSourceVar(), br.getElement(), template, FacetCount.class);

		return result;
	}

	@Override
	public DataQuery<FacetValueCount> facetValueCounts() {
		FacetedQueryResource facetedQuery = this.parent().query();

//		BinaryRelation br = FacetedBrowsingSessionImpl.createQueryFacetsAndCounts(path, isReverse, pConstraint);
		FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(facetedQuery.modelRoot().getBgpRoot()));
		facetedQuery.constraints().forEach(c -> qgen.getConstraints().add(c.expr()));

		TernaryRelation tr = qgen.getFacetValues(this.parent().query().focus().state(), this.parent().state(), !this.state.isFwd(), null, null);
		
		
		BasicPattern bgp = new BasicPattern();
		bgp.add(new Triple(tr.getS(), Vocab.value.asNode(), tr.getP()));
		bgp.add(new Triple(tr.getS(), Vocab.facetCount.asNode(), tr.getO()));
		Template template = new Template(bgp);
		
		DataQuery<FacetValueCount> result = new DataQueryImpl<>(parent.query().connection(), tr.getS(), tr.getElement(), template, FacetValueCount.class);

		return result;
	}


	@Override
	public FacetedQuery getQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryRelation facetValueRelation() {
		FacetedQueryResource facetedQuery = this.parent().query();

		FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(facetedQuery.modelRoot().getBgpRoot()));
		facetedQuery.constraints().forEach(c -> qgen.getConstraints().add(c.expr()));

		TernaryRelation tr = qgen.getFacetValueRelation(this.parent().query().focus().state(), this.parent().state(), !this.state.isFwd(), null, null);

		BinaryRelation result = new BinaryRelationImpl(tr.getElement(), tr.getP(), tr.getO());
		return result;
	}
	
}
