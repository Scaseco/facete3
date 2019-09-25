package org.aksw.facete.v3.api.path;

public class Step {
	protected String type;
	protected Object key;
	protected String alias;

	public Step(String type, Object key, String alias) {
		super();
		this.type = type;
		this.key = key;
		this.alias = alias;
	}
	
	public String getType() {
		return type;
	}

	public Object getKey() {
		return key;
	}
	public String getAlias() {
		return alias;
	}
}