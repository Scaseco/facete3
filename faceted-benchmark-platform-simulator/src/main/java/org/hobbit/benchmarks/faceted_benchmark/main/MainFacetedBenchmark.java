package org.hobbit.benchmarks.faceted_benchmark.main;

import java.util.Arrays;
import java.util.Map;

import org.hobbit.core.Constants;
import org.hobbit.core.TestConstants;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;

public class MainFacetedBenchmark {
    public static void main(String[] args) throws DockerCertificateException, DockerException, InterruptedException {

        String sessionId = "session1";

        EnvironmentVariables environmentVariables = new EnvironmentVariables();
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, TestConstants.RABBIT_HOST);
        environmentVariables.set(Constants.GENERATOR_ID_KEY, "0");
        environmentVariables.set(Constants.GENERATOR_COUNT_KEY, "1");
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, "0");
        environmentVariables.set(Constants.BENCHMARK_PARAMETERS_MODEL_KEY,
                "{ \"@id\" : \"http://w3id.org/hobbit/experiments#New\", \"@type\" : \"http://w3id.org/hobbit/vocab#Experiment\" }");
        environmentVariables.set(Constants.HOBBIT_EXPERIMENT_URI_KEY, Constants.EXPERIMENT_URI_NS + sessionId);

        environmentVariables.set(Constants.CONTAINER_NAME_KEY, "myLocalContainer");

        //ComponentStarter.main(new String[]{"org.hobbit.benchmark.FacetedBenchmarkController"});




        DockerClient dockerClient = DefaultDockerClient.fromEnv().build();

        String containerName = "my-podigg-container";
        //dockerClient.stopContainer(containerName);
        dockerClient.removeContainer(containerName);


        ContainerConfig.Builder cfgBuilder = ContainerConfig.builder();
        cfgBuilder.image("podigg/podigg-lc");
        cfgBuilder.hostname(containerName);

//        cfgBuilder.env(Arrays.asList("HOBBIT_CONTAINER_NAME=riak-kv-eb1cb01e6b1348bc9dff895d29f7fa29",
//                "CLUSTER_NAME=riakkv", "HOBBIT_RABBIT_HOST=rabbit", "HOBBIT_SESSION_ID=1481122628848",
//                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin", "OS_FAMILY=ubuntu",
//                "OS_VERSION=14.04", "OS_FLAVOR=trusty", "DEBIAN_FRONTEND=noninteractive",
//                "DEBCONF_NONINTERACTIVE_SEEN=true", "RIAK_VERSION=2.1.4", "RIAK_FLAVOR=KV", "RIAK_HOME=/usr/lib/riak"));

        // trigger creation

        ContainerConfig cfg = cfgBuilder.build();
        try {
            ContainerCreation resp = dockerClient.createContainer(cfg, containerName);
            String containerId = resp.id();
            // disconnect the container from every network it might be connected
            // to
            ContainerInfo info = dockerClient.inspectContainer(containerId);
            Map<String, AttachedNetwork> networks = info.networkSettings().networks();
            for (String networkName : networks.keySet()) {
                dockerClient.disconnectFromNetwork(containerId, networkName);
            }
            // connect to hobbit network
            dockerClient.connectToNetwork(resp.id(), "hobbit");
            // return new container id
            dockerClient.startContainer(containerId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dockerClient.close();

    }
}
