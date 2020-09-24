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

//    @Test
    public void benchmarkCartesianProductStream() throws Exception {
        for (int i= 0; i < 1; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            long count = new BenchmarkSupplierUtils().streamBasedCartesianProduct();
            System.out.println("Stream-based cartesian product " + count + " items in " + sw);
        }
    }

//    @Test
    public void benchSupplierBasedCartesianProduct() throws Exception {
        for (int i= 0; i < 1; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            long count = new BenchmarkSupplierUtils().supplierBasedCartesianProduct();
            System.out.println("Supplier-based cartesian product " + count + " items in " + sw);
        }
    }

}
