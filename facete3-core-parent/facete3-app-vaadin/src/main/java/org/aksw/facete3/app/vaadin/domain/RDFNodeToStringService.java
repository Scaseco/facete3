package org.aksw.facete3.app.vaadin.domain;

import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.rdf.model.RDFNode;

public interface RDFNodeToStringService
    extends LookupService<RDFNode, String>
{
}
