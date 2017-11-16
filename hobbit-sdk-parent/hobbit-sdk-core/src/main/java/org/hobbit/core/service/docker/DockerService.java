package org.hobbit.core.service.docker;

import com.google.common.util.concurrent.Service;

/**
 * A docker service at minimum exposes the imageName and - if running - the container id
 *
 * @author raven Sep 20, 2017
 *
 */
public interface DockerService
    extends Service
{
    String getContainerId();
    String getImageName();

    // Only valid on terminated services
    int getExitCode();
}
