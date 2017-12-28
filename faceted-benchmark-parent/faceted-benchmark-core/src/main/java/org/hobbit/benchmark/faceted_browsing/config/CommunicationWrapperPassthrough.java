package org.hobbit.benchmark.faceted_browsing.config;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.hobbit.core.config.CommunicationWrapper;

public class CommunicationWrapperPassthrough
	implements CommunicationWrapper<ByteBuffer>
{

	@Override
	public ByteBuffer wrapSender(ByteBuffer msg) {
		return msg;
	}

	@Override
	public List<ByteBuffer> wrapReceiver(ByteBuffer msg) {
		return Collections.singletonList(msg);
	}
	
//	@Override
//	public Flowable<SimpleReplyableMessage<ByteBuffer>> wrap(Flowable<SimpleReplyableMessage<ByteBuffer>> delegate) {
//		return delegate;
//	}
//
//	@Override
//	public Subscriber<ByteBuffer> wrapSender(Subscriber<ByteBuffer> subscriber) {
//		return subscriber;
//	}
//
//	@Override
//	public Flowable<ByteBuffer> wrapReceiver(Flowable<ByteBuffer> flowable) {
//		return flowable;
//	}
}
