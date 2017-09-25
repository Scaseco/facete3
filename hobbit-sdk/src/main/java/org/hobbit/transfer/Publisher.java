package org.hobbit.transfer;

import java.util.function.Consumer;

public interface Publisher<T> {
    /**
     * Expected to return a runnable that idempotently unsubscribes the subscriber
     *
     * @param subscriber
     * @return
     */
    Runnable subscribe(Consumer<? super T> subscriber);

//    void subscribe(BiConsumer<? super T, Consumer<? super T>> subscriber);

    /**
     * Unsubscribe.
     *
     * TODO Maybe an argument of type Object would be more convenient?
     *
     * @param subscribe
     */
    void unsubscribe(Consumer<? super T> subscribe);
}
