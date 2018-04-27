package org.hobbit.core.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;

public abstract class ComponentBaseExecutionThread
	extends AbstractExecutionThreadService
    implements BaseComponent
{
    private static final Logger logger = LoggerFactory.getLogger(ComponentBase.class);

    // The actual publisher of the command channel
    // All commands are re-publishehd via the local command publisher
    @Resource(name="commandReceiver")
    protected Flowable<ByteBuffer> remoteCommandPublisher;


    // This is the *local* publisher for the receiveCommand method
    // It combines data sent via the remote publisher and the receiveCommand function
    protected PublishProcessor<ByteBuffer> commandReceiver = PublishProcessor.create();

    protected Disposable commandPublisherDisposable = null;

    @Override
    public void init() throws Exception {
    	startUp();
    }

    
    @Override
    public void close() throws IOException {
        try {
            triggerShutdown();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void startUp() {
		coreInit();
    }

    
    @Override
    public void shutDown() {
    	Optional.ofNullable(commandPublisherDisposable).ifPresent(Disposable::dispose);
    }

    // FIXME Should we rename to init() ? If so, we must ensure that subclasses' init() methods call super.init()
//    @PostConstruct
    public void coreInit() {

    	commandPublisherDisposable = remoteCommandPublisher.subscribe(t -> {
//            try {
                commandReceiver.onNext(t);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        });
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        logger.info("Seen command: " + command + " with " + data.length + " bytes");
        ByteBuffer bb = ByteBuffer.wrap(new byte[1 + data.length]).put(command).put(data);
        bb.rewind();

//        try {
            commandReceiver.onNext(bb);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

}
