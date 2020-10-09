package org.aksw.facete.v3.api;

import java.util.Arrays;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
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

//    public static long reduceToSum(Long accumulator, int i, int j) {
//        return ++accumulator;
//    }

    public static interface IndexedReducer<A, V> {
        public A reduce(A acc, int i, V value);
    }


    public static <T> Stream<T> appendAction(Stream<? extends T> stream, Runnable runnable) {
        Stream<T> result = Stream.concat(
                stream,
                Stream
                    .of((T)null)
                    .filter(x -> {
                        runnable.run();
                        return false;
                    })
                );
        return result;
    }

    public static void takeOut(int arr[], int len, int idx) {
        for(int i = idx; i < len - 1; ++i) {
            arr[i] = arr[i + 1];
        }
    }

    public static void putBack(int arr[], int len, int idx, int value) {
        // shift right
        for(int i = len - 1; i > idx; --i) {
            arr[i] = arr[i - 1];
        }

        arr[idx] = value;
    }





    @Benchmark
//    @Fork(value = 1, warmups = 2)
    @BenchmarkMode(Mode.Throughput)
    public long streamBasedCartesianProduct() {
        long count = simpleRecursionStream(n, 0l, BenchmarkSupplierUtils::reduceToNestedPairs).count();
        return count;
    }

    public long streamBasedCartesianProductSmartLeafs() {
        long count = simpleRecursionStreamSmartLeafs(n, 0l, BenchmarkSupplierUtils::reduceToNestedPairs).count();
        return count;
    }

    public long supplierBasedCartesianProduct() throws Exception {
        Callable<?> callable = simpleRecursionSupplier(n, 0l, BenchmarkSupplierUtils::reduceToNestedPairs);
        long count = 0;
        while (callable.call() != null) {
            ++count;
        }

        return count;
    }


    public long cartesianProductArrayShift() {
        int[] arr = new int[n];
        Arrays.fill(arr, m);

        return simpleRecursionArrayShift(arr, arr.length, 0l, BenchmarkSupplierUtils::reduceToNestedPairs).count();
    }

    public <A> Stream<A> simpleRecursionArrayShift(int[] remaining, int remainingLen, A initAcc, IndexedReducer<A, Integer> reducer) {
        if (remainingLen <= 0) {
            return Stream.of(initAcc);
        } else {
            int pickIdx = remainingLen >> 1;
            int val = remaining[pickIdx];
            takeOut(remaining, remainingLen, pickIdx);

            return IntStream.range(0, val).boxed().flatMap(j -> {
                A nextAcc = reducer.reduce(initAcc, pickIdx, j);

                return appendAction(simpleRecursionArrayShift(remaining, remainingLen - 1, nextAcc, reducer),
                        () -> {
                            putBack(remaining, remainingLen - 1, pickIdx, val);
                        });
            });
        }
    }

    public long cartesianProductArrayCopy() {
        int[] arr = new int[n];
        Arrays.fill(arr, m);

        return simpleRecursionArrayCopy(arr, 0l, BenchmarkSupplierUtils::reduceToNestedPairs).count();
    }

    public <A> Stream<A> simpleRecursionArrayCopy(int[] remaining, A initAcc, IndexedReducer<A, Integer> reducer) {
        if (remaining.length <= 0) {
            return Stream.of(initAcc);
        } else {
            int pickIdx = remaining.length >> 1;
            int val = remaining[pickIdx];
            int[] nextRemaining = ArrayUtils.remove(remaining, pickIdx);
            return IntStream.range(0, val).boxed().flatMap(j -> {
                A nextAcc = reducer.reduce(initAcc, pickIdx, j);

                return simpleRecursionArrayCopy(nextRemaining, nextAcc, reducer);
            });
        }
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

    public <A> Stream<A> simpleRecursionStreamSmartLeafs(int i, A initAcc, IndexedReducer<A, Integer> reducer) {
        if (i <= 0) {
            return Stream.of(initAcc);
        } else {
            if(i - 1 == 0) {
                return IntStream.range(0, m).boxed().map(j -> {
                    A nextAcc = reducer.reduce(initAcc, i, j);

                    return nextAcc;
                });
            } else {
                return IntStream.range(0, m).boxed().flatMap(j -> {
                    A nextAcc = reducer.reduce(initAcc, i, j);

                    return simpleRecursionStreamSmartLeafs(i - 1, nextAcc, reducer);
                });
            }
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

            return SupplierUtils.flatMap(() -> {
                Integer j = seq.call();
                Callable<A> r = null;
                if (j != null) {
                    A nextAcc = reducer.reduce(initAcc, i, j);
                    r = simpleRecursionSupplier(i - 1, nextAcc, reducer);
                }
                return r;
            });
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
