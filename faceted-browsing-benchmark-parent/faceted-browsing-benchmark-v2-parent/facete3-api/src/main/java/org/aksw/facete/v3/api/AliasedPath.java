package org.aksw.facete.v3.api;

import java.util.Map.Entry;

import org.apache.jena.sparql.path.P_Path0;

/**
 * An aliased path is a sequence of step-alias pairs.
 * The alias may be null, in which case processors should internally assume a default value.
 * 
 * @author raven
 *
 */
public interface AliasedPath
	extends Iterable<Entry<P_Path0, String>>
{
}
