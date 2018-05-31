package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction0;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

public class ExprPath<P>
	extends ExprFunction0
{
	protected P path;
	
	public ExprPath(P path) {
		super("path");
		this.path = path;
	}
	
	public P getPath() {
		return path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Expr obj, boolean bySyntax) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExprPath other = (ExprPath) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public NodeValue eval(FunctionEnv env) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Expr copy() {
		return new ExprPath(path);
	}
}
