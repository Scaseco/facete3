package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction0;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.hobbit.benchmark.faceted_browsing.v2.main.PathToRelationMapper;

//public class ExprTransformViaPathMapper<P>
//	extends ExprTransformCopy
//{
//	protected PathToRelationMapper<P> mapper;
//
//	public ExprTransformViaPathMapper(PathToRelationMapper<P> mapper) {
//		super();
//		this.mapper = mapper;
//	}
//
//	@Override
//	public Expr transform(ExprFunction0 expr) {
//		Expr result;
//		if(expr instanceof ExprPath) {
//			P path = ((ExprPath<P>) expr).getPath();
//			result = mapper.getExpr(path);
//		} else {
//			result = super.transform(expr);
//		}
//		
//		return result;
//	}
//}
