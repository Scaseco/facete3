package org.aksw.facete.v3.impl;

import org.aksw.jena_sparql_api.mapper.jpa.criteria.CriteriaQueryImpl;

public class RdfCriteriaQuery<T>
	extends CriteriaQueryImpl<T>
{
	public RdfCriteriaQuery(Class<T> resultType) {
		super(resultType);
	}
    
}
