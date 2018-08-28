package org.aksw.jena_sparql_api.changeset.util;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.changeset.CS;
import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.aksw.jena_sparql_api.changeset.api.RdfStatement;
import org.aksw.jena_sparql_api.changeset.ex.api.CSX;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroup;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroupState;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetState;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
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
//	public static ChangeSetGroup getLatestChangeSetGroup(Model model) {
//		UnaryRelation concept = new Concept(
//				ElementUtils.createElementGroup(
//						ElementUtils.createElementTriple(Vars.s, RDF.type.asNode(), CSX.ChangeSetGroup.asNode()),
//						new ElementFilter(new E_NotExists(ElementUtils.createElementTriple(Vars.x, CSX.precedingChangeSetGroup.asNode(), Vars.s)))),
//				Vars.s);
//		
//		List<Node> items = ServiceUtils.fetchList(FluentQueryExecutionFactory.from(model).create(), concept);
//		if(items.size() > 1) {
//			throw new RuntimeException("Multiple changeset groups");
//		}
//
//		ChangeSetGroup tmp = items.isEmpty()
//				? null
//				: ModelUtils.convertGraphNodeToRDFNode(items.get(0), model).asResource().as(ChangeSetGroup.class);
//
//		return tmp;
//	}

	public static ChangeSetGroup getSuccessor(ChangeSetGroup csg) {
		Model model = csg.getModel();
		UnaryRelation concept = new Concept(
				ElementUtils.createElementTriple(Vars.s, CSX.precedingChangeSetGroup.asNode(), csg.asNode()),
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


	public static ChangeSet getSuccessor(ChangeSet cs) {
		Model model = cs.getModel();
		UnaryRelation concept = new Concept(
				ElementUtils.createElementTriple(Vars.s, CS.precedingChangeSet.asNode(), cs.asNode()),
				Vars.s);
		
		List<Node> items = ServiceUtils.fetchList(FluentQueryExecutionFactory.from(model).create(), concept);
		if(items.size() > 1) {
			throw new RuntimeException("Multiple changeset groups");
		}

		ChangeSet tmp = items.isEmpty()
				? null
				: ModelUtils.convertGraphNodeToRDFNode(items.get(0), model).asResource().as(ChangeSet.class);

		return tmp;
	}

	
    public static void writeReifiedStatement(Resource s, Statement stmt) {
        s
        	.addProperty(RDF.type, RDF.Statement)
        	.addProperty(RDF.subject, stmt.getSubject())
        	.addProperty(RDF.predicate, stmt.getPredicate())
        	.addProperty(RDF.object, stmt.getObject());
    }

    
    public static RdfStatement createReifiedStatement(Model model, Statement stmt) {
		RdfStatement stmtRes = model.createResource().as(RdfStatement.class);
		writeReifiedStatement(stmtRes, stmt);

		return stmtRes;
    }
    
    
    public static void applyUndo(ChangeSet cs, Model targetModel) {
		List<Statement> addStmts = cs.additions().stream().map(RdfStatement::getStatement).collect(Collectors.toList());
		List<Statement> delStmts = cs.removals().stream().map(RdfStatement::getStatement).collect(Collectors.toList());

		targetModel.remove(addStmts);
		targetModel.add(delStmts);
    }
    
    public static void applyRedo(ChangeSet cs, Model targetModel) {
    	List<Statement> addStmts = cs.additions().stream().map(RdfStatement::getStatement).collect(Collectors.toList());
		List<Statement> delStmts = cs.removals().stream().map(RdfStatement::getStatement).collect(Collectors.toList());

		targetModel.add(addStmts);
		targetModel.remove(delStmts);
    }
    
    

    public static void trackAndApplyChanges(Model csgModel, Model dataModel, Delta delta) {
		Graph addGraph = delta.getAdditions();
		Graph delGraph = delta.getDeletions();
		
		trackAndApplyChanges(csgModel, dataModel, addGraph, delGraph);
    }
    
    public static void trackAndApplyChanges(Model csgModel, Model dataModel, Graph addGraph, Graph delGraph) {

    	// Clear any redo information
    	//clearRedoChangeSetGroup(csgModel);
    	ChangeSetGroupManager manager = new ChangeSetGroupManager(csgModel, dataModel);
    	manager.clearRedo();
    	
    	
		Set<Node> ss = Sets.union(
				GraphUtil.listSubjects(addGraph, Node.ANY, Node.ANY).toSet(),
				GraphUtil.listSubjects(delGraph, Node.ANY, Node.ANY).toSet());
		
		ChangeSetGroupState csgState = manager.getState();
		ChangeSetGroup precedingCsg = csgState.getLatestChangeSetGroup();
		
		ChangeSetGroup csg = csgModel.createResource().as(ChangeSetGroup.class);
		csg.addProperty(RDF.type, CSX.ChangeSetGroup);
		csg.setPrecedingChangeSetGroup(precedingCsg);		
		
		csgState.setLatestChangeSetGroup(csg);
		
		for(Node s : ss) {
			// TODO Find the last changeset for that resource
			// ?s cs:subjectOfChange <s>
			// NOT EXISTS { ?x cs:preceedingSubject ?s }
			
//			UnaryRelation concept = new Concept(
//					ElementUtils.createElementGroup(
//							ElementUtils.createElementTriple(Vars.s, CS.subjectOfChange.asNode(), s),
//							new ElementFilter(new E_NotExists(ElementUtils.createElementTriple(Vars.x, CS.precedingChangeSet.asNode(), Vars.s)))),
//					Vars.s);

//			
//			
//			// Note: Raise an exception if there are multiple changesets
//			//new DataQueryImpl<Resource>(conn, concept, null, Resource.class).
//			List<Node> precedingChangeSets = ServiceUtils.fetchList(FluentQueryExecutionFactory.from(dataModel).create(), concept);
//			if(precedingChangeSets.size() > 1) {
//				throw new RuntimeException("Multiple changesets for " + s);
//			}
//			
//			Resource tmp = precedingChangeSets.isEmpty()
//					? null
//					: ModelUtils.convertGraphNodeToRDFNode(precedingChangeSets.get(0), dataModel).asResource();
			

			// TODO Should the changesets themselves become tracked in delta?
			// If we undo a changeset group, one can argue that all related changesets should disappear from the model
			// Conversely, if we retain the changesets, the resource histories (subjectOfChange) would be inconsistent
			// Alternatively, each resource has a latestChangeset attribute
			ChangeSetState css = ModelUtils.convertGraphNodeToRDFNode(s, csgModel).as(ChangeSetState.class);
			//ChangeSetManager csm = new ChangeSetManager(css, dataModel);
			ChangeSet precedingChangeSet = css.getLatestChangeSet();
			
			ChangeSet cs = csgModel.createResource().as(ChangeSet.class);
			csg.members().add(cs);
			
			cs.setPrecedingChangeSet(precedingChangeSet);
			Resource soc = ModelUtils.convertGraphNodeToRDFNode(s, csgModel).asResource();
			cs.setSubjectOfChange(soc);

			css.setLatestChangeSet(cs);
			
			List<Statement> addStmts = addGraph.find(s, Node.ANY, Node.ANY)
					.mapWith(t -> ModelUtils.tripleToStatement(dataModel, t))
					.toList();

			List<Statement> delStmts = delGraph.find(s, Node.ANY, Node.ANY)
					.mapWith(t -> ModelUtils.tripleToStatement(dataModel, t))
					.toList();

			
			for(Statement stmt : addStmts) {
				RdfStatement r = createReifiedStatement(csgModel, stmt);
				cs.additions().add(r);
			}

			for(Statement stmt : delStmts) {
				RdfStatement r = createReifiedStatement(csgModel, stmt);
				cs.removals().add(r);
			}
			
			// end of change set code

			dataModel.add(addStmts);
			dataModel.remove(delStmts);
		}
    }

	public static void trackChangesInTxn(Model model, Consumer<Model> action) {
		trackChangesInTxn(model, model, action);
	}
	
	public static void trackChangesInTxn(Model changeModel, Model dataModel, Consumer<Model> action) {
		
		Runnable a = () -> {
			Graph graph = dataModel.getGraph();
			Delta delta = new Delta(graph);
			Model deltaModel = ModelFactory.createModelForGraph(delta);

			action.accept(deltaModel);

			trackAndApplyChanges(changeModel, dataModel, delta);
		};
		
		if(dataModel.supportsTransactions()) {
			dataModel.executeInTxn(a);			
		} else {
			a.run();
		}
	}
}
