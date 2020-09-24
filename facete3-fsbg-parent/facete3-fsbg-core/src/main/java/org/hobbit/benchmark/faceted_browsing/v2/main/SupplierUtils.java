package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.aksw.commons.accessors.SingleValuedAccessorDirect;


/**
 * Utils for stream like operations on suppliers.
 * TODO Probably get rid of these utils and use streams instead.
 * TODO Alternatively, implement for Callable (= Supplier + Exception) instead, and only provide wrappers for Supplier here
 *
 * @author Claus Stadler, Oct 23, 2018
 *
 */
public class SupplierUtils {


    public static <T> Callable<T> just(T item) {
        Object[] next = {item};
        return () -> {
            @SuppressWarnings("unchecked")
            T r = (T)next[0];
            next[0] = null;
            return r;
        };
    }

    public static <T> Supplier<T> toSupplier(Callable<T> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> Callable<T> from(Iterable<T> iterable) {
        return from(iterable.iterator());
    }

    public static <T> Callable<T> from(Iterator<T> it) {
        return () -> it.hasNext() ? it.next() : null;
    }

    /**
     * Create a callable of optionals for an iterator whose .next() method may yield null values
     *
     * @param it
     * @return
     */
    public static <T> Callable<Optional<T>> fromNullable(Iterator<T> it) {
        return () -> it.hasNext() ? Optional.ofNullable(it.next()) : null;
    }

    /**
     * As long as there are items in b, injects items of b between each pair of items in a:
     * [a1] [b1] ... [an-1] [bn-1] [an]
     * Will emit all items in a; i.e. items of a will continue to be emitted after b is consumed
     * If there is 0 or 1 items in a, b will not be called
     *
     * @param a
     * @param b
     * @return
     */
    public static <T> Callable<T> interleave(Callable<T> a, Callable<T> b) {
        int toggle[] = {0}; // 0: a-then-b, 1: b-then-a, 2: only-a, 3: only-null

        return () -> {
            T r;
            for(;;) {
                if(toggle[0] == 0 || toggle[0] == 2) {
                    r = a.call();

                    toggle[0] = r == null ? 3 : 1;
                } else if(toggle[0] == 1) {
                    r = b.call();
                    if(r == null) {
                        toggle[0] = 2;
                        continue;
                    } else {
                        toggle[0] = 0;
                    }
                } else {
                    r = null;
                }
                break;
            }

            return r;
        };
    }

    public static <T> Callable<T> limit(int n, Callable<T> supplier) {
        int i[] = {0};
        return () -> {
            T r = i[0]++ < n ? supplier.call() : null;
            return r;
        };
    }


    public static <I, O> Callable<O> map(Callable<I> supplier, Function<? super I, O> fn) {
        return () -> {
            I tmp = supplier.call();
            O r = tmp == null ? null : fn.apply(tmp);
            return r;
        };
    }

    /**
     * Flat maps a supplier of suppliers by assuming an inner supplier is finished upon returning null
     *
     * @param lhs
     * @return
     */
    public static <T> Callable<T> flatMap(Callable<? extends Callable<? extends T>> lhs) {

        Object[] rhs = {null};
        boolean[] done = {false};

        return () -> {
            T r = null;

            do {
                @SuppressWarnings("unchecked")
                Callable<? extends T> current = (Callable<? extends T>)rhs[0];

                if (current == null && !done[0]) {
                    current = lhs.call();
                    // If we get null for the next supplier, we assume we are done
                    if (current == null) {
                        done[0] = true;
                        break;
                    } else {
                        rhs[0] = current;
                    }
                    continue;
                }

                r = current.call();
                if(r == null) {
                    rhs[0] = null;
                }
            } while (r == null && !done[0]);

            return r;
        };
    }

    public static <T> Callable<T> flatMapIterable(Callable<? extends Iterable<T>> supplier) {
        return flatMap(() -> {
            Iterable<T> iterable = supplier.call();
            Callable<T> r = iterable == null ? null : SupplierUtils.from(iterable);
            return r;
        });
    }

    /**
     * Flat maps a supplier of suppliers of optionals by assuming an inner supplier is finished *either*
     * upon returning null or returning an empty optional
     * @param supplier
     * @return
     */
    public static <T> Callable<Optional<T>> flatMapOptionals(Callable<? extends Callable<? extends Optional<T>>> supplier) {
        return flatMap(supplier, Optional::isPresent);
    }

    public static <T> Callable<T> flatMap(Callable<? extends Callable<? extends T>> supplier, Predicate<? super T> isPresent) {

        // When done, ref's value will be set to null
        // On init, ref's value is a *reference* (to null)
        SingleValuedAccessorDirect<SingleValuedAccessorDirect<Callable<? extends T>>> ref =
                new SingleValuedAccessorDirect<>(new SingleValuedAccessorDirect<>(null));

        return () -> {
            SingleValuedAccessorDirect<Callable<? extends T>> held = ref.get();
            T r = null;
            if(held != null) {
                //while(r == null) {
                while(r == null || (isPresent != null && !isPresent.test(r))) {
                    Callable<? extends T> current = held.get();

                    if(current == null) {
                        Callable<? extends T> next = supplier.call();
                        // If we get null for the next supplier, we assume we are done
                        if(next == null) {
                            ref.set(null);
                            break;
                        } else {
                            held.set(next);
                        }
                        continue;
                    }

                    r = current.call();
                    if(r == null) {
                        held.set(null);
                    }
                }
            }
            return r;
        };
    }

}