package org.hobbit.core.config;

/**
 * Wrapper used to add metadata to payload messages.
 * When sending, used to e.g. inject hobbit session IDs into messages. 
 * When receiving, used to filter out unrelated messages. 
 * 
 * 
 * @author raven
 *
 * @param <T>
 */
public interface CommunicationWrapper<T> {
	T wrapSender(T msg);
	Iterable<T> wrapReceiver(T msg);
	
//	Flowable<SimpleReplyableMessage<ByteBuffer>> wrap(Flowable<SimpleReplyableMessage<ByteBuffer>> delegate);
//	Subscriber<ByteBuffer> wrapSender(Subscriber<ByteBuffer> subscriber);
//	Flowable<ByteBuffer> wrapReceiver(Flowable<ByteBuffer> flowable);
}
