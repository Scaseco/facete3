package org.aksw.jena_sparql_api.collection;


import java.util.function.Predicate;

import com.google.common.collect.Multiset;

/**
 * A predicate backed by a multiset of items that are considered as discarded.
 * predicate.test(x) will return false as long as the multiset contains x (true otherwise).
 * Whenever the multiset contained x then x's cardinality is reduced by 1
 * (by removing one occurrence of x from the multiset)
 *
 * This is useful to create collection views that hide a specific number of items.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public class PredicateFromMultisetOfDiscardedItems<T>
    implements Predicate<T>
{
    protected Multiset<T> discards;

    public PredicateFromMultisetOfDiscardedItems(Multiset<T> discards) {
        super();
        this.discards = discards;
    }

    public static <T> Predicate<T> create(Multiset<T> discards) {
        return new PredicateFromMultisetOfDiscardedItems<>(discards);
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
