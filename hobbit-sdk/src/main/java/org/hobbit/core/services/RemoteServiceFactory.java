package org.hobbit.core.services;

import java.util.Map;

import com.google.common.util.concurrent.Service;

/**
 * Factory for services that are started remotely.
 *
 * There are two ways to determine the remote service state
 * - periodic polling -> some method similar to AbstractScheduledService
 * - waiting for push messages
 *
 *
 *
 *
 * Remote service
 */
public class RemoteServiceFactory {

    // Maps service id to service objects (may be client stubs)
    protected Map<String, Service> idToService;

    void
    //onServiceShutdown()
}
