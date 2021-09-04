package org.aksw.jena_sparql_api.schema.traversal.rdf;

import static org.aksw.facete.v3.api.Direction.BACKWARD;

import org.aksw.facete.v3.api.Direction;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;


/**
 * Mixin for Node Navigation methods
 */
public interface TraversalDirection<P, D extends TraversalProperty<P>>
    extends TraversalNode<TraversalDirection<P, D>>
{
    D fwd();
    D bwd();

    @Override
    default TraversalDirection<P, D> traverse(Path<Node> path) {
        return null;
    }


    // Convenience shortcuts
    default P fwd(Property property) {
        return fwd().via(property);
    }

    default P bwd(Property property) {
        return bwd().via(property);
    }

    default P fwd(String p) {
        Property property = ResourceFactory.createProperty(p);
        return fwd().via(property);
    }

    default P bwd(String p) {
        Property property = ResourceFactory.createProperty(p);
        return bwd().via(property);
    }

    default P fwd(Node node) {
        return fwd().via(ResourceFactory.createProperty(node.getURI()));
    }

    default P bwd(Node node) {
        return bwd().via(ResourceFactory.createProperty(node.getURI()));
    }

    default D step(Direction direction) {
        return BACKWARD.equals(direction) ? bwd() : fwd();
    }

    default P step(String p, Direction direction) {
        return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
    }

    default P step(Node p, Direction direction) {
        return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
    }

    default P step(Property p, Direction direction) {
        return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
    }


}
