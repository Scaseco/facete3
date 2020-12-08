package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Sets;


public class ObservableSetUnion<T>
    extends AbstractSet<T>
    implements ObservableSet<T>
{
    protected ObservableSet<T> lhs;
    protected ObservableSet<T> rhs;
    protected Set<T> effectiveSet;

    public ObservableSetUnion(ObservableSet<T> lhs, ObservableSet<T> rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
        this.effectiveSet = Sets.union(lhs, rhs);
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        // FIXME Create a union event - i.e. cross check additions / deletions against the values
        // in the sets:
        // - suppress an added value if it was already present (in the other set)
        // - suppress a deleted value if it is present in the other set

        Runnable a = lhs.addPropertyChangeListener(convertListener(this, rhs, listener));
        Runnable b = rhs.addPropertyChangeListener(convertListener(this, lhs, listener));

        // Return a runnable that deregister both listeners
        return () -> { a.run(); b.run(); };
    }

    @SuppressWarnings("unchecked")
    public static <T> PropertyChangeListener convertListener(
            Object self, Set<T> other,
            PropertyChangeListener listener) {

        return ev -> {
            CollectionChangedEventImpl<T> newEv = convertEvent(self, (CollectionChangedEventImpl<T>)ev, other);
            if (newEv.hasChanges()) {
                listener.propertyChange(newEv);
            }
        };
    }


    protected static <T> Set<T> nullSafeDifference(Set<T> set, Set<T> other) {
        return set == null ? Collections.emptySet() : Sets.difference((Set<T>)set, other);
    }

    @SuppressWarnings("unchecked")
    public static <T> CollectionChangedEventImpl<T> convertEvent(Object self,
            CollectionChangedEventImpl<T> ev, Set<T> other) {

        // Added items that already exist in the 'other' set are substracted
        Set<T> effectiveAdditions = nullSafeDifference((Set<T>)ev.getAdditions(), other);
        // Deletions that already exist in the other set are also substracted
        Set<T> effectiveDeletions = nullSafeDifference((Set<T>)ev.getDeletions(), other);

        return new CollectionChangedEventImpl<T>(
            self, self,
            Sets.union(Sets.difference((Set<T>)self, effectiveDeletions), effectiveAdditions),
            effectiveAdditions,
            effectiveDeletions,
            // Refreshes are just passed through
            nullSafeDifference((Set<T>)ev.getRefreshes(), other));
    }


    @Override
    public Iterator<T> iterator() {
        return effectiveSet.iterator();
    }

    @Override
    public int size() {
        return effectiveSet.size();
    }

    public static <T> ObservableSet<T> create(ObservableSet<T> a, ObservableSet<T> b) {
        return new ObservableSetUnion<>(a, b);
    }

    public static void main(String[] args) {
        ObservableSet<String> a = ObservableSetImpl.decorate(new LinkedHashSet<>());
        ObservableSet<String> b = ObservableSetImpl.decorate(new LinkedHashSet<>());

        ObservableSet<String> c = ObservableSetUnion.create(a, b);

        c.addPropertyChangeListener(ev -> System.out.println(ev));


        a.add("Hello"); // expect addition
        b.add("Hello"); // expect no event

        a.remove("Hello"); // expect no event
        b.remove("Hello"); // expect event

    }
}

