package org.hobbit.core.component;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.reactivex.Flowable;

/**
 * Simple Data Generator that sends the generated data
 * to both the TG and the SA.
 * 
 * 
 * 
 * 
 * @author raven
 *
 * @param <T> The type of the generated data records 
 */
public interface DataProcessor<T>
	extends Consumer<Supplier<Flowable<T>>>
{
}
