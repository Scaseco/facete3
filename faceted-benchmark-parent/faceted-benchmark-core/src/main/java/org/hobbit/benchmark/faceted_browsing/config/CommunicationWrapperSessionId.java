package org.hobbit.benchmark.faceted_browsing.config;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.hobbit.core.config.CommunicationWrapper;
import org.hobbit.core.config.HobbitConfigChannelsPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunicationWrapperSessionId
	implements CommunicationWrapper<ByteBuffer>
{    
    private static final Logger logger = LoggerFactory.getLogger(CommunicationWrapperSessionId.class);
    
	protected String sessionId;
	protected Set<String> acceptedHeaderIds;

	public CommunicationWrapperSessionId(String sessionId, Set<String> acceptedHeaderIds) {
		super();
		this.sessionId = sessionId;
		this.acceptedHeaderIds = acceptedHeaderIds;
	}

	
	@Override
	public ByteBuffer wrapSender(ByteBuffer msg) {
		ByteBuffer result = HobbitConfigChannelsPlatform.createCmdMessage(msg, sessionId);
		return result;
	}


	@Override
	public List<ByteBuffer> wrapReceiver(ByteBuffer msg) {
		List<ByteBuffer> result = Collections.singletonList(msg).stream()
			.map(HobbitConfigChannelsPlatform::parseCommandBuffer)
			.peek(e -> logger.info("CommunicationWrapper " + (acceptedHeaderIds.contains(e.getKey()) ? "accepted" : "rejected") + " message with session id " + e.getKey()))
			.filter(e -> acceptedHeaderIds.contains(e.getKey()))
			.map(Entry::getValue)
			.collect(Collectors.toList());

		return result;
	}

	
//	@Override
//	public Flowable<SimpleReplyableMessage<ByteBuffer>> wrap(Flowable<SimpleReplyableMessage<ByteBuffer>> delegate) {
//		return delegate
//			.flatMap(msg -> Flowable.fromIterable(wrap(msg)));
//	}
//
//	@Override
//	public Subscriber<ByteBuffer> wrapSender(Subscriber<ByteBuffer> subscriber) {
//		return transformBeforePublish(subscriber, this::transformMsg);
//	}
//	
//	@Override
//	public Flowable<ByteBuffer> wrapReceiver(Flowable<ByteBuffer> flowable) {			
//		return flowable
//				.map(HobbitConfigChannelsPlatform::parseCommandBuffer)
//				.filter(e -> acceptedHeaderIds.contains(e.getKey()))
//				.map(Entry::getValue);
//	}
//
//	public Set<SimpleReplyableMessage<ByteBuffer>> wrap(SimpleReplyableMessage<ByteBuffer> msg) {
//		Entry<String, ByteBuffer> e = HobbitConfigChannelsPlatform.parseCommandBuffer(msg.getValue());
//
//		SimpleReplyableMessage<ByteBuffer> base = acceptedHeaderIds.contains(e.getKey())							
//				? new SimpleReplyableMessageImpl<>(e.getValue(), msg.getReplyConsumer())
//				: null;
//		
//		Set<SimpleReplyableMessage<ByteBuffer>> result = base == null ? Collections.emptySet() : Collections.singleton(base);
//		return result;
//	}
//
//	public static <I, O> Subscriber<I> transformBeforePublish(Subscriber<O> subscriber, Function<? super I, ? extends O> transform) {
//		PublishProcessor<I> result = PublishProcessor.create();			
//		result
//			.map(transform::apply)
//			.subscribe(subscriber);
//		return result;
//		
//	}
//	
//	public ByteBuffer transformMsg(ByteBuffer data) {
//		ByteBuffer result = HobbitConfigChannelsPlatform.createCmdMessage(data, sessionId);
//		return result;
//	}

}
