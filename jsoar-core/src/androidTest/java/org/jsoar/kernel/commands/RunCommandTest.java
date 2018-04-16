/*
 * Copyright (c) 2010 Dave Ray <daveray@gmail.com>
 *
 * Created on May 19, 2010
 */
package org.jsoar.kernel.commands;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.jsoar.kernel.AgentRunController;
import org.jsoar.kernel.Phase;
import org.jsoar.kernel.RunType;
import org.jsoar.kernel.SoarException;
import org.jsoar.util.commands.DefaultSoarCommandContext;


public class RunCommandTest extends AndroidTestCase
{
    private MockRunControl mock;
    private RunCommand command;
    
    private static class MockRunControl implements AgentRunController
    {
        long count = -1;
        RunType runType;
        
        /* (non-Javadoc)
         * @see org.jsoar.kernel.AgentRunController#runFor(long, org.jsoar.kernel.RunType)
         */
        @Override
        public void runFor(long n, RunType runType)
        {
            this.count = n;
            this.runType = runType;
        }

        @Override
        public Phase getStopPhase()
        {
            throw new UnsupportedOperationException("getStopPhase not implemented in this test mock");
        }

        @Override
        public void setStopPhase(Phase phase)
        {
            throw new UnsupportedOperationException("setStopPhase not implemented in this test mock");
        }
    }
    
    @Override
    public void setUp()
    {
        this.mock = new MockRunControl();
        this.command = new RunCommand(mock);
    }
    
    public void testThrowsExceptionOnNonNumericCount()
    {
        try {
            execute("run", "-d", "xyz");
            fail("Should have thrown exception");
        } catch (SoarException e) {
            Assert.assertEquals("Expected integer for run count, got 'xyz'", e.getMessage());
        }
    }
    
    public void testThrowsExceptionOnZeroCount()
    {
        try {
            execute("run", "-e", "0");
            fail("Should have thrown exception");
        } catch (SoarException e) {
            Assert.assertEquals("Expected count larger than 0 for run command, got 0", e.getMessage());
        }
    }
    
    public void testThrowsExceptionNegativeCount()
    {
        try {
            execute("run", "-e", "-10");
            fail("Should have thrown exception");
        } catch (SoarException e) {
            Assert.assertEquals("Unknow option '-10'", e.getMessage());
        }
    }
    
    public void testThrowsExceptionWhenMultipleCountsGiven()
    {
        try {
            execute("run", "5", "-e", "10");
            fail("Should have thrown exception");
        } catch (SoarException e) {
            Assert.assertEquals("Multiple counts given for run command: run 5 -e 10",e.getMessage());
        }
    }
    
    public void testThrowsExceptionWhenMultipleRunTypesGiven()
    {
        try {
            execute("run", "-d", "-e");
            fail("Should have thrown exception");
        } catch (SoarException e) {
            Assert.assertEquals("Multiple run types specified, DECISIONS and ELABORATIONS", e.getMessage());
        }
    }
    
    public void testDefaultsToRunForever() throws Exception
    {
        execute("run");
        verify(1, RunType.FOREVER);
    }
    
    public void testRunForever() throws Exception
    {
        execute("run", "-f");
        verify(1, RunType.FOREVER);
        execute("run", "--forever");
        verify(1, RunType.FOREVER);
    }
    
    public void testCountDefaultsToOneDecisionIfNoIntegerArgumentIsGiven() throws Exception
    {
        execute("run", "-d");
        verify(1, RunType.DECISIONS);
        execute("run", "--decision");
        verify(1, RunType.DECISIONS);
    }
    public void testCountDefaultsToOneElaborationIfNoIntegerArgumentIsGiven() throws Exception
    {
        execute("run", "-e");
        verify(1, RunType.ELABORATIONS);
        execute("run", "--elaboration");
        verify(1, RunType.ELABORATIONS);
    }
    public void testCountDefaultsToOnePhaseIfNoIntegerArgumentIsGiven() throws Exception
    {
        execute("run", "-p");
        verify(1, RunType.PHASES);
        execute("run", "--phase");
        verify(1, RunType.PHASES);
    }
    
    public void testCountDecisions() throws Exception
    {
        execute("run", "-d", "99");
        verify(99, RunType.DECISIONS);
        execute("run", "--decision", "99");
        verify(99, RunType.DECISIONS);
    }
    
    public void testCountDecisionsReversed() throws Exception
    {
        execute("run", "99", "-d");
        verify(99, RunType.DECISIONS);
        execute("run", "99", "--decision");
        verify(99, RunType.DECISIONS);
    }
    
    public void testCountElaborations() throws Exception
    {
        execute("run", "-e", "100");
        verify(100, RunType.ELABORATIONS);
        execute("run", "--elaboration", "100");
        verify(100, RunType.ELABORATIONS);
    }
    
    public void testCountElaborationsReversed() throws Exception
    {
        execute("run", "100", "-e");
        verify(100, RunType.ELABORATIONS);
        execute("run", "100", "--elaboration");
        verify(100, RunType.ELABORATIONS);
    }

    
    public void testCountPhases() throws Exception
    {
        execute("run", "-p", "7654321");
        verify(7654321, RunType.PHASES);
        execute("run", "--phase", "7654321");
        verify(7654321, RunType.PHASES);
    }
   
    public void testCountPhasesReversed() throws Exception
    {
        execute("run", "7654321", "-p");
        verify(7654321, RunType.PHASES);
        execute("run", "7654321", "--phase");
        verify(7654321, RunType.PHASES);
    }
    
    public void testCountOutputMods() throws Exception
    {
        execute("run", "-o", "7654321");
        verify(7654321, RunType.MODIFICATIONS_OF_OUTPUT);
        execute("run", "--output", "7654321");
        verify(7654321, RunType.MODIFICATIONS_OF_OUTPUT);
    }
    
    public void testCountOutputModsReversed() throws Exception
    {
        execute("run", "7654321", "-o");
        verify(7654321, RunType.MODIFICATIONS_OF_OUTPUT);
        execute("run", "7654321", "--output");
        verify(7654321, RunType.MODIFICATIONS_OF_OUTPUT);
    }
    
    ////////////////////////////////////////////////////////////////////////
    
    private void execute(String... args) throws SoarException
    {
        mock.count = -1;
        mock.runType = null;
        command.execute(DefaultSoarCommandContext.empty(), args);
    }
    private void verify(long count, RunType runType)
    {
        assertEquals(count, mock.count);
        assertEquals(runType, mock.runType);
    }
}
