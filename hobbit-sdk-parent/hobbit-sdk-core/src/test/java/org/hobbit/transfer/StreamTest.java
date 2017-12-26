package org.hobbit.transfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;


public class StreamTest {

	@Test
	public void testFlowableTransformation() {
		List<String> actual = new ArrayList<>();//core.toList().blockingGet();

		
		PublishProcessor<String> core = PublishProcessor.create();
		core.subscribe(actual::add);
		
		PublishProcessor<String> enhanced = PublishProcessor.create();
		
		enhanced.map(x -> "Hello " + x).subscribe(core);
		

		enhanced.onNext("World");
		enhanced.onNext("Bob");
		enhanced.onComplete();

		
		Assert.assertEquals(Arrays.asList("Hello World", "Hello Bob"), actual);
	}

	@Test
	public void testFlowableTransformationForLocalOrdering() {
		List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		
		List<Integer> actual = Flowable
			.just(5, 4, 3, 2, 1, 6, 7, 9, 8, 10)
			.compose(FlowableTransformerLocalOrdering.<Integer, Integer>transformer(1, (id) -> id + 1, item -> item)::apply)
			.toList().blockingGet();
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testStream() throws Exception {
		//PublishingWritableByteChannel channel = new PublishingWritableByteChannelSimple();
		PublishProcessor<ByteBuffer> channel = PublishProcessor.create(); 
		
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
				.range(0, 100000)
				.mapToObj(Integer::toString)
				.collect(Collectors.toList());
				//.map(s -> s.getBytes(StandardCharsets.UTF_8))
				//.map(ByteBuffer::wrap);

		PrintStream out = new PrintStream(OutputStreamChunkedTransfer.newInstanceForByteChannel(channel::onNext, null));
		
		expected.forEach(out::println);

		out.flush();
		out.close();
		
		List<String> actual = waitForStreamReception.get(10, TimeUnit.SECONDS);
		//System.out.println(expected);
		Assert.assertEquals(expected, actual);
		// Wait for the stream thread to finish
		//Thread.sleep(50000);
	}
}
