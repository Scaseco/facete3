package org.hobbit.benchmark.faceted_browsing.v2.vocab;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.impl.DataQueryImpl;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.E_Datatype;
import org.apache.jena.sparql.expr.E_IsNumeric;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.AggMax;
import org.apache.jena.sparql.expr.aggregate.AggMin;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.Template;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class ConceptAnalyser {
    boolean includeNonNumeric;


    /**
     *  DISTINCT ?p { ?s ?p ?o . FILTER(datatype(?o) = xsd:dateTime) }
     *
     * @param model
     */
    public UnaryRelation analyzeEventGraphs(Model model) {
        Query query = null;
        QueryExecutionFactory.create(query, model).execSelect();

        ResourceShapeBuilder rsp = new ResourceShapeBuilder();
        List<Node> temporalPredicates = new ArrayList<>();

        // TODO extract the shape pattern for the given temporal triple
        // later check if

//		for(Node p : temporalPredicates) {
//
//		}
//
        return null;
    }

    public static UnaryRelation createConceptTemporalProperties() {

        UnaryRelation result = new Concept(
                ElementUtils.createElementGroup(
                    ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o),
                    new ElementFilter(new E_Datatype(new ExprVar(Vars.o)))),
                Vars.o);

        return result;
    }

    // Based on min/max value, we could create a simple histogram based on a given number of buckets


    /**
     * Analyze the values of the last relation column.
     * Group by all other columns.
     *
     * Checks the number of numeric types and yields
     * - total number of numeric values
     * - distinct number of numeric values
     * - minimum value
     *  -maximum value
     *
     * SELECT
     *   concept(?s)
     *   FILTER(isNumeric(?s))
     *
     * @param c
     */
    public static DataQuery<SetSummary> checkDatatypes(Relation c) {
        List<Var> vars = c.getVars();
        Var targetVar = vars.get(vars.size() - 1);

        List<Var> groupVars = vars.subList(0, vars.size() - 1);

        // TODO Would be cool if we could extend the RDF mapper to support this kind of query construnction:
//		RdfEntityManager em;
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//		CriteriaQuery cq = cb.createQuery();
//		Root root = cq.from(c);
//		cq.multiselect(cb.count(root), cb.max(root), cb.min(root));
//		cq.where(cb.isNumeric(root));

        Query core = new Query();
        core.setQuerySelectType();


        //c.joinOn(c.getVar()).with(new ElementFilter(new E_IsNumeric(c.)));

        Element filteredElement = ElementUtils.groupIfNeeded(c.getElement(),
                new ElementFilter(new E_IsNumeric(new ExprVar(targetVar))));

        core.setQueryPattern(filteredElement);

        VarExprList vel = core.getProject();
        ExprVar es = new ExprVar(targetVar);

        groupVars.forEach(vel::add);
        groupVars.forEach(core.getGroupBy()::add);

        Var s;
        if(groupVars.size() == 1) {
            s = groupVars.get(0);
        } else {
            s = Var.alloc("_groupVar__");
            vel.add(s, new E_BNode());
        }

        vel.add(Vars.x, core.allocAggregate(new AggCountVar(es)));
        vel.add(Vars.a, core.allocAggregate(new AggCountVarDistinct(es)));
        vel.add(Vars.y, core.allocAggregate(new AggMin(es)));
        vel.add(Vars.z, core.allocAggregate(new AggMax(es)));

        BasicPattern bgp = new BasicPattern();

        groupVars.forEach(gv -> bgp.add(new Triple(s, Vocab.groupKey.asNode(), gv)));

        bgp.add(new Triple(s, Vocab.totalValueCount.asNode(), Vars.x));
        bgp.add(new Triple(s, Vocab.distinctValueCount.asNode(), Vars.a));
        bgp.add(new Triple(s, Vocab.min.asNode(), Vars.y));
        bgp.add(new Triple(s, Vocab.max.asNode(), Vars.z));
        Template template = new Template(bgp);

        DataQuery<SetSummary> result = new DataQueryImpl<>(null, new ElementSubQuery(core), s, template, SetSummary.class);

        return result;
    }
}

