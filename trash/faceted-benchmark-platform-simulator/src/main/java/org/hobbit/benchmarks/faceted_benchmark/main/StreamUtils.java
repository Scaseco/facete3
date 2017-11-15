package org.hobbit.benchmarks.faceted_benchmark.main;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.stream.Stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;

public class StreamUtils {
    public static void writeObjectStream(OutputStream out, Stream<?> stream, Kryo kryo, boolean closeWhenDone) {
        try {
            Output output = new Output(out);
            stream.forEach(item -> {
                //System.out.println("Wrote item "  + item);
                kryo.writeClassAndObject(output, item);
            });
            output.flush();
        } finally {
            if(closeWhenDone) {
                try {
                    out.close();
                } catch(Exception e) {
                    throw new RuntimeException();
                }
            }
        }
    }

    public static <T> Stream<T> readObjectStream(InputStream in, Kryo kryo) {
        Input input = new Input(in);

        Iterator<Object> it = new AbstractIterator<Object>() {
            @Override
            protected Object computeNext() {
                Object r = input.eof()
                        ? endOfData()
                        : kryo.readClassAndObject(input);
                return r;
            }
        };

        @SuppressWarnings("unchecked")
        Stream<T> result = Streams.stream(it).map(x -> (T)x);
        result.onClose(() -> { try { in.close(); } catch(Exception e) { throw new RuntimeException(e); }});

        return result;
    }

}
