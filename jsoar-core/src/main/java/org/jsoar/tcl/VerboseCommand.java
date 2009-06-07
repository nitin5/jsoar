/*
 * Copyright (c) 2009 Dave Ray <daveray@gmail.com>
 *
 * Created on Jun 6, 2009
 */
package org.jsoar.tcl;

import org.jsoar.kernel.Agent;
import org.jsoar.kernel.tracing.Trace.Category;

import tcl.lang.TclException;

/**
 * @author ray
 */
public class VerboseCommand extends AbstractToggleCommand
{

    /**
     * @param agent
     */
    public VerboseCommand(Agent agent)
    {
        super(agent);
    }

    /* (non-Javadoc)
     * @see org.jsoar.tcl.AbstractToggleCommand#execute(org.jsoar.kernel.Agent, boolean)
     */
    @Override
    protected void execute(Agent agent, boolean enable) throws TclException
    {
        agent.getTrace().setEnabled(Category.VERBOSE, enable);
    }
}
