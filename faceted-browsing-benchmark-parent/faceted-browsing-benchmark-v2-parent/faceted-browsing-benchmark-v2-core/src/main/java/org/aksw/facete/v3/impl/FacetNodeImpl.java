package org.aksw.facete.v3.impl;

import java.util.Objects;
import java.util.Set;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.DataQuery;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;
import org.hobbit.benchmark.faceted_browsing.v2.main.FacetedQueryGenerator;


public class FacetNodeImpl
	implements FacetNodeResource
{
	protected FacetedQueryResource query;
//	protected FacetNodeResource parent;
	protected Resource state;

//	public FacetNodeImpl(FacetedQueryResource query, Resource state) {
//		this(query, state);
//	}

//	public FacetNodeImpl(Resource state) {
//		this(Objects.requireNonNull(parent).query(), parent, state);
//	}

	/**
	 * Avoid using this ctor directly
	 * 
	 * @param query
	 * @param parent
	 * @param state
	 */
	public FacetNodeImpl(FacetedQueryResource query, Resource state) {
		this.query = query;
//		this.parent = parent;
		this.state = Objects.requireNonNull(state); 
	}

	@Override
	public FacetNodeResource parent() {
		Resource p = ResourceUtils.getPropertyValue(state, Vocab.parent, Resource.class).orElse(null);
		
		return p == null ? null : new FacetNodeImpl(query, p);
	}
	
	@Override
	public FacetedQueryResource query() {
		return query;
	}
	
	@Override
	public Resource state() {
		return state;
	}
	
	@Override
	public FacetDirNode fwd() {
		return new FacetDirNodeImpl(this, true);
	}

	@Override
	public FacetDirNode bwd() {
		return new FacetDirNodeImpl(this, false);
	}

	@Override
	public BinaryRelation getReachingRelation() {
		BinaryRelation result;

		FacetNodeResource parent = parent();
		if(parent == null) {
			result = null;
		} else {
			
			boolean isReverse = false;
			Set<Statement> set = ResourceUtils.listProperties(parent().state(), null).filterKeep(stmt -> stmt.getObject().equals(state)).toSet();
			
			if(set.isEmpty()) {
				isReverse = true;
				set = ResourceUtils.listReverseProperties(parent().state(), null).filterKeep(stmt -> stmt.getSubject().equals(state)).toSet();
			}
			
			// TODO Should never fail - but ensure that
			Property p = set.iterator().next().getPredicate();
			
			result = create(p.asNode(), isReverse);
		}

		return result;
	}
	
	public boolean isReverse() {
		boolean isReverse = false;
		Set<Statement> set = ResourceUtils.listProperties(parent().state(), null).filterKeep(stmt -> stmt.getObject().equals(state)).toSet();
		
		if(set.isEmpty()) {
			isReverse = true;
//			set = ResourceUtils.listReverseProperties(parent().state(), null).filterKeep(stmt -> stmt.getSubject().equals(state)).toSet();
		}
		
		return isReverse;
	}
	
	public static BinaryRelation create(Node node, boolean isReverse) {
		//ElementUtils.createElement(triple)
		Triple t = isReverse
				? new Triple(Vars.o, node, Vars.s)
				: new Triple(Vars.s, node, Vars.o);

		BinaryRelation result = new BinaryRelationImpl(ElementUtils.createElement(t), Vars.s, Vars.o);
		return result;
	}


	public DataQuery<?> createValueQuery(boolean excludeConstraints) {
		FacetedQueryGenerator<FacetNode> qgen = new FacetedQueryGenerator<FacetNode>(new PathAccessorImpl(query));
		query.constraints().forEach(c -> qgen.getConstraints().add(c.expr()));

		FacetNode focus = query().focus();

		UnaryRelation c = qgen.getConceptForAtPath(focus, this, excludeConstraints);
		
		//System.out.println("Available values: " + c);
		
		SparqlQueryConnection conn = query.connection();
		DataQuery<?> result = new DataQueryImpl<>(conn, c.getVar(), c.getElement(), null, RDFNode.class);

		return result;

	}

	@Override
	public DataQuery<?> availableValues() {
		DataQuery<?> result = createValueQuery(true);
		return result;		
	}

	@Override
	public DataQuery<?> remainingValues() {
		DataQuery<?> result = createValueQuery(false);
		return result;
	}

	public FacetNode as(String varName) {
		ResourceUtils.setLiteralProperty(state, Vocab.alias, varName);		
		return this;
	}
	
	@Override
	public FacetNode as(Var var) {
		return as(var.getName());
	}
	
	@Override
	public Var alias() {
		return ResourceUtils.getLiteralPropertyValue(state, Vocab.alias, String.class)
			.map(Var::alloc).orElse(null);
	}

//	@Override
//	public FacetNodeResource parent() {
//		return parent;
//	}

	@Override
	public FacetNode root() {
		FacetNode result = FacetedQueryGenerator.getRoot(this, FacetNode::parent);
		return result;
	}

	@Override
	public ConstraintFacade<? extends FacetNodeResource> constraints() {
		return new ConstraintFacadeImpl<FacetNodeResource>(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FacetNodeImpl other = (FacetNodeImpl) obj;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.equals(other.query))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
}
