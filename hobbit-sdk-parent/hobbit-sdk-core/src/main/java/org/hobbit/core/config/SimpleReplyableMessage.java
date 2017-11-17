package org.hobbit.core.config;

// Simple refers to the value and the reply having the same generic type
public interface SimpleReplyableMessage<T> {
	T getValue();
	void reply(T value);
}
