/*
 * (c) 2008  Dave Ray
 *
 * Created on Aug 22, 2008
 */
package org.jsoar;

import android.test.AndroidTestCase;

import org.jsoar.kernel.Agent;
import org.jsoar.kernel.Production;
import org.jsoar.kernel.ProductionType;
import org.jsoar.kernel.rhs.functions.RhsFunctionContext;
import org.jsoar.kernel.symbols.Identifier;
import org.jsoar.kernel.symbols.Symbol;
import org.jsoar.kernel.symbols.SymbolFactory;
import org.jsoar.kernel.symbols.SymbolFactoryImpl;
import org.jsoar.kernel.tracing.Printer;

import java.io.StringWriter;

/**
 * @author ray
 */
public class JSoarTest extends AndroidTestCase
{
    protected SymbolFactoryImpl syms;
    protected RhsFunctionContext rhsFuncContext = new RhsFunctionContext() {

        @Override
        public SymbolFactory getSymbols()
        {
            return syms;
        }

        /* (non-Javadoc)
         * @see org.jsoar.kernel.rhs.functions.RhsFunctionContext#addWme(org.jsoar.kernel.symbols.Identifier, org.jsoar.kernel.symbols.Symbol, org.jsoar.kernel.symbols.Symbol)
         */
        @Override
        public Void addWme(Identifier id, Symbol attr, Symbol value)
        {
            throw new UnsupportedOperationException("This test implementation of RhsFunctionContext doesn't support addWme");
        }

        /* (non-Javadoc)
         * @see org.jsoar.kernel.rhs.functions.RhsFunctionContext#getProductionBeingFired()
         */
        @Override
        public Production getProductionBeingFired()
        {
            return null;
        }
        
    };
    
    @Override
    public void setUp() throws Exception
    {
        this.syms = new SymbolFactoryImpl();
    }

    public static void verifyProduction(Agent agent, String name, ProductionType type, String body, boolean internal)
    {
        Production j = agent.getProductions().getProduction(name);
        assertNotNull(j);
        assertEquals(type, j.getType());
        StringWriter writer = new StringWriter();
        j.print(new Printer(writer), internal);
        assertEquals(body, writer.toString());
        
    }

}