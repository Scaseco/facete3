package org.aksw.facete.v3.impl;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFactory;

public class RDFConnectionBuilder<T, P> {
	protected P parent;
	protected T connection;
	
	public RDFConnectionBuilder(P parent) {
		super();
		this.parent = parent;
	}

	public RDFConnectionBuilder<T, P> defaultModel() {
		setSource(ModelFactory.createDefaultModel());
		return this;
	}
	
	public RDFConnectionBuilder<T, P> setSource(Model model) {
		setSource(DatasetFactory.wrap(model));
		return this;
	}

	@SuppressWarnings("unchecked")
	public RDFConnectionBuilder<T, P> setSource(Dataset dataset) {
		connection = (T)RDFConnectionFactory.connect(dataset);
		
		return this;
	}
	
	public RDFConnectionBuilder<T, P> setSource(T connection) {
		this.connection = connection;
		return this;
	}

	public T getConnection() {
		return connection;
	}
	
	public P end() {
		return parent;
	}
}