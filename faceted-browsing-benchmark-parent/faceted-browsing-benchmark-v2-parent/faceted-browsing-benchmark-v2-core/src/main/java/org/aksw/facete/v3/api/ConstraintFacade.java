package org.aksw.facete.v3.api;

import java.util.Collection;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.NodeHolder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.Range;

/**
 * In general, there are anonymous and named constraints.
 * Named constraints can be
 * 
 * 
 * @author raven
 *
 */
public interface ConstraintFacade<B> {
	Collection<FacetConstraint> list();

	Collection<HLFacetConstraint> listHl(); 
	
	default Stream<FacetConstraint> stream() {
		return list().stream();
	}
	
	/** Add an anonymous equal constraint */
	ConstraintFacade<B> eq(Node node);
	ConstraintFacade<B> exists();
	ConstraintFacade<B> gt(Node node);
	ConstraintFacade<B> neq(Node node);

	
	ConstraintFacade<B> range(Range<NodeHolder> range);

	default ConstraintFacade<B> eqIri(String iriStr) {
		return eq(NodeFactory.createURI(iriStr));
	}

	default ConstraintFacade<B> eq(String stringLiteral) {
		return eq(NodeFactory.createLiteral(stringLiteral));
	}
	
	default ConstraintFacade<B> eq(RDFNode rdfNode) {
		return eq(rdfNode.asNode());
	}

//	default ConstraintFacade<B> exists(RDFNode rdfNode) {
//		return exists(rdfNode.asNode());
//	}

	/** End constraint building and return the parent object */
	B end();
}
