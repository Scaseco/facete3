package org.hobbit.core.config;

public interface SimpleReplyableMessage<T> {
	T getValue();
	void reply(T value);
}
