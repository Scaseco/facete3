package org.hobbit.transfer;

import java.util.List;

import com.google.common.collect.Lists;

public class IterableUtils {
    /**
     * Creates a copy of an iterable in a synchronized block and then
     *
     * Prevents concurrent modification exceptions when e.g. subscribers change during event processing
     */
    public static <T> List<T> synchronizedCopy(Iterable<T> items) {
        List<T> result;
        synchronized(items) {
            result = Lists.newArrayList(items);
        }
        return result;
    }
}
