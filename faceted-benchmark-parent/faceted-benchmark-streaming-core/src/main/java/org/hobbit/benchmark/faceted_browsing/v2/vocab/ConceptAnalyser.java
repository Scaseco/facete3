package org.hobbit.benchmark.faceted_browsing.v2.vocab;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_IsNumeric;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.AggMax;
import org.apache.jena.sparql.expr.aggregate.AggMin;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

public class ConceptAnalyser {
	boolean includeNonNumeric;
	
	
	// Based on min/max value, we could create a simple histogram based on a given number of buckets
	
	/**
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
	public static Query checkDatatypes(Concept c) {
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
				new ElementFilter(new E_IsNumeric(new ExprVar(c.getVar()))));
		
		core.setQueryPattern(filteredElement);
		
		VarExprList vel = core.getProject();
		ExprVar es = new ExprVar(c.getVar());

		vel.add(Vars.x, core.allocAggregate(new AggCountVar(es)));
		vel.add(Vars.a, core.allocAggregate(new AggCountVarDistinct(es)));
		vel.add(Vars.y, core.allocAggregate(new AggMin(es)));
		vel.add(Vars.z, core.allocAggregate(new AggMax(es)));

//		Query result = new Query();
//		result.setQueryConstructType();
//		result.setQueryPattern(c.getElement());
		
		
		
//		BasicPattern bgp = new BasicPattern();
//		
//		
//		result.setConstructTemplate(new Template(bgp));
		
		//result.getConstructTemplate().

		return core;
	}
}

