package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Collection;
import java.util.function.Predicate;

import org.aksw.commons.collections.FilteringCollection;

import com.google.common.collect.Collections2;


public class FilteredObservableCollection<T>
	extends FilteringCollection<T, ObservableCollection<T>>
	implements ObservableCollection<T>
	//extends ObservableCollectionBase<T, Collection<T>>
{
	public FilteredObservableCollection(ObservableCollection<T> backend, Predicate<? super T> predicate) {
		super(backend, predicate);
	}
	
//	@Override
//	public Collection<T> getBackend() {
//		return (ObservableCollection<T>)super.getBackend();
//	}

    @SuppressWarnings("unchecked")
	public static <T> CollectionChangedEventImpl<T> filter(Object self, CollectionChangedEventImpl<T> event, Predicate<? super T> predicate) {
		return new CollectionChangedEventImpl<>(self,
    			Collections2.filter((Collection<T>)event.getOldValue(), predicate::test),
    			Collections2.filter((Collection<T>)event.getNewValue(), predicate::test),
    			Collections2.filter((Collection<T>)event.getAdditions(), predicate::test),
    			Collections2.filter((Collection<T>)event.getDeletions(), predicate::test),
    			Collections2.filter((Collection<T>)event.getRefreshes(), predicate::test)
    	);
    }
    
    
    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        return getBackend().addVetoableChangeListener(event -> {
            CollectionChangedEventImpl<T> newEv = filter(this, (CollectionChangedEventImpl<T>)event, predicate);
            if (newEv.hasChanges()) {
                listener.vetoableChange(newEv);
            }
        });
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        return getBackend().addPropertyChangeListener(event -> {
            CollectionChangedEventImpl<T> newEv = filter(this, (CollectionChangedEventImpl<T>)event, predicate);
            if (newEv.hasChanges()) {
                listener.propertyChange(newEv);
            }
        });
    }    
}

