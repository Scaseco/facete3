package org.hobbit.benchmarks.faceted_browsing.components;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.hobbit.transfer.Publisher;

public class PublisherUtils {
    public static <T> CompletableFuture<T> awaitMessage(Publisher<T> publisher, Predicate<T> condition) {
        CompletableFuture<T> result = new CompletableFuture<T>();

        Consumer<T> subscriber = item -> {
            boolean isConditionSatisfied = condition.test(item);
            if(isConditionSatisfied) {
                result.complete(item);
            }
        };

        publisher.subscribe(subscriber);

        result.whenComplete((i, t) -> publisher.unsubscribe(subscriber));
        return result;
    }
}
