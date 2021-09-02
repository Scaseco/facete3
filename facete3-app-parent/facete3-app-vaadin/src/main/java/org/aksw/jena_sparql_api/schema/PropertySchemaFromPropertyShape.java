package org.aksw.jena_sparql_api.schema;

import java.util.Collection;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shacl.vocabulary.SHACLM;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.model.SHPropertyShape;

import com.google.common.collect.Streams;


@ResourceView
public interface PropertySchemaFromPropertyShape
    extends PropertySchema, Resource
{
    default SHPropertyShape getPropertyShape() {
        return as(SHPropertyShape.class);
    }

    default Node getPredicate() {
        SHPropertyShape propertyShape = getPropertyShape();
        Resource r = propertyShape.getPath();
        Resource p;
        if (r.hasProperty(SHACLM.inversePath)) {
            p = r.getPropertyResourceValue(SHACLM.inversePath);
        } else {
            p = r;
        }

        Node result = p.asNode();
        return result;

        //return predicate;
    }

    default boolean isForward() {
        SHPropertyShape propertyShape = getPropertyShape();
        Resource r = propertyShape.getPath();
        boolean result = !r.hasProperty(SHACLM.inversePath);
        return result;
    }

    default NodeSchemaFromNodeShape getTargetSchema() {
        SHPropertyShape propertyShape = getPropertyShape();

        NodeSchemaFromNodeShape result = null;
        Resource targetRes = propertyShape.getClassOrDatatype();

        if (targetRes != null) {
            SHNodeShape targetShape = targetRes.canAs(SHNodeShape.class) ? targetRes.as(SHNodeShape.class) : null;
            result = targetShape == null ? null : targetShape.as(NodeSchemaFromNodeShape.class); // new NodeSchemaFromNodeShape(targetShape);
        }
        return result;
    }

    default boolean canMatchTriples() {
        return true;
    }

    default Triple createMatchTriple(Node source) {
        boolean isForward = isForward();
        Node predicate = getPredicate();

        Triple result = TripleUtils.createMatch(source, predicate, isForward);
        return result;
    }

    default boolean matchesTriple(Node source, Triple triple) {

        Triple matcher = createMatchTriple(source);
        boolean result = matcher.matches(triple);
        return result;
    }

    default long copyMatchingValues(Node source, Collection<Node> target, Graph sourceGraph) {
        boolean isForward = isForward();

        long result = streamMatchingTriples(source, sourceGraph)
            .map(t -> TripleUtils.getTarget(t, isForward))
            .peek(target::add)
            .count();

        return result;
    }

    /**
     * Return a stream of the triples in sourceGraph that match this
     * predicate schema for the given starting node.
     *
     * @param source
     * @param sourceGraph
     * @return
     */
    default Stream<Triple> streamMatchingTriples(Node source, Graph sourceGraph) {
        Triple matcher = createMatchTriple(source);

        ExtendedIterator<Triple> it = sourceGraph.find(matcher);
        Stream<Triple> result = Streams.stream(it);

        return result;

    }

    /**
     * Copy triples that match the predicate specification from the source graph into
     * the target graph.
     *
     * @param target
     * @param source
     */
    default long copyMatchingTriples(Node source, Graph targetGraph, Graph sourceGraph) {
        long result = streamMatchingTriples(source, sourceGraph)
                .peek(targetGraph::add)
                .count();

        return result;
    }

//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((propertyShape == null) ? 0 : propertyShape.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        PropertySchemaFromPropertyShape other = (PropertySchemaFromPropertyShape) obj;
//        if (propertyShape == null) {
//            if (other.propertyShape != null)
//                return false;
//        } else if (!propertyShape.equals(other.propertyShape))
//            return false;
//        return true;
//    }
}
