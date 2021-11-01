package org.aksw.facete3.app.shared.concept;

import java.util.function.Supplier;

import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.facete.v3.experimental.Resolvers;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.aksw.jena_sparql_api.pathlet.Pathlet;
import org.aksw.jena_sparql_api.pathlet.PathletJoinerImpl;
import org.aksw.jena_sparql_api.pathlet.PathletSimple;
import org.aksw.jena_sparql_api.relationlet.RelationletElementImpl;
import org.aksw.jena_sparql_api.relationlet.RelationletSimple;
import org.aksw.jena_sparql_api.relationlet.VarRefStatic;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class TestPathletList {

    public void testBasic() {
        // Set up a base construct query for extension
        Query baseQuery = QueryFactory.create(
                "PREFIX eg: <http://www.example.org/>\n" + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                        + "CONSTRUCT { ?s <http://ex.org/parent> ?o }\n" + "WHERE { ?s eg:test ?o }");

        Resolver resolver = Resolvers.from(baseQuery, Vars.s);

        Path psimple = Path.newPath().fwd(RDF.type);
        Path commonParentPath = Path.newPath().optional().fwd("http://ex.org/parent");

        Path p1 = commonParentPath.fwd(Vars.x, "p1");
        Path p2 = commonParentPath.fwd(Vars.y, "p1");

        PathletJoinerImpl pathlet = new PathletJoinerImpl(resolver);
        // Add the base query to the pathlet, with variable ?s joining with the
        // pathlet's root
        // and ?s also being the connector for subsequent joins
        pathlet.add(new PathletSimple(Vars.s, Vars.s,
                new RelationletElementImpl(baseQuery.getQueryPattern()).pinAllVars()));

        // Now add some paths
        pathlet.resolvePath(psimple);
        pathlet.resolvePath(p1);
        pathlet.resolvePath(p2);
        Supplier<VarRefStatic> ref = pathlet.resolvePath(p2.optional().fwd(RDFS.comment));

        RelationletSimple rn = pathlet.materialize();
        System.out.println("Materialized Element: " + rn.getElement());
        System.out.println("Materialized Vars    : " + rn.getExposedVars());

        System.out.println("Plain VarRef: " + ref.get());
        System.out.println("Resolved VarRef: " + rn.resolve(ref.get()));
    }


//    @Test
//    void testPathletList() {
    public static void main(String[] args) {

        // Set up the base query (construct type) which to extend
        Query baseQuery = QueryFactory.create("CONSTRUCT WHERE { ?s a <urn:Department> ; <urn:employees> ?ln }");
        Var rootVar = Vars.s;

        // In this example we extend the graph pattern such that the actual
        // *list items* of employees is retrieved
        Resolver resolver = Resolvers.from(baseQuery, rootVar);

        // Set up the paths that traverse the base query
        // Note that paths are independent objects (e.g. they do not depend on the base query)
        Path start = Path.newPath().fwd("urn:employees");
        Path ln = start.fwd(PathFactory.pathZeroOrMore1(PathFactory.pathLink(RDF.Nodes.rest)));
        Path f = ln.fwd(RDF.first);
        Path r = ln.fwd(RDF.rest);

        // Set up the join expression builder
        PathletJoinerImpl rootPathlet = new PathletJoinerImpl(resolver);

        // Add the query element and pin all its variables so
        // that they do not become renamed
        // Also, queryPatternPathlet declares rootVar to be both the connection point
        // for pathlets it is connected to as well as for pathlets connected to it
        Pathlet queryPatternPathlet = new PathletSimple(rootVar, rootVar, new RelationletElementImpl(baseQuery.getQueryPattern()).pinAllVars());

        rootPathlet.add(queryPatternPathlet);

        rootPathlet.resolvePath(ln);
        Supplier<VarRefStatic> firstVarRef = rootPathlet.resolvePath(f);
        Supplier<VarRefStatic> restVarRef = rootPathlet.resolvePath(r);

        // Materialize the join
        RelationletSimple rn = rootPathlet.materialize();
        System.out.println("Materialized Element: " + rn.getElement());

        // Get the variables that correspond to the first/rest paths
        Var firstVar = rn.resolve(firstVarRef.get());
        Var restVar = rn.resolve(restVarRef.get());

        System.out.println("firstVar: " + firstVar);
        System.out.println("restVar: " + restVar);

        /*
Expected output:

Materialized Element: { ?s  a                <urn:Department> ;
      <urn:employees>  ?ln .
  ?ln (<http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>)* ?v_3 .
  ?v_3  <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>  ?o .
  ?v_3  <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>  ?v_2
}
firstVar: ?o
restVar: ?v_2
         */
    }
}
