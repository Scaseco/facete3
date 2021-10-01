package org.aksw.facete3.app.vaadin;

import org.aksw.jena_sparql_api.path.core.PathNode;
import org.aksw.jena_sparql_api.path.core.PathOpsNode;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePathNode;
import org.apache.jena.graph.Node;
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

//    static { JenaSystem.init(); }

    public static void main(String[] args) {

        PathNode a = PathOpsNode.get().fromString("/<http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04> \"<http://www.w3.org/ns/dcat#distribution>\"^^<http://jsa.aksw.org/dt/sparql/path> _:BB6a59d0a7XX2D8cc7XX2D4394XX2Db07cXX2D3c0e4df72a96");
        PathNode b = PathOpsNode.get().fromString("/<http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04> \"<http://www.w3.org/ns/dcat#distribution>\"^^<http://jsa.aksw.org/dt/sparql/path> _:BB6a59d0a7XX2D8cc7XX2D4394XX2Db07cXX2D3c0e4df72a96");

        Node x = RDFDatatypePathNode.createNode(a);
        Node y = RDFDatatypePathNode.createNode(b);


        System.out.println("path test: " + a.equals(b));
        System.out.println("path test as node: " + x.equals(y));
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
