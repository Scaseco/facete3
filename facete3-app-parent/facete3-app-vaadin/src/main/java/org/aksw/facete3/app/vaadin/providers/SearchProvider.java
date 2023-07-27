package org.aksw.facete3.app.vaadin.providers;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;

/** A search provider turns a string into a specification for matching resources */
public interface SearchProvider {
//    Resource getMetadata();
    RDFNodeSpec search(String searchString);
}
