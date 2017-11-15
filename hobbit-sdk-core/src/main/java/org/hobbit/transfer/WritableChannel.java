package org.hobbit.transfer;

import java.io.IOException;
import java.nio.channels.Channel;
import java.util.function.Function;

public interface WritableChannel<T>
	extends Channel //, Consumer<T>
{
	int write(T obj) throws IOException;

	<I> WritableChannel<I> prependEncoder(Function<? super I, ? extends T> encoder);
}
