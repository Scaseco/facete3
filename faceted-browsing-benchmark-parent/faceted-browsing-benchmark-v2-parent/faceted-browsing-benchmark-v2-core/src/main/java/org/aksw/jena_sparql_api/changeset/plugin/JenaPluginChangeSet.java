package org.aksw.jena_sparql_api.changeset.plugin;

import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.aksw.jena_sparql_api.changeset.api.RdfStatement;
import org.aksw.jena_sparql_api.changeset.impl.ChangeSetImpl;
import org.aksw.jena_sparql_api.changeset.impl.RdfStatementImpl;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginChangeSet
	implements JenaSubsystemLifecycle
{
	public void start() {
		init();
	}

	@Override
	public void stop() {
	}

	public static void init() {
		init(BuiltinPersonalities.model);
	}

	public static void init(Personality<RDFNode> p) {
		p.add(ChangeSet.class, new SimpleImplementation(ChangeSetImpl::new));
		p.add(RdfStatement.class, new SimpleImplementation(RdfStatementImpl::new));
	}
}
