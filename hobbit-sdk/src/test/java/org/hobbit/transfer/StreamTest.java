package org.hobbit.transfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

public class StreamTest {
	@Test
	public void testStream() throws Exception {
		PublishingWritableByteChannel channel = new PublishingWritableByteChannelSimple();
		
		StreamManager streamManager = new InputStreamManagerImpl();
				
		streamManager.subscribe(in -> {
			//new Thread(() -> {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
				String line;
				try {
					while((line = reader.readLine()) != null) {
						System.out.println("Received: " + line);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			//}).run();
		});
		
		channel.subscribe(streamManager::handleIncomingData);
		
		Stream<String> data = IntStream
				.range(0, 10000)
				.mapToObj(Integer::toString);
				//.map(s -> s.getBytes(StandardCharsets.UTF_8))
				//.map(ByteBuffer::wrap);

		PrintStream out = new PrintStream(OutputStreamChunkedTransfer.newInstanceForByteChannel(channel, null));
		
		data.forEach(t -> {
			//System.out.println("Sending: " + t);
			//System.out.println("Sending: " + t);
			out.println("item #" + t);
		});
		out.flush();
		out.close();
		
		// Wait for the stream thread to finish
		Thread.sleep(50000);
	}
}
