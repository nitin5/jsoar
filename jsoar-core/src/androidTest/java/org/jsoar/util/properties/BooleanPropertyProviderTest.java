package org.jsoar.util.properties;
import android.test.AndroidTestCase;

/*
 * Copyright (c) 2008  Dave Ray <daveray@gmail.com>
 *
 * Created on Dec 23, 2008
 */

/**
 * @author ray
 */
public class BooleanPropertyProviderTest extends AndroidTestCase
{

    /**
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    public void tearDown() throws Exception
    {
    }

    /**
     * Test method for {@link org.jsoar.util.properties.BooleanPropertyProvider#toString()}.
     */
    public void testToString()
    {
        final PropertyKey<Boolean> key = PropertyKey.builder("testToString", Boolean.class).defaultValue(true).build();
        final BooleanPropertyProvider provider = new BooleanPropertyProvider(key);
        assertEquals("true", provider.toString());
        provider.set(false);
        assertEquals("false", provider.toString());
    }

}