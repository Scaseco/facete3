package org.hobbit.core.component;

import java.nio.ByteBuffer;

import org.reactivestreams.Subscriber;

public class DataGeneratorComponentImpl<T>
	extends DataGeneratorComponentBase
{
	public Runnable action;

	public DataGeneratorComponentImpl(
			Subscriber<ByteBuffer> commandSender,
			Runnable action) {
		super(commandSender);
		this.action = action;
	}
	
	
	@Override
	void runDataGeneration() {
		action.run();
	}
}
