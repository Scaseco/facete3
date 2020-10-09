package org.aksw.facete.v3.api;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.base.Stopwatch;

public class TestSupplierUtils {
//    @Test
    public void benchmarkSupplierUtilsNotYetWorking() throws Exception {
        Options opt = new OptionsBuilder()
                .include(BenchmarkSupplierUtils.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }


    int iterations = 1;


    @Test
    public void benchStreamBasedCartesianProductWithArrayShift() throws Exception {
        for (int i= 0; i < iterations; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            long count = new BenchmarkSupplierUtils().cartesianProductArrayShift();
            System.out.println("Cartesian product array shift " + count + " items in " + sw);
        }
    }

    @Test
    public void benchStreamBasedCartesianProductWithArrayCopy() throws Exception {
        for (int i= 0; i < iterations; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            long count = new BenchmarkSupplierUtils().cartesianProductArrayCopy();
            System.out.println("Cartesian product array copy " + count + " items in " + sw);
        }
    }

    @Test
    public void benchSupplierBasedCartesianProduct() throws Exception {
        for (int i= 0; i < iterations; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            long count = new BenchmarkSupplierUtils().supplierBasedCartesianProduct();
            System.out.println("Supplier-based cartesian product " + count + " items in " + sw);
        }
    }

    @Test
    public void benchmarkCartesianProductStream() throws Exception {
        for (int i= 0; i < iterations; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            long count = new BenchmarkSupplierUtils().streamBasedCartesianProduct();
            System.out.println("Stream-based cartesian product " + count + " items in " + sw);
        }
    }

    @Test
    public void benchmarkCartesianProductStreamSmartLeafs() throws Exception {
        for (int i= 0; i < iterations; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            long count = new BenchmarkSupplierUtils().streamBasedCartesianProductSmartLeafs();
            System.out.println("Stream-based cartesian product smart leafs " + count + " items in " + sw);
        }
    }

}
