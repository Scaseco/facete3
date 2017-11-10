package org.hobbit.config.platform;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import io.reactivex.Flowable;

public class ChannelWrapperImpl<T>
	implements ChannelWrapper<T>
{
	protected Consumer<T> consumer;
	protected Flowable<T> flowable;
	protected Callable<?> close;

//	public ChannelWrapperImpl(Consumer<? super T> consumer, Flowable<? extends T> flowable, Closeable closeable) {
//		super();
//		this.consumer = consumer;
//		this.flowable = flowable;
//		this.close = () -> { try { closeable.close(); } catch (IOException e) { throw new RuntimeException(e); } };
//	}

	public ChannelWrapperImpl(Consumer<T> consumer, Flowable<T> flowable, Callable<?> close) {
		super();
		this.consumer = consumer;
		this.flowable = flowable;
		this.close = close;
	}

	@Override
	public void close() throws IOException {
		if(close != null) {
			try {
				close.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public Consumer<T> getConsumer() {
		return consumer;
	}

	@Override
	public Flowable<T> getFlowable() {
		return flowable;
	}
	
}
