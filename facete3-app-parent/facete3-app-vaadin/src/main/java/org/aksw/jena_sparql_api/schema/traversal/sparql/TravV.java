package org.aksw.jena_sparql_api.schema.traversal.sparql;

import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravAlias;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravDirection;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravTripleVisitor;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

//public interface TravV<V> {
//
//    String reachingAlias();
//
//    TravAlias<V> parent();
//
//    /** The domain alias for 'going to' a certain value - delegates to traverse */
//    default TravDirection<V> goTo(String iri) {
//        return goTo(NodeFactory.createURI(iri));
//    }
//
//    /** The domain alias for 'going to' a certain value - delegates to traverse */
//    default TravDirection<V> goTo(Resource r) {
//        return goTo(r.asNode());
//    }
//
//    /** The domain alias for 'going to' a certain value - delegates to traverse */
//    default TravDirection<V> goTo(Node value) {
//        return traverse(value);
//    }
//
//
//    TravDirection<V> traverse(Node segment);
//
//    V payload();
//
//    <T> T accept(TravTripleVisitor<V> visitor);
//
//}