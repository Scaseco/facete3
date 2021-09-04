package org.aksw.jena_sparql_api.schema.traversal.sparql;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.node.PathOpsNode;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravAlias;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravDirection;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravProperty;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravValues;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;

public class TravProviderTripleSparql
    extends TravProviderTripleBase<QueryBuilder>
{
    protected UnaryRelation rootConcept;

    public TravProviderTripleSparql(UnaryRelation rootConcept) {
        super(PathOpsNode.get().newRoot());
        this.rootConcept = rootConcept;
    }

    @Override
    public QueryBuilder computeValue(TravValues<QueryBuilder> node) {
        QueryBuilder result;
        if (node.getPath().getNameCount() == 0) {
            result = new QueryBuilderImpl(rootConcept);
        } else {
            Relation tmp = Concept.parse("?s { VALUES (?s) { <urn:fwd> <urn:bwd> } }");
            result = new QueryBuilderImpl(tmp);
        }
        return result;
    }

    @Override
    public QueryBuilder computeValue(TravDirection<QueryBuilder> node) {
        QueryBuilder qb = computeValue(node.getParent());
        UnaryRelation ur = qb.getBaseRelation().toUnaryRelation();
        UnaryRelation ur2 = ur.joinOn(ur.getVar()).with(RelationUtils.SPO, Vars.s).project(Vars.s).toUnaryRelation();
        return new QueryBuilderImpl(ur2);
    }

    @Override
    public QueryBuilder computeValue(TravProperty<QueryBuilder> node) {
        QueryBuilder qb = computeValue(node.getParent().getParent());
        UnaryRelation ur = qb.getBaseRelation().toUnaryRelation();

        Node p = Iterables.getLast(node.getParent().getPath().getSegments());

        UnaryRelation ur2 = ur.joinOn(ur.getVar()).with(RelationUtils.SPO, Vars.s).project(Vars.s).toUnaryRelation();
        return new QueryBuilderImpl(ur2);
    }

    @Override
    public QueryBuilder computeValue(TravAlias<QueryBuilder> node) {
        UnaryRelation tmp = Concept.parse("?s { VALUES (?s) { <urn:default> }");
        return new QueryBuilderImpl(tmp);
    }

}
