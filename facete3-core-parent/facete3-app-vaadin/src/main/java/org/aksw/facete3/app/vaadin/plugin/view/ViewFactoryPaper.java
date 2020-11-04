package org.aksw.facete3.app.vaadin.plugin.view;

import java.util.Collections;
import java.util.List;

import org.aksw.facete3.app.shared.viewselector.ViewTemplate;
import org.aksw.facete3.app.shared.viewselector.ViewTemplateImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rx.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class ViewFactoryPaper
    implements ViewFactory
{
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
                    .createResource("http://cord19.aksw.org/view/paper")
                    .addLiteral(RDFS.label, "Paper"),

                // The condition for which sete of resources the view is applicable
                Concept.parse("?s { ?s <http://id.loc.gov/ontologies/bibframe/title> ?o } "),

                // The entity-centric construct query for what information to fetch when applying the view
                attrQuery
                );

    }


    @Override
    public Component createComponent(Resource initialData) {
        PaperViewComponent result = new PaperViewComponent(initialData);
        return result;
    }
}


class PaperViewComponent
    extends VerticalLayout
{
    protected Label authors;
    protected Anchor titleLink;

    protected HorizontalLayout summaryWrapper;
    protected Paragraph summaryContent;
    protected Button showMoreBtn;

    protected Resource state;

    public PaperViewComponent(Resource initialState) {
        this.state = initialState;
        authors = new Label();
        titleLink = new Anchor();
        summaryContent = new Paragraph();
        summaryWrapper = new HorizontalLayout();
        showMoreBtn = new Button("More ...");

        summaryContent.setWidthFull();

        summaryWrapper.setWidthFull();
        summaryWrapper.setHeightFull();

        summaryWrapper.add(summaryContent);
        summaryWrapper.add(showMoreBtn);

        showMoreBtn.addClickListener(event -> {

        });


        add(titleLink);
        add(authors);
        add(summaryWrapper);

        setWidthFull();
        display();
    }

    public void setState(Resource state) {
        this.state = state;
        display();
    }


    public void display() {

        String xtitle = ResourceUtils.tryGetLiteralPropertyValue(state, Bibframe.title, String.class).orElse("");
        String xdoi = ResourceUtils.tryGetLiteralPropertyValue(state, Bibframe.identifiedBy, String.class).orElse("");
        String xsummary = ResourceUtils.tryGetLiteralPropertyValue(state, Bibframe.summary, String.class).orElse("");
        String xcreator = ResourceUtils.tryGetLiteralPropertyValue(state, DCTerms.creator, String.class).orElse("");

        titleLink.setText(xtitle);
        authors.setText(xcreator);
        summaryContent.setText(xsummary);
    }
}