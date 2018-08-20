package org.hobbit.benchmark.faceted_browsing.v2.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.VPath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

//class PathVisitorImpl<T>
//	implements PathVisitor<T> {
//
//	@Override
//	public T visit(VPath<?> path) {
//		path.
//	}
//	
//}


public class DimensionConstraintBlock {

	//protected BiMap<String, VPath<Node>> namedPaths;
	protected Set<SPath> paths;
	protected Set<Expr> exprs;

	public DimensionConstraintBlock() {
		this(new LinkedHashSet<>(), new LinkedHashSet<>());
	}
	
	
	public DimensionConstraintBlock(Set<SPath> paths, Set<Expr> exprs) {
		this.paths = paths;
		this.exprs = exprs;
	}

	public Set<Expr> getExprsForPath(VPath<?> path) {
		Var v = Var.alloc(path.getAlias());
		
		Set<Expr> result = exprs.stream()
				.filter(e -> !e.getVarsMentioned().contains(v))
				.collect(Collectors.toSet());
		
		return result;
	}
	
	public DimensionConstraintBlock copyWithoutPath(VPath<?> vpath) {
		Set<SPath> tmpPaths = paths.stream()
				.filter(path -> !path.equals(vpath))
				.collect(Collectors.toSet());

		Set<Expr> tmp = getExprsForPath(vpath);

		Set<Expr> tmpExprs = exprs.stream()
				.filter(expr -> tmp.contains(expr))
				.collect(Collectors.toSet());
		

		DimensionConstraintBlock result = new DimensionConstraintBlock(tmpPaths, tmpExprs);
		return result;
	}


	public Set<SPath> getPaths() {
		return paths;
	}


	public Set<Expr> getExprs() {
		return exprs;
	}
	
	
}
