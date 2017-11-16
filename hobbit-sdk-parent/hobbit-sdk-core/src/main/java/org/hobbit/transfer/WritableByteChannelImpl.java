package org.hobbit.transfer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;


public class WritableByteChannelImpl
	implements WritableChannel<ByteBuffer>, WritableByteChannel
{
	protected WritableByteChannel delegate;

	public WritableByteChannelImpl(WritableByteChannel delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public boolean isOpen() {
		boolean result = delegate.isOpen();
		return result;
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public int write(ByteBuffer obj) throws IOException {
		int result = delegate.write(obj);
		return result;
	}

	@Override
	public <I> WritableChannel<I> prependEncoder(Function<? super I, ? extends ByteBuffer> encoder) {
		WritableChannel<I> result = new WritableChannelEncoder<I, ByteBuffer>(this, encoder);
		return result;
	}

	public static WritableByteChannelImpl wrap(Function<? super ByteBuffer, ? extends Number> consumer, Callable<?> close, Supplier<Boolean> isOpen) {
		WritableByteChannelWrapper wrapper = new WritableByteChannelWrapper(consumer, close, isOpen);

		WritableByteChannelImpl result = new WritableByteChannelImpl(wrapper);
		return result;
		
//		ChannelWrapper<ByteBuffer> result = new ChannelWrapperImpl<>(consumer, flowable, () -> { channel.close(); return null; }, () -> channel.isOpen());
//		this.delegate = delegate;
	}

}
