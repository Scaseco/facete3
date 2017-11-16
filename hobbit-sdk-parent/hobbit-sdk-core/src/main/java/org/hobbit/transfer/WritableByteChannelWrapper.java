package org.hobbit.transfer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class WritableByteChannelWrapper
	implements WritableByteChannel
{
	protected Function<? super ByteBuffer, ? extends Number> consumer;
	protected Callable<?> close;
	protected Supplier<Boolean> isOpen;

	public WritableByteChannelWrapper(Function<? super ByteBuffer, ? extends Number> consumer, Callable<?> close, Supplier<Boolean> isOpen) {
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
	public int write(ByteBuffer t) {
		Number tmp = consumer.apply(t);
		int result = tmp.intValue();
		return result;
	}
}

// public class WritableByteChannelSimple
// implements WritableByteChannel
// {
// protected ByteBuffer dataBuffer = ByteBuffer.allocate(4096);
// protected long batchSequenceId = 1;
// protected boolean closeChannelOnClose = false;
//
// protected ChunkedProtocolWriter protocol;
// protected Consumer<ByteBuffer> dataDelegate;
//
// protected Runnable closeAction;
//
// protected ByteBuffer payloadRegion;
//
// public WritableByteChannelSimple(ChunkedProtocolWriter protocol,
// Consumer<ByteBuffer> dataDelegate, Runnable closeAction) {
// super();
// this.protocol = protocol;
// this.dataDelegate = dataDelegate;
// this.closeAction = closeAction;
//
// dataBuffer = protocol.nextBuffer(dataBuffer);
// //payloadRegion = protocol.getPayload(dataBuffer);
// }
//
// @Override
// public int write(ByteBuffer src) throws IOException {
// ByteBuffer tmp = src.duplicate();
// write(tmp.array(), 0, tmp.position());
// }
//
//// public int write(byte[] b, int off, int len) throws IOException {
////
//// int remainingLen = len;
//// while(remainingLen > 0) {
//// int remainingCapacity = dataBuffer.limit() - dataBuffer.position();
////
//// if(remainingCapacity == 0) {
//// flush();
//// continue;
//// }
////
//// int batchSize = Math.min(remainingLen, remainingCapacity);
////
//// dataBuffer.put(b, off, batchSize);
////
//// off += batchSize;
//// remainingLen -= batchSize;
//// remainingCapacity -= batchSize;
//// }
////
//// }
//
//
// public void flush() throws IOException {
//// int pos = dataBuffer.position();
//
//// dataBuffer.rewind();
//// ByteBuffer slice = dataBuffer.slice();
//// slice.limit(pos);
//// ByteBuffer p
//// byte[] msgData = new byte[pos];
//// dataBuffer.rewind();
//// dataBuffer.get(msgData);
//
// System.out.println("Sending packet: " + protocol.toString(dataBuffer));
// //channel.basicPublish(exchangeName, routingKey, properties, msgData);
// dataDelegate.accept(dataBuffer);
//
// dataBuffer = protocol.nextBuffer(dataBuffer);
// //payloadRegion = protocol.getPayload(dataBuffer);
//
//
// //super.flush();
// }
//
//
// @Override
// public void close() throws IOException {
// protocol.setLastChunkFlag(dataBuffer, true);
//
//// int pos = dataBuffer.position();
//// dataBuffer.position(0);
//// long currentSeqId = dataBuffer.getLong(0);
//// dataBuffer.position(0);
//// dataBuffer.putLong(0, -currentSeqId);
//// dataBuffer.position(pos);
//
// flush();
//
// if(closeAction != null) {
// closeAction.run();
// }
//// if(closeChannelOnClose) {
//// try {
//// channel.close();
//// } catch (TimeoutException e) {
//// throw new RuntimeException(e);
//// }
//// }
// }
//
// }
