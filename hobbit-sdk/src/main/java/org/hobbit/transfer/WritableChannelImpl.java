package org.hobbit.transfer;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class WritableChannelImpl<T>
	implements WritableChannel<T>
{
	protected Consumer<T> consumer;
	protected Callable<?> close;
	protected Supplier<Boolean> isOpen;

	public WritableChannelImpl(Consumer<T> consumer, Callable<?> close, Supplier<Boolean> isOpen) {
		super();
		Objects.requireNonNull(consumer);
		Objects.requireNonNull(close);
		Objects.requireNonNull(isOpen);
		
		this.consumer = consumer;
		this.close = close;
		this.isOpen = isOpen;
	}

	@Override
	public boolean isOpen() {
		boolean result = isOpen.get();
		return result;
	}

	@Override
	public void close() throws IOException {
		try {
			close.call();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public int write(T t) {
		consumer.accept(t);
		return -2;
	}

	@Override
	public <I> WritableChannel<I> prependEncoder(Function<? super I, ? extends T> encoder) {
		Consumer<I> newConsumer = (msg) -> {
			T encodedMsg = encoder.apply(msg);
			consumer.accept(encodedMsg);
		};
		
		WritableChannelImpl<I> result = new WritableChannelImpl<>(newConsumer, close, isOpen);
		return result;
	}
	
}
