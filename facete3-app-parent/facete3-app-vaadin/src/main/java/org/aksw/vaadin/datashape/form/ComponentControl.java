package org.aksw.vaadin.datashape.form;

import java.util.Map;
import java.util.function.Function;

/**
 * Interface for controlling a (compound) component's life cycle after it has been created.
 * 
 * Allows re-locating, refreshing and closing the component.
 * 
 * @author raven
 *
 * @param <C>
 */
public interface ComponentControl<T, C>
	extends AutoCloseable
{	
	Map<Object, ComponentControl<?, ?>> getChildren();
	
	void detach();
	void attach(C target);
	void refresh(T state);
	
	@Override
	void close();
	
	
	default <I> ComponentControl<I, C> withAdapter(Function<? super I, ? extends T> converter) {
		return new ComponentControlTransform<>(this, converter);
	}
}

