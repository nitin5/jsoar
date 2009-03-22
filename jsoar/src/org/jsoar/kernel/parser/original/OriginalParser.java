/*
 * Copyright (c) 2009  Dave Ray <daveray@gmail.com>
 *
 * Created on Jan 17, 2009
 */
package org.jsoar.kernel.parser.original;

import java.io.IOException;
import java.io.Reader;

import org.jsoar.kernel.Production;
import org.jsoar.kernel.VariableGenerator;
import org.jsoar.kernel.parser.Parser;
import org.jsoar.kernel.parser.ParserContext;
import org.jsoar.kernel.parser.ParserException;
import org.jsoar.kernel.rhs.functions.RhsFunctionManager;
import org.jsoar.kernel.symbols.SymbolFactoryImpl;
import org.jsoar.kernel.tracing.Printer;
import org.jsoar.util.adaptables.AbstractAdaptable;
import org.jsoar.util.adaptables.Adaptables;

/**
 * @author ray
 */
public class OriginalParser extends AbstractAdaptable implements Parser
{

    /* (non-Javadoc)
     * @see org.jsoar.kernel.parser.Parser#parseProduction(org.jsoar.kernel.parser.ParserContext, java.io.Reader)
     */
    @Override
    public Production parseProduction(ParserContext context, Reader reader) throws ParserException
    {
        final Printer printer = require(context, Printer.class);
        final SymbolFactoryImpl syms = require(context, SymbolFactoryImpl.class);
        final VariableGenerator vg = new VariableGenerator(syms);
        final RhsFunctionManager rhsFunctions = require(context, RhsFunctionManager.class);
        try
        {
            Lexer lexer = new Lexer(printer, reader);
            OriginalParserImpl parser = new OriginalParserImpl(vg, lexer);
            parser.setRhsFunctions(rhsFunctions);
            lexer.getNextLexeme();
            return parser.parseProduction();
        }
        catch (IOException e)
        {
            throw new ParserException(e);
        }
    }
    
    private static <T> T require(ParserContext context, Class<T> klass)
    {
        final T t = Adaptables.adapt(context, klass);
        if(t == null)
        {
            throw new IllegalStateException(OriginalParser.class.getName() + " requires " + klass.getCanonicalName());
        }
        return t;
    } 
}