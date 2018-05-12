package org.hobbit.core.utils;

import java.util.function.Consumer;

// Somewhat ugly but working: https://stackoverflow.com/questions/18198176/java-8-lambda-function-that-throws-exception
@FunctionalInterface
public interface CheckedConsumer<T> {
   void accept(T t) throws Exception;

   public static <U> Consumer<U> wrap(CheckedConsumer<U> delegate) {
       return (item) -> {
        try {
            delegate.accept(item);
        } catch (Exception e) {
            throw new RuntimeException(e);
        };
    };
   }
}
