/**
 * 
 */
package org.jsoar.performancetesting;

/**
 * @author ALT
 *
 */
public interface TestFactory
{
    /**
     * The main worker function of the factory.  It creates new and initialized tests from the
     * passed parameters.
     * 
     * @param testName
     * @param testFile
     * @param decisionCycles
     * @return A new instance of a class which has already been created and initialized.
     */
    public Test createTest(String testName, String testFile, Integer decisionCycles);
}