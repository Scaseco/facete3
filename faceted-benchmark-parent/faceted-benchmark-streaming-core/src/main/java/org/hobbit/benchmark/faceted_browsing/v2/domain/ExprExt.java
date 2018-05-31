package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.apache.jena.sparql.expr.ExprFunction0;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * Expression that references a dimension
 * Goal is to make expressions independent of the alias of the dimension
 * 
 * Similar to ExprVar
 * 
 * @author Claus Stadler, May 30, 2018
 *
 */
//public class ExprExt
//	extends ExprFunction0
//{
//	protected ExprExt(String fName) {
//		super(fName);
//	}
//
//	@Override
//	public NodeValue eval(FunctionEnv env) {
//		throw new UnsupportedOperationException();
//	}
//}
