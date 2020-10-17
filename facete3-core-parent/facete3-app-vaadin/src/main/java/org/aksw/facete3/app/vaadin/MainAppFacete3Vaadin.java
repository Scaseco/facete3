package org.aksw.facete3.app.vaadin;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
public class MainAppFacete3Vaadin extends SpringBootServletInitializer {

    public static void main(String[] args) {

        // Interestingly wrapping the cxt in a try-with-resources block to ensure
        // auto-closing of it causes application start up to fail -
        // probably this is due to the app running in separate
        // thread
        ConfigurableApplicationContext cxt = new SpringApplicationBuilder()
                .sources(ConfigCord19.class)
                .sources(MainAppFacete3Vaadin.class)
                .run(args);
    }

}
