/**
 * 
 */
package org.jsoar.performancetesting.jsoar;

import org.jsoar.performancetesting.Test;
import org.jsoar.performancetesting.TestFactory;

/**
 * This creates instantiations of JSoar tests.
 * 
 * @author ALT
 *
 */
public class JSoarTestFactory implements TestFactory
{
    /**
     * This creates JSoar Tests.  It takes a test's name, file, and
     * the number of decision cycles to run and returns a new test
     * which has been created and initialized with all those values.
     * 
     * @param testName
     * @param testFile
     * @param decisionCycles
     * @return A new and initialized JSoar test.
     */
    @Override
    public Test createTest(String testName, String testFile, Integer decisionCycles)
    {
        JSoarTest jsoarTest = new JSoarTest();
        
        jsoarTest.initialize(testName, testFile, decisionCycles);
        
        return jsoarTest;
    }
}