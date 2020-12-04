package org.aksw.jena_sparql_api.collection;


import java.util.function.Predicate;

import com.google.common.collect.Multiset;

/**
 * A predicate backed by a multiset of items that are considered as discarded.
 * predicate.test(x) will return false as long as the multiset contains x.
 * On each 'hit' an item in the multiset is returned.
 *
 * This is useful to create a collection views that hide a specific number of items.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public class PredicateDiscard<T>
    implements Predicate<T>
{
    protected Multiset<T> discards;

    public PredicateDiscard(Multiset<T> discards) {
        super();
        this.discards = discards;
    }

    public static <T> Predicate<T> create(Multiset<T> discards) {
        return new PredicateDiscard<>(discards);
    }

    @Override
    public boolean test(T t) {
        boolean isDiscarded = discards.contains(t);

        if (isDiscarded) {
            discards.remove(t);
        }

        return !isDiscarded;
    }
}
