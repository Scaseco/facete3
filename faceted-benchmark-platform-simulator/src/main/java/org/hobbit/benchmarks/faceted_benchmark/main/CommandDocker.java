package org.hobbit.benchmarks.faceted_benchmark.main;

import java.util.List;

import com.spotify.docker.client.DockerClient;

/**
 * Execute a command in a docker container
 *
 * @author raven
 *
 */
public class CommandDocker {
    protected DockerClient dockerClient;
    protected String name;
    protected List<?> args;

}
