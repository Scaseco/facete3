package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;

import org.aksw.commons.accessors.SingleValuedAccessor;

import com.google.common.base.Converter;

public interface ObservableValue<T>
    extends SingleValuedAccessor<T>
{
    Runnable addPropertyChangeListener(PropertyChangeListener listener);


    default <X> ObservableValue<X> convert(Converter<T, X> converter) {
        return new ObservableConvertingValue<>(this, converter);
    }
}
