package org.aksw.facete.v3.api;

import java.util.Optional;

/**
 * A convenience interface (or rather trait) to perform inline casts
 * 
 * 
 * @author Claus Stadler, Dec 28, 2018
 *
 */
public interface Castable {
	default <T> T as(Class<T> clazz) {
		boolean canAs = canAs(clazz);
		@SuppressWarnings("unchecked")
		T result = canAs ? (T)this : null;
		return result;
	}
	
	default <T> Optional<T> tryAs(Class<T> clazz) {
		T tmp = as(clazz);
		return Optional.ofNullable(tmp);
	}
	
	default boolean canAs(Class<?> clazz) {
		boolean result = clazz.isAssignableFrom(this.getClass());
		return result;
	}
}
