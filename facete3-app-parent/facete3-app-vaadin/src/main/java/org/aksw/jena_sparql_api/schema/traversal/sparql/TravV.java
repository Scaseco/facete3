package org.aksw.jena_sparql_api.schema.traversal.sparql;

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