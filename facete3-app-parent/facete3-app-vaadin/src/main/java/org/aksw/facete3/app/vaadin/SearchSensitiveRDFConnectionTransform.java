package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.jenax.arq.connection.core.RDFConnectionTransform;

public interface SearchSensitiveRDFConnectionTransform {
    RDFConnectionTransform create(RDFNodeSpec rdfNodeSpec);
}
