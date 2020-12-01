package org.aksw.jena_sparql_api.collection;

import java.util.Collection;
import java.util.Collections;

public class CollectionChangedEventImpl<T>
    extends CollectionChangedEvent<T>
{
    protected Collection<T> additions;
    protected Collection<T> deletions;
    protected Collection<T> refreshes;

//    public CollectionChangedEvent(Object source, String propertyName, Object oldValue, Object newValue) {
//        super(source, propertyName, oldValue, newValue);
//    }

    public CollectionChangedEventImpl(Object source,
            Collection<T> oldValue,
            Collection<T> newValue,

            Collection<T> additions,
            Collection<T> deletions,
            Collection<T> refreshes) {
        super(source, "items", oldValue, newValue);
        this.additions = additions == null ? Collections.emptySet() : additions;
        this.deletions = deletions == null ? Collections.emptySet() : deletions;
        this.refreshes = refreshes == null ? Collections.emptySet() : refreshes;
    }

    public Collection<T> getAdditions() {
        return additions;
    }

    public Collection<T> getDeletions() {
        return deletions;
    }

    public Collection<T> getRefreshes() {
        return refreshes;
    }

    @Override
    public String toString() {
        return "CollectionChangedEventImpl [additions=" + additions + ", deletions=" + deletions + ", refreshes="
                + refreshes + "]";
    }
}
