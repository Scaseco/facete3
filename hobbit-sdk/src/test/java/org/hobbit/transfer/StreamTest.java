package org.hobbit.transfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;


public class StreamTest {
	@Test
	public void testStream() throws Exception {
		PublishingWritableByteChannel channel = new PublishingWritableByteChannelSimple();
		
		StreamManager streamManager = new InputStreamManagerImpl();
		
		CompletableFuture<List<String>> waitForStreamReception = new CompletableFuture<>(); 
		
		
		streamManager.subscribe(in -> {
			//new Thread(() -> {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
				String line;
				List<String> r = new ArrayList<>();
				
				try {
					while((line = reader.readLine()) != null) {
						r.add(line);
						//System.out.println("Received: " + line);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					waitForStreamReception.complete(r);
				}
			//}).run();
		});
		
		channel.subscribe(streamManager::handleIncomingData);
		
		List<String> expected = IntStream
				.range(0, 10000)
				.mapToObj(Integer::toString)
				.collect(Collectors.toList());
				//.map(s -> s.getBytes(StandardCharsets.UTF_8))
				//.map(ByteBuffer::wrap);

		PrintStream out = new PrintStream(OutputStreamChunkedTransfer.newInstanceForByteChannel(channel, null));
		
		expected.forEach(out::println);

		out.flush();
		out.close();
		
		List<String> actual = waitForStreamReception.get(10, TimeUnit.SECONDS);
		
		Assert.assertEquals(expected, actual);
		// Wait for the stream thread to finish
		//Thread.sleep(50000);
	}
}
