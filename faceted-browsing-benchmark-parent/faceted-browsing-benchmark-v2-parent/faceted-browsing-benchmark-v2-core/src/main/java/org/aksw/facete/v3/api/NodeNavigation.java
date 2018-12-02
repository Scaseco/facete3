package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.Path;

import java.util.Optional;

import static org.aksw.facete.v3.api.Direction.BACKWARD;


/**
 * Mixin for Node Navigation methods
 */
public interface NodeNavigation<N extends NodeNavigation<N,D,M>, D extends DirNodeNavigation<M>, M extends MultiNodeNavigation<N>> {
	D fwd();
	D bwd();

	//BgpNode model();

	// Convenience shortcuts
	default M fwd(Property property) {
		return fwd().via(property);
	}

	default M bwd(Property property) {
		return bwd().via(property);
	}

	default M fwd(String p) {
		Property property = ResourceFactory.createProperty(p);
		return fwd().via(property);
	}

	default M bwd(String p) {
		Property property = ResourceFactory.createProperty(p);
		return bwd().via(property);
	}

	default M fwd(Node node) {
		return fwd().via(ResourceFactory.createProperty(node.getURI()));
	}

	default M bwd(Node node) {
		return bwd().via(ResourceFactory.createProperty(node.getURI()));
	}

	default D step(Direction direction) {
		return BACKWARD.equals(direction) ? bwd() : fwd();
	}

	default M step(String p, Direction direction) {
		return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
	}

	default M step(Node p, Direction direction) {
		return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
	}

	default M step(Property p, Direction direction) {
		return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
	}

	default N walk(Path path) {
		NodeNavigation<N, D, M> result;
		if(path == null) {
			result = this;
		} else if(path instanceof P_Seq) {
			P_Seq seq = (P_Seq)path;
			result = walk(seq.getLeft()).walk(seq.getRight());
		} else if(path instanceof P_Link) {
			P_Link link = (P_Link)path;
			result = fwd(link.getNode()).one();
		} else if(path instanceof P_ReverseLink) {
			P_ReverseLink reverseLink = (P_ReverseLink)path;
			result = bwd(reverseLink.getNode()).one();
		} else {
			throw new IllegalArgumentException("Unsupported path type " + path + " " + Optional.ofNullable(path).map(Object::getClass).orElse(null));
		}

		return (N) result;
	}

	default N walk(SimplePath simplePath) {
		return walk(SimplePath.toPropertyPath(simplePath));
	}
}
