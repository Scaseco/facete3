package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;

import org.aksw.commons.accessors.SingleValuedAccessor;

import com.google.common.base.Converter;

public interface ObservableValue<T>
    extends SingleValuedAccessor<T>
{
    Runnable addPropertyChangeListener(PropertyChangeListener listener);

    /** Type-safe variant. Uses {@link #addPropertyChangeListener(PropertyChangeListener)} and casts. */
    default Runnable addValueChangeListener(ValueChangeListener<T> listener) {
    	return addPropertyChangeListener(ev -> listener.propertyChange(ValueChangeEvent.<T>adapt(ev)));
    }

    default <X> ObservableValue<X> convert(Converter<T, X> converter) {
        return new ObservableConvertingValue<>(this, converter);
    }
}
