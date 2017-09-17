package org.hobbit.benchmarks.faceted_browsing;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;

public class OmitSimilarItems<T>
    implements Consumer<T>
{
    protected BiPredicate<? super T, ? super T> isTooSimilar;

    protected Consumer<? super T> itemDelegate;
    protected Consumer<Long> skipCountDelegate;

    protected T firstDistinguishedItem = null;
    protected T mostRecentlySkippedItem = null;
    protected long itemSkipCount = 0;

    public OmitSimilarItems(Consumer<? super T> itemDelegate, Consumer<Long> skipCountDelegate, BiPredicate<? super T, ? super T> isTooSimilar) {
        super();
        this.itemDelegate = itemDelegate;
        this.skipCountDelegate = skipCountDelegate;
        this.isTooSimilar = isTooSimilar;
    }


    @Override
    public void accept(T item) {

        boolean skip = isTooSimilar.test(firstDistinguishedItem, item);
        if(skip) {
            mostRecentlySkippedItem = item;
            ++itemSkipCount;
        } else {

            if(mostRecentlySkippedItem != null) {
                if(itemSkipCount > 1) {
                    skipCountDelegate.accept(itemSkipCount);
                }
                itemDelegate.accept(mostRecentlySkippedItem);
                //System.out.println(lineSkipped);
                mostRecentlySkippedItem = null;
                itemSkipCount = 0;
            }
            itemDelegate.accept(item);
            firstDistinguishedItem = item;
        }
    }

    public static Consumer<String> forStrings(int maxLevenshteinDistance, Consumer<String> delegate) {
        BiPredicate<String, String> predicate =
            (a, b) -> a == null || b == null
                ? false
                : StringUtils.getLevenshteinDistance(a, b) <= maxLevenshteinDistance;

        Consumer<String> result = new OmitSimilarItems<>(
            delegate,
            (itemSkipCount) -> delegate.accept("  ... " + itemSkipCount + " similar lines omitted ..."),
            predicate
        );

        return result;
    }
}
