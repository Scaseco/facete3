package org.aksw.facete3.app.vaadin;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.jena.sys.JenaSystem;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
public class MainAppFacete3Vaadin extends SpringBootServletInitializer {

    static { JenaSystem.init(); }

    public static void main(String[] args) {

//        String urlStr = "https://sci-hub.se/10.1016/j.tetlet.2016.07.001";
//
//        URL url;
//        try {
//            url = new URL(urlStr);
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//
//        String rawHtml;
//        if (true) {
//            try (InputStream in = url.openStream()) {
//                rawHtml = IOUtils.toString(in, StandardCharsets.UTF_8);
//            } catch (IOException e1) {
//                throw new RuntimeException(e1);
//            }
//        } else {
//            rawHtml = "    <div id=\"article\">\n" +
//                    "        <iframe src = \"https://cyber.sci-hub.se/MTAuMTAxNi9qLnRldGxldC4yMDE2LjA3LjAwMQ==/shetty2016.pdf#view=FitH\" id = \"pdf\"></iframe>\n" +
//                    "    </div>\n" +
//                    "";
//        }
//
//        System.out.println(rawHtml);
//
//
//        Pattern pattern = Pattern.compile("<\\s*iframe\\s[^>]*src\\s*=\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
//        Matcher matcher = pattern.matcher(rawHtml);
//        System.out.println("MATCH");
//        if (matcher.find()) {
//            String pdfUrlStr = matcher.group(1);
//            System.out.println(pdfUrlStr);
//        }
//
//        if (true) return;

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
