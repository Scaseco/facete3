package org.hobbit.core.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

// Simple refers to the value and the reply having the same generic type
/**
 * At the core, a message is seen as a Supplier of a payload T.
 * 
 * 
 * 
 * @author raven Nov 20, 2017
 *
 * @param <T>
 */
public interface SimpleReplyableMessage<T>
	extends Supplier<T>
{
	T getValue();
	Consumer<T> getReplyConsumer();

	// TODO Probably remove the reply method in favor of the consumer
	void reply(T value);
}
