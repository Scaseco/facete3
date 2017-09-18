package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.hobbit.core.Commands;
import org.hobbit.interfaces.BenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class DefaultCommandHandler
    implements Consumer<ByteBuffer>
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultCommandHandler.class);

    @Autowired
    protected ApplicationContext ctx;

    @Autowired
    protected ObservableByteChannel cmdQueue;

    protected Class<? extends BenchmarkController> benchmarkControllerClass;

    public DefaultCommandHandler(Class<? extends BenchmarkController> benchmarkControllerClass) {
        super();
        this.benchmarkControllerClass = benchmarkControllerClass;
    }

    public static Entry<Byte, byte[]> formatToHobbitApi(ByteBuffer buffer) {
        Entry<Byte, byte[]> result;

        int pos = buffer.position();

        int limit = buffer.limit();
        if(limit == 0) {
            result = null;
        } else {
            buffer.position(0);
            byte b = buffer.get();
            byte[] dst = new byte[limit - 1];
            buffer.get(dst, 0, dst.length);

            result = new SimpleEntry<>(b, dst);
            //buffer.position(pos);
        }

        buffer.position(pos);
        return result;
    }

    public static void forwardToHobbit(ByteBuffer buffer, BiConsumer<Byte, byte[]> consumer) {
        Entry<Byte, byte[]> e = formatToHobbitApi(buffer);
        if(e != null) {
            consumer.accept(e.getKey(), e.getValue());
        }
    }


    @Override
    public void accept(ByteBuffer t) {
        logger.info("Seen " + t.remaining() + " bytes on command queue");


        if(t.remaining() > 0) {
            if(t.get(0) == Commands.START_BENCHMARK_SIGNAL) {
                logger.info("Starting benchmark");

                // Create a new instance of the benchmark controller
                BenchmarkController benchmarkController;
                try {
                    benchmarkController = benchmarkControllerClass.newInstance();
                    ctx.getAutowireCapableBeanFactory().autowireBean(benchmarkController);

                    // Register the benchmark controller as a listener to the command queue
                    // FIXME Somehow unregister the component when done...
                    cmdQueue.addObserver(buffer -> forwardToHobbit(buffer, benchmarkController::receiveCommand));

                    benchmarkController.executeBenchmark();


                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
             }
        }

    }



}
