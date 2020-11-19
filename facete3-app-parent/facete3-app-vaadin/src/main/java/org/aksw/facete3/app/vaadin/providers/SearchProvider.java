package org.aksw.facete3.app.vaadin.providers;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;

public interface SearchProvider {
//    Resource getMetadata();
    RDFNodeSpec search(String searchString);
}
