package org.hobbit.system;

import org.hobbit.core.components.AbstractSystemAdapter;

public abstract class AbstractSparqlBasedSystemAdapter
    extends AbstractSystemAdapter
{
    public abstract receiveDataStream(InputStream in) {

    }

    

    /**
     * Data events are interpreted as (bulk) loading requests.
     *
     *
     *
     */
    @Override
    public void receiveGeneratedData(byte[] bytes) {
        
        // Make sure the message indicates a streaming protocol
        if(streamManager.isStartOfStream(bytes)) {
            streamManager.newStream();
            //InputStream in = new InputStreamChunkedTransfer();
            //receiveDataStream();
        }
        
        streamManager.handle(bytes);
    }

    /**
     * Task requestes are simply SPARQL query strings
     */
    @Override
    public void receiveGeneratedTask(String arg0, byte[] arg1) {
    }
}

