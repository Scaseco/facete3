package org.hobbit.core.config;

import java.util.function.Consumer;

public class SimpleReplyableMessageImpl<T>
	implements SimpleReplyableMessage<T>
{
	protected T value;
	protected Consumer<T> replyer;
	
	public SimpleReplyableMessageImpl(T value, Consumer<T> replyer) {
		super();
		this.value = value;
		this.replyer = replyer;
	}

	@Override
	public T getValue() {
		return value;
	}
	
	@Override
	public void reply(T value) {
		System.out.println("[STATUS] Sending reply");
		replyer.accept(value);
	}
	
	@Override
	public Consumer<T> getReplyConsumer() {
		return replyer;
	}	
}
