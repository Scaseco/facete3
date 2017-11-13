package org.hobbit.config.platform;

import org.hobbit.transfer.WritableChannel;

import io.reactivex.Flowable;

public class ChannelWrapper<T>
	//implements ChannelWrapper<T>
{
	protected WritableChannel<T> writableChannel;
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

	public ChannelWrapper(WritableChannel<T> writableChannel, Flowable<T> flowable) {
		super();
		//this.consumer = consumer;
		this.flowable = flowable;
		//this.close = close;
		//this.isOpen = isOpen;
		
		
		this.writableChannel = writableChannel; //new WritableChannelImpl<>(consumer, close, isOpen);
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
	public WritableChannel<T> getWritableChannel() {
		return writableChannel;
	}
	
	//@Override
	public Flowable<T> getFlowable() {
		return flowable;
	}
	
}
