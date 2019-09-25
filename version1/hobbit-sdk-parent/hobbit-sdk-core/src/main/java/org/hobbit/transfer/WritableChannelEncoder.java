package org.hobbit.transfer;

import java.io.IOException;
import java.util.function.Function;


public class WritableChannelEncoder<I, O>
	implements WritableChannel<I>
{
	protected WritableChannel<O> delegate;
	protected Function<? super I, ? extends O> encoder;
	
	public WritableChannelEncoder(WritableChannel<O> delegate, Function<? super I, ? extends O> encoder) {
		super();
		this.delegate = delegate;
		this.encoder = encoder;
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
	public int write(I obj) throws IOException {
		O encodedObj = encoder.apply(obj);
		int result = delegate.write(encodedObj);
		return result;
	}

	@Override
	public <X> WritableChannel<X> prependEncoder(Function<? super X, ? extends I> newEncoder) {

		Function<? super X, ? extends O> compoundEncoder = newEncoder.andThen(encoder);
		
		WritableChannel<X> result = new WritableChannelEncoder<>(delegate, compoundEncoder);
		return result;
	}
}
