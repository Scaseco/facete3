#!/bin/bash
# Test script; should become consolidated into either a unit test or at least a test main class

export HOBBIT_RABBIT_HOST=localhost


java -cp faceted-benchmark-benchmark-controller/target/faceted-benchmark-benchmark-controller-1.0.0-SNAPSHOT.jar org.hobbit.core.run.ComponentStarter org.hobbit.benchmark.FacetedBenchmarkController
