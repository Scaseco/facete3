package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.hobbit.core.Commands;
import org.hobbit.interfaces.BenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class PseudoHobbitPlatformController
    implements Consumer<ByteBuffer>
{
    private static final Logger logger = LoggerFactory.getLogger(PseudoHobbitPlatformController.class);

    @Autowired
    protected ApplicationContext ctx;

    @Autowired
    protected ObservableByteChannel cmdQueue;

    protected Class<? extends BenchmarkController> benchmarkControllerClass;

    public PseudoHobbitPlatformController(Class<? extends BenchmarkController> benchmarkControllerClass) {
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

                    Consumer<ByteBuffer> observer = buffer -> forwardToHobbit(buffer, benchmarkController::receiveCommand);

                    try {
                        // Register the benchmark controller as a listener to the command queue
                        cmdQueue.addObserver(observer);

                        benchmarkController.executeBenchmark();
                    } finally {
                        IOUtils.closeQuietly(benchmarkController);

                        // After the benchmark controller served its purpose, deregister it from events
                        cmdQueue.removeObserver(observer);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
             }
        }

    }





}
