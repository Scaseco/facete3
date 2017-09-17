package org.hobbit.benchmarks.faceted_browsing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class SimpleProcessExecutor {
    protected ProcessBuilder processBuilder;
    protected Consumer<String> outputSink;
    protected UnaryOperator<Consumer<String>> similarityRemover;

    protected boolean isService;

    public SimpleProcessExecutor(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
        this.outputSink = System.out::println;
        this.similarityRemover = (dest) -> OmitSimilarItems.forStrings(6, dest);
    }

    public Consumer<String> getOutputSink() {
        return outputSink;
    }

    public SimpleProcessExecutor setOutputSink(Consumer<String> outputSink) {
        this.outputSink = outputSink;
        return this;
    }

    public UnaryOperator<Consumer<String>> getSimilarityRemover() {
        return similarityRemover;
    }

    public SimpleProcessExecutor setSimilarityRemover(UnaryOperator<Consumer<String>> similarityRemover) {
        this.similarityRemover = similarityRemover;
        return this;
    }

    public boolean isService() {
        return isService;
    }

    public SimpleProcessExecutor setService(boolean isService) {
        this.isService = isService;
        return this;
    }

    public void watchProcessOutput(Process p) {
        try {
            Consumer<String> sink = similarityRemover == null
                    ? outputSink
                    : similarityRemover.apply(outputSink);


            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while((line = br.readLine()) != null) {
                sink.accept(line);
            }

            p.waitFor();
            int exitValue = p.exitValue();
            sink.accept("Process terminated with exit code " + exitValue);
        }
        catch(IOException e) {
            // Probably just the process died, so ignore
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Process execute() throws IOException, InterruptedException {
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();

        if(isService) {
            new Thread(() -> watchProcessOutput(p)).start();
        } else {
            watchProcessOutput(p);
        }

        return p;
    }

    public static SimpleProcessExecutor wrap(ProcessBuilder processBuilder) {
        return new SimpleProcessExecutor(processBuilder);
    }
}