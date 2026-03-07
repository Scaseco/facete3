package org.aksw.facete3.app.vaadin;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.lumo.Lumo;

import org.apache.jena.sys.JenaSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
@PWA(name = "Facete3 Vaadin Application", shortName = "Facete3",
description = "This is an example Vaadin application.") // , enableInstallPrompt = true)
@StyleSheet(Lumo.COMPACT_STYLESHEET)
@Push(PushMode.AUTOMATIC)
@EnableAsync
// @EnableAsync // for long running tasks, see https://vaadin.com/docs/latest/advanced/long-running-tasks
public class MainAppFacete3Vaadin implements AppShellConfigurator {

    public static void main(String[] args) {
        JenaSystem.init();

        System.setProperty("spring.cloud.config.import-check.enabled", "false");

        // FIXME Spring complains about a cycle in ConfigEndpoint but I haven't figured out where and why
        System.setProperty("spring.main.allow-circular-references", "true");

        SpringApplication.run(MainAppFacete3Vaadin.class, args);
        // JenaRuntime.isRDF11 = false;
        // Interestingly wrapping the cxt in a try-with-resources block to ensure
        // auto-closing of it causes application start up to fail -
        // probably this is due to the app running in a separate
        // thread
//        ConfigurableApplicationContext cxt = new SpringApplicationBuilder()
//                .bannerMode(Mode.OFF)
//                .sources(MainAppFacete3Vaadin.class)
//                .run(args);
    }
}
