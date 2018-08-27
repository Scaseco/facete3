package org.aksw.facete.v3.model.api;

import java.util.Optional;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.Path;

public interface FacetNodeResource
	extends Resource
{
	FacetedQuery query();
	
	FacetDirNode fwd();
	FacetDirNode bwd();

	
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

	
	default FacetMultiNode nav(String p, boolean reverse) {
		return reverse ? bwd(p) : fwd(p);
	}

	default FacetMultiNode nav(Node p, boolean reverse) {
		return reverse ? bwd(p) : fwd(p);
	}

	default FacetMultiNode nav(Property p, boolean reverse) {
		return reverse ? bwd(p) : fwd(p);
	}

	
//	default FacetNode nav(Path path) {
//		FacetNode result;
//		if(path == null) {
//			result = this;
//		} else if(path instanceof P_Seq) {
//			P_Seq seq = (P_Seq)path;
//			result = nav(seq.getLeft()).nav(seq.getRight());
//		} else if(path instanceof P_Link) {
//			P_Link link = (P_Link)path;
//			result = fwd(link.getNode()).one();
//		} else if(path instanceof P_ReverseLink) {
//			P_Link reverseLink = (P_Link)path;
//			result = bwd(reverseLink.getNode()).one();
//		} else {
//			throw new IllegalArgumentException("Unsupported path type " + path + " " + Optional.ofNullable(path).map(Object::getClass).orElse(null));
//		}
//		
//		return result;
//	}
	

	FacetNode as(String varName);
	FacetNode as(Var var);
	Var alias();


	FacetNode parent();

	BinaryRelation getReachingRelation();
	
	FacetNode root();
	

	ConstraintFacade<? extends FacetNode> constraints();	
}
