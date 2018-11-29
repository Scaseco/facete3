package org.aksw.facete.v3.api;

public interface Castable {
	default <T> T as(Class<T> clazz) {
		@SuppressWarnings("unchecked")
		T result = clazz.isAssignableFrom(this.getClass()) ? (T)this : null;
		return result;
	}
}
