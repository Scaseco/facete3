package org.aksw.vaadin.datashape.form;


/**
 * Interface for controlling a (compound) component's life cycle after it has been created.
 * 
 * Allows re-locating, refreshing and closing the component.
 * 
 * @author raven
 *
 * @param <C>
 */
public interface ComponentControl<C>
	extends AutoCloseable
{
	void detach();
	void attach(C target);
	void refresh();
	
	@Override
	void close();
}

