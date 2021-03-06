/**
 * 
 */
package org.jsoar.performancetesting.jsoar;

import org.jsoar.performancetesting.Test;
import org.jsoar.performancetesting.TestFactory;
import org.jsoar.performancetesting.TestSettings;

/**
 * A class to create instantiations of JSoar tests.
 * 
 * @author ALT
 *
 */
public class JSoarTestFactory implements TestFactory
{
    private String label;

    private String jsoarDirectory;

    public JSoarTestFactory()
    {
        this.label = new String();
        this.jsoarDirectory = new String();
    }

    public JSoarTestFactory(String label, String jsoarDirectory)
    {
        this.label = label;
        this.jsoarDirectory = jsoarDirectory;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public void setJSoarDirectory(String jsoarDirectory)
    {
        this.jsoarDirectory = jsoarDirectory;
    }

    /**
     * This creates JSoar Tests. It takes a test's name, file, and the number of
     * decision cycles to run and returns a new test which has been created and
     * initialized with all those values.
     * 
     * @param testName
     * @param testFile
     * @param settings
     * @return A new and initialized JSoar test.
     */
    @Override
    public Test createTest(String testName, String testFile,
            TestSettings settings)
    {
        JSoarTest jsoarTest = new JSoarTest(label, jsoarDirectory);

        jsoarTest.initialize(testName, testFile, settings);

        return jsoarTest;
    }
}
