package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;

import com.google.common.collect.Streams;


class PathVisitorFailByDefault
	implements PathVisitor {
	@Override public void visit(P_Link pathNode) { unhandledPath(pathNode); }
	@Override public void visit(P_ReverseLink pathNode) { unhandledPath(pathNode); }
	@Override public void visit(P_NegPropSet pathNotOneOf) { unhandledPath(pathNotOneOf); }
	@Override public void visit(P_Inverse inversePath) { unhandledPath(inversePath); }
	@Override public void visit(P_Mod pathMod) {  unhandledPath(pathMod); }
	@Override public void visit(P_FixedLength pFixedLength) { unhandledPath(pFixedLength); }
	@Override public void visit(P_Distinct pathDistinct) { unhandledPath(pathDistinct); }
	@Override public void visit(P_Multi pathMulti) { unhandledPath(pathMulti); }
	@Override public void visit(P_Shortest pathShortest) { unhandledPath(pathShortest); }
	@Override public void visit(P_ZeroOrOne path) { unhandledPath(path); }
	@Override public void visit(P_ZeroOrMore1 path) { unhandledPath(path); }
	@Override public void visit(P_ZeroOrMoreN path) { unhandledPath(path); }
	@Override public void visit(P_OneOrMore1 path) { unhandledPath(path); }
	@Override public void visit(P_OneOrMoreN path) { unhandledPath(path); }
	@Override public void visit(P_Alt pathAlt) { unhandledPath(pathAlt); }
	@Override public void visit(P_Seq pathSeq) { unhandledPath(pathSeq); }

	public void unhandledPath(Path path) {
		throw new UnsupportedOperationException();
	}
}

public class PathVisitorToList
	extends PathVisitorFailByDefault {
	
	protected List<P_Path0> result = new ArrayList<>();
	
	public List<P_Path0> getResult() {
		return result;
	}
	
	@Override
	public void visit(P_Link path) {
		result.add(path);
	}
	
	@Override
	public void visit(P_ReverseLink path) {
		result.add(path);
	}
	
	@Override
	public void visit(P_Seq pathSeq) {
		pathSeq.getLeft().visit(this);
		pathSeq.getRight().visit(this);
	}
	
	public static Path toPath(List<P_Path0> steps) {
		return ExprUtils.<Path>opifyBalanced(steps, (a, b) -> new P_Seq(a, b));
		//return ExprUtils.opifyBalanced(steps, P_Seq::new);
	}
	
	public static List<P_Path0> toList(Path path) {
		PathVisitorToList visitor = new PathVisitorToList();
		path.visit(visitor);
		List<P_Path0> result = visitor.getResult();
		return result;
	}
	
	public static int countForwardLinks(Iterable<? extends Path> paths) {
		int result = (int)Streams.stream(paths)
			.filter(p -> p instanceof P_Path0 ? ((P_Path0)p).isForward() : false)
			.count();
		return result;
	}

	public static int countReverseLinks(Iterable<? extends Path> paths) {
		int result = (int)Streams.stream(paths)
				.filter(p -> p instanceof P_Path0 ? !((P_Path0)p).isForward() : false)
				.count();
		return result;		
	}

}
