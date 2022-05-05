package org.aksw.facete3.app.vaadin.plugin.view;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.facete3.app.shared.viewselector.ViewTemplate;
import org.aksw.facete3.app.shared.viewselector.ViewTemplateImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rx.entity.model.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.vaadin.alejandro.PdfBrowserViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;


public class ViewFactoryDoiPdfViewer
    implements ViewFactory
{
    public static final Property orkgDoi = ResourceFactory.createProperty("http://orkg.org/orkg/predicate/P26");

    @Override
    public ViewTemplate getViewTemplate() {

        // FIXME The view template should be static;
        // at present each invocation creates a new one

        EntityQueryImpl attrQuery = new EntityQueryImpl();


        /*
         * Unfortunately there is no syntax (yet) for entity-centric sparql;
         * the following is (roughly)
         *
         * ENTITY ?s
         * CONSTRUCT { ?s ?p ?o }
         * WHERE { ?s ?p ?o }
         *
         */
        List<Var> vars = Collections.singletonList(Vars.s);
        EntityGraphFragment fragment = new EntityGraphFragment(
                vars,
                new EntityTemplateImpl(Collections.<Node>singletonList(Vars.s), new Template(
                        BasicPattern.wrap(Collections.singletonList(Triple.create(Vars.s, Vars.p, Vars.o))))),
                ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o)
                );

        attrQuery.getOptionalJoins().add(new GraphPartitionJoin(fragment));

        return new ViewTemplateImpl(
                // The id of the view
                ModelFactory.createDefaultModel()
                    .createResource("http://cord19.aksw.org/view/pdf-from-doi")
                    .addLiteral(RDFS.label, "PDF/Doi"),

                // The condition for which set of resources the view is applicable
                new Concept(ElementUtils.groupIfNeeded(
                        ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o),
                        new ElementFilter(ExprUtils.oneOf(Vars.p,
                                DCTerms.identifier.asNode(), orkgDoi.asNode(), Bibframe.identifiedBy.asNode()))
                        ), Vars.s),

                // The entity-centric construct query for what information to fetch when applying the view
                attrQuery
                );

    }


    @Override
    public Component createComponent(RDFNode initialData) {
        PaperViewComponentDoi result = new PaperViewComponentDoi(initialData.asResource());
        return result;
    }
}


class PaperViewComponentDoi
    extends VerticalLayout
{

//    protected Button showMoreBtn;

    protected Resource state;


    public PaperViewComponentDoi(Resource initialState) {
        this.state = initialState;
        setWidthFull();
        setHeightFull();
        display();
    }

    public void setState(Resource state) {
        this.state = state;
        display();
    }


    public void display() {

        String doi = null;
        for (Property p : Arrays.asList(Bibframe.identifiedBy, ViewFactoryDoiPdfViewer.orkgDoi, DCTerms.identifier)) {
            if (doi == null) {
                doi = ResourceUtils.getLiteralPropertyValue(state, p, String.class);
            }

            if (doi == null) {
                Resource tmp = ResourceUtils.getPropertyValue(state, p, Resource.class);
                if (tmp != null && tmp.isURIResource()) {
                    doi = tmp.getURI();
                }
            }

            if (doi != null) {
                break;
            }
        }


        System.out.println("DOI: " + doi);
        if (doi == null) {
            return;
        }

        if (doi.startsWith("https://doi.org/")) {
            doi = doi.substring("https://doi.org/".length());
        }

        String finalDoi = doi;

        String scihubUrl = "https://sci-hub.se/" + finalDoi;

        Span span = new Span(scihubUrl);
        add(span);
        Button tryLoadPdfBtn = new Button("Try to load");

        tryLoadPdfBtn.addClickListener(ev -> {
            if (false) {
                IFrame iFrame = new IFrame(scihubUrl);
                iFrame.setWidthFull();
                iFrame.setHeightFull();
                add(iFrame);
            } else {
                URL url;
                try {
                    url = new URL(scihubUrl);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }

                byte[] rawBytes;
                try (InputStream in = url.openStream()) {
                    rawBytes = IOUtils.toByteArray(in); //IOUtils.toString(in, StandardCharsets.UTF_8);
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }

                Callable<InputStream> inputStreamSupp;

                if (rawBytes.length >= 4 && rawBytes[0] == '%' && rawBytes[1] == 'P' && rawBytes[2] == 'D' && rawBytes[3] == 'F') {
                    inputStreamSupp = () -> new ByteArrayInputStream(rawBytes); //, StandardCharsets.UTF_8);
                } else {
                    String rawHtml = new String(rawBytes, StandardCharsets.UTF_8);

                    Pattern pattern = Pattern.compile("<\\s*iframe\\s[^>]*src\\s*=\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(rawHtml);
                    if (matcher.find()) {
                        String pdfUrlStr = matcher.group(1);
                        URL pdfUrl;
                        try {
                            pdfUrl = new URL(pdfUrlStr);
                        } catch (Exception e1) {
                            throw new RuntimeException(e1);
                        }

                        inputStreamSupp = () -> pdfUrl.openStream();
                    } else {
                        inputStreamSupp = null;
                    }
                }

                if (inputStreamSupp == null) {
                    System.err.println("Could not obtain input stream");
                    return;
                }

                //Callable<InputStream> finalInputStreamSupp = inputStreamSupp;
                String fileName = finalDoi.replace('/', '-') + ".pdf";

                StreamResource streamResource = new StreamResource(fileName, () -> {
                    try {
                        return inputStreamSupp.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                PdfBrowserViewer pdfViewer = new PdfBrowserViewer(streamResource);
                pdfViewer.setHeight("100%");
                pdfViewer.setWidth("100%");

                add(pdfViewer);
            }
        });


        add(tryLoadPdfBtn);
    }
}