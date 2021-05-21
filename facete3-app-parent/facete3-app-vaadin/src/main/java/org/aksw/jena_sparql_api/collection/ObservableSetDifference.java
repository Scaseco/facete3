package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Sets;


public class ObservableSetDifference<T>
    extends AbstractSet<T>
    implements ObservableSet<T>
{
    protected ObservableSet<T> lhs;
    protected ObservableSet<T> rhs;
    protected Set<T> effectiveSet;

    public ObservableSetDifference(ObservableSet<T> lhs, ObservableSet<T> rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
        this.effectiveSet = Sets.difference(lhs, rhs);
    }

    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        Runnable a = lhs.addVetoableChangeListener(convertVetoableChangeListener(this, rhs, listener));
        Runnable b = rhs.addVetoableChangeListener(convertVetoableChangeListener(this, lhs, listener));

        // Return a runnable that deregister both listeners
        return () -> { a.run(); b.run(); };
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        Runnable a = lhs.addPropertyChangeListener(convertPropertyChangeListener(this, rhs, listener));
        Runnable b = rhs.addPropertyChangeListener(convertPropertyChangeListener(this, lhs, listener));

        // Return a runnable that deregister both listeners
        return () -> { a.run(); b.run(); };
    }

    @SuppressWarnings("unchecked")
    public static <T> VetoableChangeListener convertVetoableChangeListener(
            Object self, Set<T> other,
            VetoableChangeListener listener) {

        return ev -> {
            CollectionChangedEvent<T> newEv = convertEvent(self, (CollectionChangedEvent<T>)ev, other);
            if (newEv.hasChanges()) {
                listener.vetoableChange(newEv);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> PropertyChangeListener convertPropertyChangeListener(
            Object self, Set<T> other,
            PropertyChangeListener listener) {

        return ev -> {
            CollectionChangedEvent<T> newEv = convertEvent(self, (CollectionChangedEvent<T>)ev, other);
            if (newEv.hasChanges()) {
                listener.propertyChange(newEv);
            }
        };
    }


    protected static <T> Set<T> nullSafeDifference(Set<T> set, Set<T> other) {
        return set == null ? Collections.emptySet() : Sets.difference((Set<T>)set, other);
    }


//    @SuppressWarnings("unchecked")
//    public static <T> CollectionChangedEventImpl<T> convertVetoableChangeEvent(Object self,
//            CollectionChangedEventImpl<T> ev, Set<T> other) {
//
//        // Added items that already exist in the 'other' set are substracted
//        Set<T> effectiveAdditions = nullSafeDifference((Set<T>)ev.getAdditions(), other);
//        // Deletions that already exist in the other set are also substracted
//        Set<T> effectiveDeletions = nullSafeDifference((Set<T>)ev.getDeletions(), other);
//
//        return new CollectionChangedEventImpl<T>(
//            self, self,
//            Sets.union(Sets.difference((Set<T>)self, effectiveDeletions), effectiveAdditions),
//            effectiveAdditions,
//            effectiveDeletions,
//            // Refreshes are just passed through
//            nullSafeDifference((Set<T>)ev.getRefreshes(), other));
//    }

    @SuppressWarnings("unchecked")
    public static <T> CollectionChangedEvent<T> convertEvent(Object self,
            CollectionChangedEvent<T> ev, Set<T> other) {

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
        return new ObservableSetDifference<>(a, b);
    }

    public static void main(String[] args) {
        ObservableSet<String> a = ObservableSetImpl.decorate(new LinkedHashSet<>());
        ObservableSet<String> b = ObservableSetImpl.decorate(new LinkedHashSet<>());

        ObservableSet<String> c = ObservableSetDifference.create(a, b);

        c.addPropertyChangeListener(ev -> System.out.println(ev));


        a.add("Hello"); // expect addition
        b.add("Hello"); // expect no event

        a.remove("Hello"); // expect no event
        b.remove("Hello"); // expect event

    }
}

