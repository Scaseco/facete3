package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.jena_sparql_api.core.connection.RDFConnectionTransform;

public interface SearchSensitiveRDFConnectionTransform {
    RDFConnectionTransform create(RDFNodeSpec rdfNodeSpec);
}
