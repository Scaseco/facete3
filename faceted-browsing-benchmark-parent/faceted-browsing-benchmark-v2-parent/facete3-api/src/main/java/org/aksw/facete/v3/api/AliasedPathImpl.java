package org.aksw.facete.v3.api;

import java.util.Collections;
import java.util.List;

/**
 * An aliased path is a sequence of step-alias pairs.
 * The alias may be null, in which case processors should internally assume a default value.
 * 
 * @author raven
 *
 */
public class AliasedPathImpl
	extends PathListBase<AliasedPath, AliasedPathStep>
	implements AliasedPath
{
	public AliasedPathImpl(List<AliasedPathStep> steps) {
		super(steps);
	}

	@Override
	protected AliasedPathImpl create(List<AliasedPathStep> steps) {
		return new AliasedPathImpl(steps);
	}
	
	public static AliasedPath empty() {
		return new AliasedPathImpl(Collections.emptyList());
	}
}
