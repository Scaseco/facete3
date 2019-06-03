package org.aksw.facete.v3.api;

import java.util.List;

public interface PathBase<T extends PathBase<T, S>, S>
{
	List<S> getSteps();
	
	T getParent();
	S getLastStep();
	
	// Create a path by appending a step to this path
	T subPath(S step);	
}
