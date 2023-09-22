package org.aksw.facete3.app.vaadin;

import org.apache.jena.JenaRuntime;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
@EnableAsync // for long running tasks, see https://vaadin.com/docs/latest/advanced/long-running-tasks
public class MainAppFacete3Vaadin extends SpringBootServletInitializer {

//    static { JenaSystem.init(); }

    public static void main(String[] args) {
        JenaRuntime.isRDF11 = false;

        // Interestingly wrapping the cxt in a try-with-resources block to ensure
        // auto-closing of it causes application start up to fail -
        // probably this is due to the app running in a separate
        // thread
        ConfigurableApplicationContext cxt = new SpringApplicationBuilder()
                .bannerMode(Mode.OFF)
                .sources(MainAppFacete3Vaadin.class)
                .run(args);
    }
}
