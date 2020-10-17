package org.aksw.facete3.app.vaadin;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;

public class MainTestCodeSnippets {

//    static { JenaSystem.init(); }

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");

        try (ConfigurableApplicationContext cxt = new SpringApplicationBuilder()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("spring.devtools.restart.enabled", false)
                        .build())
                .headless(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Mode.OFF)
                .sources(ConfigEndpoint.class)
                .run()) {

            cxt.getBean(Runnable.class).run();

            DataRefSparqlEndpoint dataRef = cxt.getBean(DataRefSparqlEndpoint.class);
            dataRef.setServiceUrl("http://cord19.aksw.org/sparql");

            System.out.println("Refreshing...");
            cxt.getBean(RefreshScope.class).refreshAll();

            cxt.getBean(Runnable.class).run();
        }

    }
}
