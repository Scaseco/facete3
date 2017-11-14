package org.hobbit.core.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;


public class PublisherUtils {

    public static <T> CompletableFuture<T> triggerOnMessage(Flowable<T> publisher, Predicate<? super T> condition) {
        return triggerOnMessage(Collections.singleton(publisher), condition);
    }

    public static <T> CompletableFuture<T> triggerOnMessage(Collection<Flowable<T>> publishers, Predicate<? super T> condition) {
        CompletableFuture<T> result = new CompletableFuture<T>();

        Consumer<T> subscriber = item -> {
            boolean isConditionSatisfied = condition.test(item);
            if(isConditionSatisfied) {
                result.complete(item);
            }
        };

        List<Disposable> unsubscribers = publishers.stream()
                .map(publisher -> publisher.subscribe(subscriber::accept))
                .collect(Collectors.toList());



        // When complete - one way or the other - unsubscribe the listeners
        result.whenComplete((i, t) -> unsubscribers.forEach(Disposable::dispose));
        return result;
    }
}
