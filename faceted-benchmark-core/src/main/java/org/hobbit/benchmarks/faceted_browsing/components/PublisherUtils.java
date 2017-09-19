package org.hobbit.benchmarks.faceted_browsing.components;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hobbit.transfer.Publisher;

public class PublisherUtils {

    public static <T> CompletableFuture<T> awaitMessage(Publisher<T> publisher, Predicate<? super T> condition) {
        return awaitMessage(Collections.singleton(publisher), condition);
    }

    public static <T> CompletableFuture<T> awaitMessage(Collection<Publisher<T>> publishers, Predicate<? super T> condition) {
        CompletableFuture<T> result = new CompletableFuture<T>();

        Consumer<T> subscriber = item -> {
            boolean isConditionSatisfied = condition.test(item);
            if(isConditionSatisfied) {
                result.complete(item);
            }
        };

        List<Runnable> unsubscribers = publishers.stream()
                .map(publisher -> publisher.subscribe(subscriber))
                .collect(Collectors.toList());



        result.whenComplete((i, t) -> unsubscribers.forEach(Runnable::run));
        return result;
    }
}
