package org.aksw.jena_sparql_api.pathlet;

import org.aksw.facete.v3.api.path.Step;

public class Path
	extends PathBuilder
{
	protected Path parent;
	protected Step step;

	public Path() {
		this(null, null);
	}
	
	public Path(Path parent, Step step) {
		super();
		this.parent = parent;
		this.step = step;
	}
	
	public Path getParent() {
		return parent;
	}

	public Step getStep() {
		return step;
	}

	@Override
	public Path appendStep(Step step) {
		return new Path(this, step);
	}
	
	public static Path newPath() {
		return new Path();
	}
}