/*
 * Copyright (c) 2009 Dave Ray <daveray@gmail.com>
 *
 * Created on Oct 14, 2009
 */
package org.jsoar.legilimens.trace;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.properties.PropertyKey;
import org.jsoar.util.properties.PropertyProvider;

/**
 * @author ray
 */
public class AgentTraceState
{
    private static final Log logger = LogFactory.getLog(AgentTraceState.class);
    
    public static final PropertyKey<AgentTraceState> KEY = PropertyKey.builder("legilimens.trace", AgentTraceState.class).readonly(true).build();
    
    private final ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
    private final OutputStreamWriter writer = new OutputStreamWriter(output);
    
    public static AgentTraceState attach(ThreadedAgent agent)
    {
        final AgentTraceState ats = new AgentTraceState(agent);
        agent.getProperties().setProvider(KEY, new PropertyProvider<AgentTraceState>()
        {

            @Override
            public AgentTraceState get()
            {
                return ats;
            }

            @Override
            public AgentTraceState set(AgentTraceState value)
            {
                return null;
            }
        });
        
        return ats;
    }
    
    private AgentTraceState(ThreadedAgent agent)
    {
        agent.getPrinter().addPersistentWriter(writer);
    }
    
    public int getSize()
    {
        return output.size();
    }
    
    public byte[] getBytes()
    {
        return output.toByteArray();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return output.size() + " bytes written";
    }

}
