package org.aksw.facete3.app.vaadin.plugin.view;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

public class Cardinalities {
    public static <T> Optional<T> expectZeroOrOne(Stream<T> items) {
        Optional<T> result;
        try (Stream<T> closing = items) {
            Iterator<T> it = items.iterator();
            if (it.hasNext()) {
                T item = it.next();
                result = Optional.of(item);

                if (it.hasNext()) {
//                    T firstExceedingItem = it.next();
                    throw new RuntimeException("Only 0 or 1 items expected");
                }
            } else {
                result = Optional.empty();
            }
        }
        return result;
    }
}
