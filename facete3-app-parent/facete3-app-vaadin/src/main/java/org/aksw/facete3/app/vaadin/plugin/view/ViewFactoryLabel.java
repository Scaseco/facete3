package org.aksw.facete3.app.vaadin.plugin.view;

import java.util.Collections;
import java.util.List;

import org.aksw.facete3.app.shared.viewselector.ViewTemplate;
import org.aksw.facete3.app.shared.viewselector.ViewTemplateImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.rx.entity.model.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jenax.arq.aggregation.BestLiteralConfig;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.RDFS;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;


public class ViewFactoryLabel
    implements ViewFactory
{
    @Override
    public ViewTemplate getViewTemplate() {

        EntityQueryImpl attrQuery = new EntityQueryImpl();

        Node p = RDFS.Nodes.label;
        List<Var> vars = Collections.singletonList(Vars.s);
        EntityGraphFragment fragment = new EntityGraphFragment(
                vars,
                new EntityTemplateImpl(Collections.<Node>singletonList(Vars.s), new Template(
                        BasicPattern.wrap(Collections.singletonList(Triple.create(Vars.s, p, Vars.o))))),
                ElementUtils.createElementTriple(Vars.s, p, Vars.o)
                );

        attrQuery.getOptionalJoins().add(new GraphPartitionJoin(fragment));

        return new ViewTemplateImpl(
                // The id + metadata of the view
                ModelFactory.createDefaultModel()
                    .createResource("http://cord19.aksw.org/view/label")
                    .addLiteral(RDFS.label, "Label"),

                // The condition for which sete of resources the view is applicable
                Concept.parse("?s { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o } "),

                // The entity-centric construct query for what information to fetch when applying the view
                attrQuery
                );

    }

    @Override
    public Component createComponent(Resource initialData) {
        ViewComponentLabel result = new ViewComponentLabel(initialData);
        return result;
    }
}


class ViewComponentLabel
    extends Span
{
    protected Resource state;

    public ViewComponentLabel(Resource initialState) {
        this.state = initialState;
        setWidthFull();
        display();
    }

    public void setState(Resource state) {
        this.state = state;
        display();
    }

    public void display() {
        BestLiteralConfig bestLiteralCfg = BestLiteralConfig.fromProperty(RDFS.label);
        String xlabel = state == null ? null : LabelUtils.getOrDeriveLabel(state, bestLiteralCfg);

        setText(xlabel);
    }
}