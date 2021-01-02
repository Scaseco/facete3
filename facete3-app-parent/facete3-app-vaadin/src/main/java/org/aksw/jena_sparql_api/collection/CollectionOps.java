package org.aksw.jena_sparql_api.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.aksw.commons.collections.CollectionFromIterable;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

public class CollectionOps {

    public static <T> Collection<T> unionCore(Collection<T> a, Collection<T> b) {
        return CollectionFromIterable.wrap(Iterables.concat(a, b));
    }

    public static <T> Collection<T> differenceCore(Collection<T> base, T removedItem) {
        Collection<T> result = differenceCore(base, Collections.singleton(removedItem));
        return result;
    }

    public static <T> Collection<T> differenceCore(Collection<T> base, Collection<T> removedItems) {
        Collection<T> result = CollectionFromIterable.wrap(() -> Iterators.filter(
                base.iterator(),
                PredicateFromMultisetOfDiscardedItems.create(HashMultiset.create(removedItems))::test));

        return result;
    }


    public static <T> Collection<T> smartDifference(Collection<T> base, Collection<T> removedItem) {
        Collection<T> result = base instanceof Set && removedItem instanceof Set
                ? Sets.difference((Set<T>)base, (Set<T>)removedItem)
                : differenceCore(base, removedItem);

        return result;
    }

    public static <T> Collection<T> smartUnion(Collection<T> base, Collection<T> addedItems) {
        Collection<T> result = base instanceof Set && addedItems instanceof Set
                ? Sets.union((Set<T>)base, (Set<T>)addedItems)
                : unionCore(base, addedItems);

        return result;
    }
}
