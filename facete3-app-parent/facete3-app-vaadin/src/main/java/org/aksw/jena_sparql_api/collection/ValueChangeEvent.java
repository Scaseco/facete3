package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;

public class ValueChangeEvent<T>
	extends PropertyChangeEvent
{
	private static final long serialVersionUID = 0L;

	public ValueChangeEvent(Object source, String propertyName, T oldValue, T newValue) {
		super(source, propertyName, oldValue, newValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getNewValue() {
		return (T)super.getNewValue();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getOldValue() {
		return (T)super.getOldValue();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ValueChangeEvent<T> adapt(PropertyChangeEvent pce) {
		return new ValueChangeEvent<>(
				pce.getSource(),
				pce.getPropertyName(),
				(T)pce.getOldValue(),
				(T)pce.getNewValue());
	}
}
