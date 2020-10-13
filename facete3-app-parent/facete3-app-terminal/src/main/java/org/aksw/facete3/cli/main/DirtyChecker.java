package org.aksw.facete3.cli.main;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.accessors.SingleValuedAccessor;

/**
 * Not used; just a concept.
 * Idea for an api to allow for something similar to angularjs' dirty checking
 * - Have a DirtyChecker class to manage watch registrations
 * - There can be a public (convenience) API for type safety, which provides registration method
 *  for each number of arguments - up to some fixed limit
 *  Internally, we don't care about the types, so we can cast everything to object
 * - We can create Proxies that watch getters / setters of objects and invoke dirty checking
 * 
 * @param <S1>
 * @param s1
 * @param action
 * @return
 */
class DirtyChecker {
	
	
	public static <S1> Runnable watch(Supplier<S1> s1, Consumer<? super S1> action) {
		return null;
	}
	
	
	/**
	 * Bind a computation to a getter/setter
	 * 
	 * @param target
	 * @param s1
	 * @param fn
	 * @return
	 */
	public static <T, S1> Runnable bind(
			SingleValuedAccessor<T> target,
			Supplier<S1> s1, 
			Function<? super S1, ? extends T> fn) {
		return null;
	}
	
	public static <T, S1, S2> Runnable bind(
			SingleValuedAccessor<T> target,
			Supplier<S1> s1, Supplier<S2> s2,
			BiFunction<? super S1, ? super S2, ? extends T> fn) {
		return null;
	}
	
}