package org.aksw.jena_sparql_api.schema.traversal.sparql;

import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravAlias;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravDirection;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravProperty;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravTripleVisitor;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravValues;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.apache.jena.graph.Node;

public class TravTripleVisitorSparql
    // extends TravProviderTripleBase<Void>
    implements TravTripleVisitor<QueryBuilder>
{
    protected Fragment1 rootConcept;

    public TravTripleVisitorSparql(Fragment1 rootConcept) {
        // super(PathOpsNode.get().newRoot());
        this.rootConcept = rootConcept;
    }

    public static TravTripleVisitor<QueryBuilder> create(Fragment1 rootConcept) {
        return new TravTripleVisitorSparql(rootConcept);
    }

    @Override
    public QueryBuilder visit(TravValues<?> node) {
        Fragment1 rel;
        if (node.path().getNameCount() == 0) {
            rel = rootConcept;
        } else {

            Node s = node.parent().parent().parent().reachingSource();
            boolean isFwd = node.parent().parent().reachedByFwd();
            Node p = node.parent().reachingPredicate();

            // TODO The alias should affect variable naming

            rel = isFwd
                    ? FragmentUtils.createTernaryRelation(s, p, Node.ANY).project(Vars.o).toUnaryRelation()
                    : FragmentUtils.createTernaryRelation(Node.ANY, p, s).project(Vars.o).toUnaryRelation();

        }

        QueryBuilder result = new QueryBuilderImpl(rel);

        return result;
    }

    @Override
    public QueryBuilder visit(TravDirection<?> node) {
        Fragment tmp = Concept.parse("?s { VALUES ?s { <urn:fwd> <urn:bwd> } }");
        QueryBuilder result = new QueryBuilderImpl(tmp);
        return result;
//
//        QueryBuilder qb = computeValue(node.getParent());
//        UnaryRelation ur = qb.getBaseRelation().toUnaryRelation();
//        UnaryRelation ur2 = ur.joinOn(ur.getVar()).with(RelationUtils.SPO, Vars.s).project(Vars.s).toUnaryRelation();
//        return new QueryBuilderImpl(ur2);
    }

    @Override
    public QueryBuilder visit(TravProperty<?> node) {
        // Path<Node> path = node.getParent().getParent().getPath();
        // UnaryRelation s = node.getParent().getParent().getValue().getBaseRelation().toUnaryRelation();

        Node s = node.parent().path().getFileName().toSegment();
        Node dir = node.path().getFileName().toSegment();
        boolean isFwd = dir.equals(TravDirection.FWD);

        QueryBuilder qb = visit(node.parent().parent());
//        UnaryRelation ur = qb.getBaseRelation().toUnaryRelation();
//
//        Node p = Iterables.getLast(node.getParent().getPath().getSegments());
//
        Fragment1 ur2;

        if (isFwd) {
            ur2 = FragmentUtils.createTernaryRelation(s, Node.ANY, Node.ANY).project(Vars.p).toUnaryRelation();
        } else {
            ur2 = FragmentUtils.createTernaryRelation(Node.ANY, Node.ANY, s).project(Vars.p).toUnaryRelation();
        }


        return new QueryBuilderImpl(ur2);
    }

    @Override
    public QueryBuilder visit(TravAlias<?> node) {
        Fragment1 tmp = Concept.parse("?s { VALUES ?s { '' } }");
        return new QueryBuilderImpl(tmp);
    }

}
