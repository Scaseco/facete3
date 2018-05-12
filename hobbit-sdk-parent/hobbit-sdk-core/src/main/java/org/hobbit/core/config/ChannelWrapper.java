package org.hobbit.core.config;

import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;

public class ChannelWrapper<T>
	//implements ChannelWrapper<T>
{
	protected Subscriber<T> subscriber;
	protected Flowable<T> flowable;
	
//	protected Consumer<T> consumer;
//	protected Callable<?> close;
//	protected Supplier<Boolean> isOpen;

	
//	public ChannelWrapperImpl(Consumer<? super T> consumer, Flowable<? extends T> flowable, Closeable closeable) {
//		super();
//		this.consumer = consumer;
//		this.flowable = flowable;
//		this.close = () -> { try { closeable.close(); } catch (IOException e) { throw new RuntimeException(e); } };
//	}

	public ChannelWrapper(Subscriber<T> subscriber, Flowable<T> flowable) {
		super();
		//this.consumer = consumer;
		this.flowable = flowable;
		//this.close = close;
		//this.isOpen = isOpen;
		
		
		this.subscriber = subscriber; //new WritableChannelImpl<>(consumer, close, isOpen);
	}

//	@Override
//	public void close() throws IOException {
//		if(close != null) {
//			try {
//				close.call();
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//	}

//	@Override
//	public Consumer<T> getConsumer() {
//		return consumer;
//	}

	//@Override
	public Subscriber<T> getSubscriber() {
		return subscriber;
	}
	
	//@Override
	public Flowable<T> getFlowable() {
		return flowable;
	}
	
}
