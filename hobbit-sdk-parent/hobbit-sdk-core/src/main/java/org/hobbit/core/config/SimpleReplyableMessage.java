package org.hobbit.core.config;

import java.util.function.Consumer;

// Simple refers to the value and the reply having the same generic type
public interface SimpleReplyableMessage<T> {
	T getValue();
	Consumer<T> getReplyConsumer();

	// TODO Probably remove the reply method in favor of the consumer
	void reply(T value);
}
