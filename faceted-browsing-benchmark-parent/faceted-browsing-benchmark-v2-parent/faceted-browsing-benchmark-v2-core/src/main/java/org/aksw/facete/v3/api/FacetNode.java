package org.aksw.facete.v3.api;

import java.util.Optional;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.Path;


/**
 * An object backed by the set of resources at a certain (possibly empty) path of properties.
 * 
 * 
 * @author Claus Stadler, Jul 23, 2018
 *
 */
public interface FacetNode
	extends Castable
{

	FacetedQuery query();
	
	FacetDirNode fwd();
	FacetDirNode bwd();

	//BgpNode model();
	
	// Convenience shortcuts
	default FacetMultiNode fwd(Property property) {
		return fwd().via(property);
	}
	
	default FacetMultiNode bwd(Property property) {
		return bwd().via(property);
	}

	default FacetMultiNode fwd(String p) {
		Property property = ResourceFactory.createProperty(p);
		return fwd().via(property);
	}

	default FacetMultiNode bwd(String p) {
		Property property = ResourceFactory.createProperty(p);
		return bwd().via(property);
	}
	
	default FacetMultiNode fwd(Node node) {
		return fwd().via(ResourceFactory.createProperty(node.getURI()));
	}

	default FacetMultiNode bwd(Node node) {
		return bwd().via(ResourceFactory.createProperty(node.getURI()));
	}

	default FacetDirNode step(boolean reverse) {
		return reverse ? bwd() : fwd();
	}
	
	default FacetMultiNode walk(String p, boolean reverse) {
		return reverse ? bwd(p) : fwd(p);
	}

	default FacetMultiNode walk(Node p, boolean reverse) {
		return reverse ? bwd(p) : fwd(p);
	}

	default FacetMultiNode walk(Property p, boolean reverse) {
		return reverse ? bwd(p) : fwd(p);
	}

	
	default FacetNode walk(Path path) {
		FacetNode result;
		if(path == null) {
			result = this;
		} else if(path instanceof P_Seq) {
			P_Seq seq = (P_Seq)path;
			result = walk(seq.getLeft()).walk(seq.getRight());
		} else if(path instanceof P_Link) {
			P_Link link = (P_Link)path;
			result = fwd(link.getNode()).one();
		} else if(path instanceof P_ReverseLink) {
			P_Link reverseLink = (P_Link)path;
			result = bwd(reverseLink.getNode()).one();
		} else {
			throw new IllegalArgumentException("Unsupported path type " + path + " " + Optional.ofNullable(path).map(Object::getClass).orElse(null));
		}
		
		return result;
	}
	

	FacetNode as(String varName);
	FacetNode as(Var var);
	Var alias();


	FacetNode parent();

	BinaryRelation getReachingRelation();
	
	FacetNode root();
	
	/** Get the set of simple constraints affecting this facet.
	 * Simple constraints are expressions making use of only a single variable.
	 * The set of constraints is treated as a disjunction */
	//Set<Expr> getConstraints();

	/**
	 * List all
	 * 
	 * @return
	 */
//	Set<FacetConstraint> constraints();

	ConstraintFacade<? extends FacetNode> constraints();
	
	//Concept toConcept();
	
	// TODO Some API to get the values of this node by excluding all constraints
	DataQuery<?> availableValues();
	DataQuery<?> remainingValues();	
}

