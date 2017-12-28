package org.hobbit.core.config;

public interface CommunicationWrapper<T> {
	T wrapSender(T msg);
	Iterable<T> wrapReceiver(T msg);
	
//	Flowable<SimpleReplyableMessage<ByteBuffer>> wrap(Flowable<SimpleReplyableMessage<ByteBuffer>> delegate);
//	Subscriber<ByteBuffer> wrapSender(Subscriber<ByteBuffer> subscriber);
//	Flowable<ByteBuffer> wrapReceiver(Flowable<ByteBuffer> flowable);
}
