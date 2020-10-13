package org.aksw.facete3.cli.main;

import org.apache.jena.rdf.model.RDFNode;

public class DefaultTableCellRenderer2Rdf
	extends DefaultTableCellRenderer2<RDFNode>
{
	@Override
	public String toString(RDFNode node, int columnIndex, int rowIndex) {
		String result = MainCliFacete3.toString(node);
		return result;
	}
}
