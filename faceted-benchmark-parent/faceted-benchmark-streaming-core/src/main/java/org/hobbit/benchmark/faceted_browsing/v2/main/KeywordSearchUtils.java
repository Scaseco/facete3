package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Arrays;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementOptional;

public class KeywordSearchUtils {
    /**
     * ?s ?p ?o // your relation
     * Filter(Regex(Str(?o), 'searchString'))
     * 
     * if includeSubject is true, the output becomes:
     * 
     * Optional {
     *     ?s ?p ?o // your relation
     *     Filter(Regex(Str(?o), 'searchString'))
     * }
     * Filter(Regex(Str(?s), 'searchString') || Bound(?o))
     * 
     * 
     * 
     * @param relation
     * @returns
     */
    public static Concept createConceptRegex(BinaryRelation relation, String searchString, boolean includeSubject) {
        Concept result = includeSubject
            ? createConceptRegexIncludeSubject(relation, searchString)
            : createConceptRegexLabelOnly(relation, searchString);

        return result;
    }
   
    public static Concept createConceptRegexLabelOnly(BinaryRelation relation, String searchString) {
        
        Concept result;
        if(searchString != null) {
            Element element = ElementUtils.groupIfNeeded(Arrays.asList(
            		relation.getElement(),            		
                    new ElementFilter(
                        new E_Regex(new E_Str(new ExprVar(relation.getTargetVar())), NodeValue.makeString(searchString), NodeValue.makeString("i")))
            ));
            
            result = new Concept(element, relation.getSourceVar());
        } else {
            result = null;
        }

        return result;
    }

    public static Concept createConceptRegexIncludeSubject(BinaryRelation relation, String searchString) {
        Concept result;

        if(searchString != null) {
            Element relEl = relation.getElement();
            Var s = relation.getSourceVar();
            Var o = relation.getTargetVar();
    
            // var nv = NodeValueUtils.makeString(searchString);
    
            ExprVar es = new ExprVar(s);
            ExprVar eo = new ExprVar(o);
            Expr ess = NodeValue.makeString(searchString);
            Expr flags = NodeValue.makeString("i");
            
            Expr innerExpr = new E_Regex(new E_Str(eo), ess, flags);
            
            Expr outerExpr = new E_LogicalOr(
                new E_Regex(new E_Str(es), ess, flags),
                new E_Bound(eo));
            
    
            Element element = ElementUtils.groupIfNeeded(Arrays.asList(
                new ElementOptional(
                    ElementUtils.groupIfNeeded(Arrays.asList(relEl, new ElementFilter(innerExpr)))),
                new ElementFilter(outerExpr)
            ));
    
            result = new Concept(element, s);
        } else {
            result = null;
        }
        
        return result;
    }


    /**
     * ?s ?p ?o // relation
     * Filter(<bif:contains>(?o, 'searchString')
     */
    public static Concept createConceptBifContains(BinaryRelation relation, String searchString) {
        Concept result;

        if(searchString != null) {
            Element relEl = relation.getElement();
            Var o = relation.getTargetVar();
            
            ExprVar eo = new ExprVar(o);
            Expr nv = NodeValue.makeString(searchString);
            
            Element element =
                ElementUtils.groupIfNeeded(Arrays.asList(
                    relation.getElement(),
                    //new ElementFilter(new E_Equals(eo, eo))
                    new ElementFilter(new E_Function("bif:contains", new ExprList(Arrays.asList(eo, nv))))
                ));
    
            Var s = relation.getSourceVar();
            result = new Concept(element, s);
        } else {
            result = null;
        }

        return result;
    }
}
