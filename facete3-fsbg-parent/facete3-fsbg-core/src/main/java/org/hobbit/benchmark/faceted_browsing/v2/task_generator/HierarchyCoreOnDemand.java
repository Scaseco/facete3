package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementNotExists;


public class HierarchyCoreOnDemand
    implements HierarchyCore
{
    public static final Var root = Var.alloc("root");
    public static final Var parent = Var.alloc("parent");
    public static final Var ancestor = Var.alloc("ancestor");


    protected Path path;


    public HierarchyCoreOnDemand(Path path) {
        super();
        this.path = path;
    }

    @Override
    public Fragment1 roots() {
        Fragment1 result = createConceptForRoots(path);
        return result;
    }

    /**
     * Below is wrong:
     *
     * Roots are all nodes having no ancestor without parents
     * Reason for incorrectness: Consider a cycle "childCycle" which is connected to another cycle "rootCycle".
     * All members of "childCycle" would be incorrectly classified as roots.
     *
     *
     * <pre>
     * {@code
     * SELECT DISTINCT ?root {
     *   [] <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?root
     *   FILTER(NOT EXISTS { ?root (<http://www.w3.org/2000/01/rdf-schema#subClassOf>)+ ?ancestor . FILTER(NOT EXISTS {?ancestor <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent }) . })\n" +
     * }
     * }
     * </pre>
     *
     * This means that every node in a cycle which does not have any parent outside of that cycle will become a root.
     */


    /**
     *
     * Roots (of a path) are all nodes for which all parents occur as descendents - a node without a parent trivially satisfies this condition.
     * Conversely: root nodes are all nodes for which there is no parent that does not occur as a descendent
     * SELECT DISTINCT ?root {
     *   [] <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?root
     *   FILTER(NOT EXISTS { ?root <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . FILTER(NOT EXISTS {?parent (<http://www.w3.org/2000/01/rdf-schema#subClassOf>)+ ?root }) . })\n" +
     * }
     *
     * Note, that the roots for a given concept and a path is
     *   { { [] path ?root } UNION { concept(?root) } FILTER(NOT EXISTS ....) }
     * i.e. nodes that are not part of the hierarchy become roots
     */
    public static Fragment1 createConceptForRoots(Path path) {
        Element e = ElementUtils.createElementGroup(
            ElementUtils.createElement(new TriplePath(NodeFactory.createBlankNode(), path, root)),
            new ElementFilter(new E_NotExists(
                ElementUtils.createElementGroup(
                    ElementUtils.createElement(new TriplePath(root, path, parent)),
                    new ElementFilter(new E_NotExists(ElementUtils.createElementGroup(ElementUtils.createElement(new TriplePath(parent, PathFactory.pathOneOrMore1(path), root)))))
        ))));

//		System.out.println(e);

        Fragment1 result = new Concept(e, root);
        return result;
    }


    public static Fragment1 createConceptForDirectlyRelatedItems(Fragment1 baseConcept, Path relation) {
        Fragment1 targets = new Concept(ElementUtils.createElement(new TriplePath(Vars.s, relation, Vars.o)), Vars.s);

        Fragment1 result = targets
                .joinOn(Vars.o)
                .filterRelationFirst(true)
                .with(baseConcept)
                .toFragment1();

        return result;
    }

    /**
     * r: related
     * dr: directly related
     *
     * dr(a, b) :- r(a, b) and not exists x with (a, x), (x, b) with x not in [a, b]
     *
     * @param path
     * @return
     */
    public static Fragment2 createRelationForStrictDirectRelation(Path path) {
        Fragment2 result = new Fragment2Impl(ElementUtils.groupIfNeeded(
                // Result is all ?s path ?o ...
                ElementUtils.createElementPath(Vars.s, path, Vars.o),
                // where there is no ?x in between
                new ElementNotExists(ElementUtils.groupIfNeeded(
                        ElementUtils.createElementPath(Vars.s, path, Vars.x),
                        ElementUtils.createElementPath(Vars.x, path, Vars.o),
                        new ElementFilter(ExprUtils.notOneOf(Vars.x, Vars.s, Vars.o))
                )),
                // and the subclass is not a parent
                new ElementNotExists(ElementUtils.groupIfNeeded(
                    ElementUtils.createElementPath(Vars.o, new P_ZeroOrMore1(path), Vars.s))
                )),
                Vars.s,
                Vars.o);
        return result;
    }

    public static Fragment1 createConceptForDirectlyRelatedItems(Fragment1 baseConcept, Path path, Fragment1 availableValues) {
        return createConceptForDirectlyRelatedItems(baseConcept, path, availableValues, true);
    }

    public static Fragment1 createConceptForDirectlyRelatedItems(Fragment1 baseConcept, Path path, Fragment1 availableValues, boolean directOnly) {
        Fragment2 br;
        if (directOnly) {
            br = createRelationForStrictDirectRelation(path);
        } else {
            br = Fragment2Impl.create(path);
        }

        Fragment1 result = br
            .joinOn(br.getTargetVar())
            .with(baseConcept)
            .joinOn(br.getSourceVar())
            .with(availableValues)
            .project(br.getSourceVar())
            .toFragment1();

        return result;
    }

    /**
     * If any child is part of a cycle, all members of the cycle become children
     *
     *
     * -> so effective children are all direct children that did not already appear as an ancestor
     */
    @Override
    public Fragment1 children(Fragment1 nodes) {
        throw new NotImplementedException();
    }

    @Override
    public Fragment1 parents(Fragment1 nodes) {
        throw new NotImplementedException();
    }

    @Override
    public Fragment1 descendents() {
        throw new NotImplementedException();
    }

    @Override
    public Fragment1 ancestors() {
        throw new NotImplementedException();
    }
}