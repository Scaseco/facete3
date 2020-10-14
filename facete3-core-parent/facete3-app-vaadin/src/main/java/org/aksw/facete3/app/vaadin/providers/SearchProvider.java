package org.aksw.facete3.app.vaadin.providers;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;

public interface SearchProvider {
    RDFNodeSpec search(String searchString);
}
