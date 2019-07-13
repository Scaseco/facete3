package org.aksw.jena_sparql_api.data_query.impl;

import java.util.function.Supplier;

import org.aksw.facete.v3.api.path.RelationletNested;
import org.aksw.facete.v3.api.path.VarRefStatic;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

public class NodeTransformPathletVarRefResolver
	implements NodeTransform
{
	protected RelationletNested relationlet;
	
	public NodeTransformPathletVarRefResolver(RelationletNested relationlet) {
		this.relationlet = relationlet;
	}

	// Substitute the node with reference
	@Override
	public Node apply(Node t) {
		Node result = t;
		if(t instanceof NodeVarRefStaticSupplier) {
			Supplier<VarRefStatic> varRefSupplier = ((NodeVarRefStaticSupplier)t).getValue();
			VarRefStatic varRef = varRefSupplier.get();

			result = relationlet.resolve(varRef);
		}
		
		return result;
	}
}
