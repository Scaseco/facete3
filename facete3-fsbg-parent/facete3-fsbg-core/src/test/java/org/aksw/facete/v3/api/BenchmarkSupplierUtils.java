package org.aksw.facete.v3.api;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hobbit.benchmark.faceted_browsing.v2.main.SupplierUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

//@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
//@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
//@BenchmarkMode(Mode.AverageTime)
//@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@Fork(0)
//@State(Scope.Benchmark)
public class BenchmarkSupplierUtils {
    int n = 7;  // columns
    int m = 10; // rows

    public static Entry<?, ?> reduceToNestedPairs(Object accumulator, int i, int j) {
        return new SimpleEntry<>(accumulator, j);
    }

    public static interface IndexedReducer<A, V> {
        public A reduce(A acc, int i, V value);
    }

    @Benchmark
//    @Fork(value = 1, warmups = 2)
    @BenchmarkMode(Mode.Throughput)
    public long streamBasedCartesianProduct() {
        long count = simpleRecursionStream(n, null, BenchmarkSupplierUtils::reduceToNestedPairs).count();
        return count;
    }

    public long supplierBasedCartesianProduct() throws Exception {
        Callable<?> callable = simpleRecursionSupplier(n, new Object(), BenchmarkSupplierUtils::reduceToNestedPairs);
        long count = 0;
        while (callable.call() != null) {
            ++count;
        }

        return count;
    }

    public <A> Stream<A> simpleRecursionStream(int i, A initAcc, IndexedReducer<A, Integer> reducer) {
        if (i <= 0) {
            return Stream.of(initAcc);
        } else {
            return IntStream.range(0, m).boxed().flatMap(j -> {
                A nextAcc = reducer.reduce(initAcc, i, j);

                return simpleRecursionStream(i - 1, nextAcc, reducer);
            });
        }
    }

    public <A> Callable<A> simpleRecursionSupplier(int i, A initAcc, IndexedReducer<A, Integer> reducer) {
        if (i <= 0) {
            return SupplierUtils.just(initAcc);
        } else {
//            Callable<Integer> seq = SupplierUtils.from(new AbstractIterator<Integer>() {
//                int j = 0;
//                @Override
//                protected Integer computeNext() {
//                    return j >= m ? null : j++;
//                }
//            });
            Callable<Integer> seq = SupplierUtils.from(IntStream.range(0, m).iterator());

            return SupplierUtils.flatMap(SupplierUtils.map(seq, j -> {
                A nextAcc = reducer.reduce(initAcc, i, j);

                Callable<A> tmp = simpleRecursionSupplier(i - 1, nextAcc, reducer);
                return tmp;
            }));
        }
    }



//  public <A> List<?> simpleLoops(A initAcc, IndexedReducer<A, Integer> reducer) {
//      List<Object> result = new ArrayList<>();
//      A acc = initAcc;
//      for(int i = 0; i < n; ++i) {
//          A nextAcc = null;
//      	for(int j = 0; j < m; ++j) {
//
//          }
//      }
//
//      return result;
//  }


//    @Test
//    public void testFlatMapPerformance2() {
//
//
//        IntStream.range(0, n).flatMap(i -> IntStream.range(0, m).map(j -> reduce(i, j)));
//    }

}
