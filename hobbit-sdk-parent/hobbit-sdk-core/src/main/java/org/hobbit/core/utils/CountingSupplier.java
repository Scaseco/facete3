package org.hobbit.core.utils;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of a supplier that delegates the .get() call to a function.
 * This function takes a count as argument, which the supplier increments after each call.
 *
 *
 * @author raven Sep 21, 2017
 *
 * @param <T>
 */
public class CountingSupplier<T>
    implements Supplier<T>
{
    protected long current = 0;
    protected Function<Long, T> delegate;

    public CountingSupplier(Function<Long, T> delegate) {
        super();
        this.current = 0;
        this.delegate = delegate;
    }

    public CountingSupplier(long current, Function<Long, T> delegate) {
        super();
        this.current = current;
        this.delegate = delegate;
    }

    @Override
    public T get() {
        T result = delegate.apply(current++);
        return result;
    }

    public static <T> CountingSupplier<T> from(Function<Long, T> delegate) {
        return new CountingSupplier<T>(delegate);
    }
}
