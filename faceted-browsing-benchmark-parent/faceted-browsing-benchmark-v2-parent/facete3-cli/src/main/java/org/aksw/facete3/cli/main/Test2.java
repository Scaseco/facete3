package org.aksw.facete3.cli.main;

import java.util.Collection;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;

public interface Test2
	extends Resource
{
	@Iri("rdf:type")
	<T> Collection<T> getFoo(Class<T> clazz);
	<T> Iterable<T> getBar(Class<T> clazz);
}