package org.aksw.facete.v3.api;

public interface Castable {
	default <T extends FacetNode> T as(Class<T> clazz) {
		@SuppressWarnings("unchecked")
		T result = clazz.isAssignableFrom(this.getClass()) ? (T)this : null;
		return result;
	}
}
