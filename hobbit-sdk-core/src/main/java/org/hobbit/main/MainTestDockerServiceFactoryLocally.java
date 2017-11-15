package org.hobbit.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactoryDockerClient;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

public class MainTestDockerServiceFactoryLocally {
    public static void main(String[] args) throws DockerCertificateException, InterruptedException, TimeoutException {
        DockerClient dockerClient = DefaultDockerClient.fromEnv().build();


//        DefaultDockerClient.builder().s

        // Bind container port 443 to an automatically allocated available host
        // port.
        String[] ports = { "80", "22" };
        Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

        List<PortBinding> randomPort = new ArrayList<>();
        randomPort.add(PortBinding.randomPort("0.0.0.0"));
        portBindings.put("443", randomPort);

        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        DockerServiceFactoryDockerClient dockerServiceFactory = new DockerServiceFactoryDockerClient();

        ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder()
                .hostConfig(hostConfig);
//        	    .image("busybox").exposedPorts(ports)
//        	    .cmd("sh", "-c", "while :; do sleep 1; done")

        DockerService service = dockerServiceFactory
            .setDockerClient(dockerClient)
            .setContainerConfigBuilder(containerConfigBuilder)
            .setImageName("busybox")
            .get();

        Map<String, String> env = ImmutableMap.<String, String>builder()
                .put("foo", "bar")
                .put("baz", "")
                .build();

        dockerServiceFactory.setLocalEnvironment(env);




        service.startAsync();
        service.awaitRunning(60, TimeUnit.SECONDS);

        System.out.println("Image name: " + service.getImageName());
        System.out.println("Container Id: " + service.getContainerId());
        System.out.println("Env: " + dockerServiceFactory.getLocalEnvironment());

        Thread.sleep(5000);

        service.stopAsync();
        service.awaitTerminated(60, TimeUnit.SECONDS);
    }
}
