package org.aksw.jena_sparql_api.changeset.util;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.changeset.CS;
import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.aksw.jena_sparql_api.changeset.api.RdfStatement;
import org.aksw.jena_sparql_api.changeset.ex.api.CSEX;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroup;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.Delta;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDF;

public class ChangeSetUtils {
	/**
	 * Find all changesets not appearing as a preceding changeset of another one.
	 * 
	 * @param model
	 * @return
	 */
	public static ChangeSetGroup getLatestChangeSetGroup(Model model) {
		UnaryRelation concept = new Concept(
				ElementUtils.createElementGroup(
						ElementUtils.createElementTriple(Vars.s, RDF.type.asNode(), CSEX.ChangeSetGroup.asNode()),
						new ElementFilter(new E_NotExists(ElementUtils.createElementTriple(Vars.x, CSEX.precedingChangeSetGroup.asNode(), Vars.s)))),
				Vars.s);
		
		List<Node> items = ServiceUtils.fetchList(FluentQueryExecutionFactory.from(model).create(), concept);
		if(items.size() > 1) {
			throw new RuntimeException("Multiple changeset groups");
		}

		ChangeSetGroup tmp = items.isEmpty()
				? null
				: ModelUtils.convertGraphNodeToRDFNode(items.get(0), model).asResource().as(ChangeSetGroup.class);

		return tmp;
	}
	
	
	
    public static void writeReifiedStatement(Resource s, Statement stmt) {
        s
        	.addProperty(RDF.subject, stmt.getSubject())
        	.addProperty(RDF.predicate, stmt.getPredicate())
        	.addProperty(RDF.object, stmt.getObject());
    }

    
    public static RdfStatement createReifiedStatement(Model model, Statement stmt) {
		RdfStatement stmtRes = model.createResource().as(RdfStatement.class);
		writeReifiedStatement(stmtRes, stmt);

		return stmtRes;
    }
    
	public static void trackChangesInTxn(Model model, Consumer<Model> action) {
		
		Runnable a = () -> {
			Graph graph = model.getGraph();
			Delta delta = new Delta(graph);
			Model deltaModel = ModelFactory.createModelForGraph(delta);

			action.accept(deltaModel);

			Graph addGraph = delta.getAdditions();
			Graph delGraph = delta.getDeletions();

			Set<Node> ss = Sets.union(
					GraphUtil.listSubjects(addGraph, Node.ANY, Node.ANY).toSet(),
					GraphUtil.listSubjects(delGraph, Node.ANY, Node.ANY).toSet());
			
			ChangeSetGroup precedingCsg = getLatestChangeSetGroup(model);
			
			ChangeSetGroup csg = model.createResource().as(ChangeSetGroup.class);
			csg.addProperty(RDF.type, CSEX.ChangeSetGroup);
			csg.setPrecedingChangeSetGroup(precedingCsg);
			
			for(Node s : ss) {
				// TODO Find the last changeset for that resource
				// ?s cs:subjectOfChange <s>
				// NOT EXISTS { ?x cs:preceedingSubject ?s }
				
				UnaryRelation concept = new Concept(
						ElementUtils.createElementGroup(
								ElementUtils.createElementTriple(Vars.s, CS.subjectOfChange.asNode(), s),
								new ElementFilter(new E_NotExists(ElementUtils.createElementTriple(Vars.x, CS.precedingChangeSet.asNode(), Vars.s)))),
						Vars.s);
				
				// Note: Raise an exception if there are multiple changesets
				//new DataQueryImpl<Resource>(conn, concept, null, Resource.class).
				List<Node> precedingChangeSets = ServiceUtils.fetchList(FluentQueryExecutionFactory.from(model).create(), concept);
				if(precedingChangeSets.size() > 1) {
					throw new RuntimeException("Multiple changesets for " + s);
				}
				
				Resource tmp = precedingChangeSets.isEmpty()
						? null
						: ModelUtils.convertGraphNodeToRDFNode(precedingChangeSets.get(0), model).asResource();
				
				ChangeSet cs = model.createResource().as(ChangeSet.class);				
				csg.members().add(cs);
				
				cs.setPrecedingChangeSet(tmp);
				cs.setSubjectOfChange(ModelUtils.convertGraphNodeToRDFNode(s, model).asResource());
				
				List<Statement> addStmts = addGraph.find(s, Node.ANY, Node.ANY)
						.mapWith(t -> ModelUtils.tripleToStatement(model, t))
						.toList();

				List<Statement> delStmts = delGraph.find(s, Node.ANY, Node.ANY)
						.mapWith(t -> ModelUtils.tripleToStatement(model, t))
						.toList();

				
				for(Statement stmt : addStmts) {
					RdfStatement r = createReifiedStatement(model, stmt);
					cs.additions().add(r);				
				}

				for(Statement stmt : delStmts) {
					RdfStatement r = createReifiedStatement(model, stmt);
					cs.removals().add(r);				
				}
				
				model.add(addStmts);
				model.remove(delStmts);
			}			
		};
		
		if(model.supportsTransactions()) {
			model.executeInTxn(a);			
		} else {
			a.run();
		}
	}
}
