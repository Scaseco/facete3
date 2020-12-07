package org.aksw.jena_sparql_api.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.shacl.vocabulary.SHACLM;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.model.SHPropertyShape;

import com.google.common.collect.Iterables;
import com.google.common.collect.MoreCollectors;


public class NodeSchemaFromNodeShape
    implements NodeSchema
{
    protected SHNodeShape nodeShape;

    public NodeSchemaFromNodeShape(SHNodeShape nodeShape) {
        super();
        this.nodeShape = nodeShape;
    }

    protected Stream<PropertySchema> getPropertySchemas(Node predicate, boolean isForward) {
        return getPredicateSchemas().stream()
            .filter(item -> Objects.equals(item.getPredicate(), predicate) && item.isForward() == isForward);
    }

    @Override
    public PropertySchema createPropertySchema(Node predicate, boolean isForward) {
        Set<PropertySchema> set = getPropertySchemas(predicate, isForward).collect(Collectors.toSet());
        PropertySchema result = set.isEmpty() ? null : Iterables.getOnlyElement(set);

        if (result == null) {
            Model m = nodeShape.getModel();
            SHPropertyShape propertyShape = m.createResource().as(SHPropertyShape.class);

            Property p = new PropertyImpl(predicate, (ModelCom)m);

            Resource path = isForward
                    ? p
                    : m.createResource().addProperty(SHACLM.inversePath, p);

            propertyShape.addProperty(SHACLM.path, path);
            nodeShape.addProperty(SHACLM.property, propertyShape);

            result = new PropertySchemaFromPropertyShape(propertyShape);
        }
        return result;
    }

    @Override
    public Set<DirectedFilteredTriplePattern> getGenericPatterns() {
        return Collections.emptySet();
    }

    @Override
    public Collection<PropertySchema> getPredicateSchemas() {
        Collection<PropertySchema> result = nodeShape.getPropertyShapes().stream()
            .map(PropertySchemaFromPropertyShape::new)
            .collect(Collectors.toList());

        return result;
    }


}
