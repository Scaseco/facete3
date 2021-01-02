package org.aksw.vaadin.datashape.form;

import java.util.stream.Stream;

import org.aksw.jena_sparql_api.collection.RdfField;
import org.apache.jena.graph.Node;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;

public class DataProviderFromField
	extends AbstractDataProvider<Node, String>
{
	protected RdfField rdfField;
	
	
	
	public DataProviderFromField(RdfField rdfField) {
		super();
		this.rdfField = rdfField;
		
		rdfField.getAddedAsSet().addPropertyChangeListener(event -> {
			System.out.println("Refreshing field");
			this.refreshAll();
		});
	}

	@Override
	public boolean isInMemory() {
		return true;
	}

	@Override
	public int size(Query<Node, String> query) {
		return rdfField.getAddedAsSet().size();
	}

	@Override
	public Stream<Node> fetch(Query<Node, String> query) {
		return rdfField.getAddedAsSet().stream();
	}

}
