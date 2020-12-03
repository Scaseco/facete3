package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.util.Map;

public interface ObservableMap<K, V>
    extends Map<K, V>
{
    Runnable addListener(PropertyChangeListener listener);
}

