package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionTransform;

public interface SearchSensitiveRDFConnectionTransform {
    RDFConnectionTransform create(RDFNodeSpec rdfNodeSpec);
}
