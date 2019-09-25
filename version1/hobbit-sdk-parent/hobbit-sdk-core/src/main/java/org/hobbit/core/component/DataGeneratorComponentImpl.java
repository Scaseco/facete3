package org.hobbit.core.component;

import java.nio.ByteBuffer;

import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("MainService")
public class DataGeneratorComponentImpl<T>
	extends DataGeneratorComponentBase
{
//	public Runnable action;

//	public DataGeneratorComponentImpl(
//			Subscriber<ByteBuffer> commandSender,
//			Runnable action) {
//		super(commandSender);
//		this.action = action;
//	}
//	
//	
//	@Override
//	void runDataGeneration() {
//		action.run();
//	}
}
