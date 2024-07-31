package org.aksw.facete3.app.vaadin.components.sparql.wizard;

import java.io.InputStream;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefSparqlEndpoint;

public class SparqlConnectionIo {
    public void save(SparqlConnectionWizard wizard) {

        // endpoint url and graphs
        DataRefSparqlEndpoint endpointCfg = wizard.getDataRef(true);


        // polyfills
        wizard.getPolyfillClasses();
        // wizard.ge;

        // dataset id

        // types
        wizard.getSelectedTypes();

    }

    public void load(InputStream in) {
    }
}
