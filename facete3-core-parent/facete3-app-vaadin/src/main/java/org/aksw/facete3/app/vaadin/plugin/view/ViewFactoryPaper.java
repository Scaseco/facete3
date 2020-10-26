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
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.DCTerms;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;


class Bibframe {
    // TODO: move this part to config and init it there  !!
    public static final Property title = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/title");
    public static final Property identifiedBy = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/identifiedBy");
    public static final Property summary = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/summary");
    // public static final Property creator = ResourceFactory.createProperty("http://purl.org/dc/terms/creator");

}

public class ViewFactoryPaper
    implements ViewFactory
{
    @Override
    public ViewTemplate getViewTemplate() {

        // FIXME The view template should be static

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
                ModelFactory.createDefaultModel().createResource("http://cord19.aksw.org/view/paper"),

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
    protected TextArea summary;

    protected Resource state;

    public PaperViewComponent(Resource initialState) {
        this.state = initialState;
        authors = new Label();
        titleLink = new Anchor();
        summary = new TextArea();
        summary.setWidthFull();
        add(titleLink);
        add(authors);
        add(summary);

        setWidthFull();
        display();
    }

    public void setState(Resource state) {
        this.state = state;
        display();
    }


    public void display() {

        String xtitle = ResourceUtils.getLiteralPropertyValue(state, Bibframe.title, String.class);
        String xdoi = ResourceUtils.getLiteralPropertyValue(state, Bibframe.identifiedBy, String.class);
        String xsummary = ResourceUtils.getLiteralPropertyValue(state, Bibframe.summary, String.class);
        String xcreator = ResourceUtils.getLiteralPropertyValue(state, DCTerms.creator, String.class);

        titleLink.setText(xtitle);
        authors.setText(xcreator);
        summary.setValue(xsummary);
    }
}