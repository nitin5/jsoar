package org.jsoar.kernel.commands;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.output.WriterOutputStream;
import org.jsoar.kernel.Agent;
import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.commands.DebugCommand.Debug;

import picocli.CommandLine;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.Help;
import picocli.CommandLine.IExceptionHandler2;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.RunLast;
import picocli.CommandLine.UnmatchedArgumentException;

public class Utils
{
    /**
     * Helper method intended to connect SoarCommands to the picocli parser
     * Typically called from the SoarCommand's execute method
     * Any output generated by picocli will automatically be passed to the agent's writer
     * Note that while SoarCommands is the typical use case, there was no reason to restrict this to that, so it accepts any object
     * @param agent SoarAgent executing the command
     * @param command An Object, should be annotated with picocli's @Command annotation
     * @param args The args as received by a SoarCommand's execute method (i.e., the 0th arg should be the string for the command itself)
     * @throws SoarException 
     */
    public static void parseAndRun(Agent agent, Object command, String[] args) throws SoarException {
        OutputStream os = new WriterOutputStream(agent.getPrinter().getWriter(), Charset.defaultCharset(), 1024, true);
        PrintStream ps = new PrintStream(os);
        
        parseAndRun(command, args, ps);
    }
    
    
    /**
     * Executes the specified command and returns the result.
     * A command may be a user object or a pre-initialized {@code picocli.CommandLine} object.
     * For performance-sensitive commands that are invoked often,
     * it is recommended to pass a pre-initialized CommandLine object instead of the user object.
     *
     * @param command the command to execute; this may be a user object or a pre-initialized {@code picocli.CommandLine} object
     * @param args the command line arguments (the first arg will be removed from this list)
     * @param ps the PrintStream to print any command output to
     * @return the command result
     * @throws SoarException if the user input was invalid or if a runtime exception occurred
     *                      while executing the command business logic
     */
    public static List<Object> parseAndRun(Object command, String[] args, PrintStream ps) throws SoarException {
        
        CommandLine commandLine = command instanceof CommandLine
                ? (CommandLine) command
                : new CommandLine(command);
        
        // The "debug time" command takes a command as a parameter, which can contain options
        // In order to inform picocli that the options are part of the command parameter
        // the following boolean must be set to true
        if (command.getClass() == Debug.class)
        {
            commandLine.setUnmatchedOptionsArePositionalParams(true);
        }
        
        try {
            return commandLine.parseWithHandlers(
                    new RunLast().useOut(ps),
                    new ExceptionHandler(),
                    Arrays.copyOfRange(args, 1, args.length)); // picocli expects the first arg to be the first arg of the command, but for SoarCommands its the name of the command, so get the subarray starting at the second arg
        }
        catch(Exception e) {
            throw new SoarException(e.getMessage(), e); // rethrow all exceptions as SoarExceptions
        }
    }
    
    public static String parseAndRun(Object command, String[] args) throws SoarException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
            List<Object> results = parseAndRun(command, args, ps);
            if(results != null) {
                for(Object o : results) {
                    if(o != null) ps.print(o.toString());
                }
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new SoarException(e);
        }
        final String result = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        return result;
    }
    
    /**
     * This throws all exceptions, so we can catch them above and re-throw as SoarExceptions
     * @author bob.marinier
     *
     */
    protected static class ExceptionHandler implements IExceptionHandler2<List<Object>> {

        /**
         * For parameter exceptions, throw a new exception that includes the help text (which will get printed elsewhere)
         */
        @Override
        public List<Object> handleParseException(ParameterException ex, String[] args)
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
            
                ps.println(ex.getMessage());
                if (!UnmatchedArgumentException.printSuggestions(ex, ps)) {
                    ex.getCommandLine().usage(ps, Help.Ansi.AUTO);
                }
                final String result = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                throw new RuntimeException(result, ex);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * For execution exceptions, just rethrow without printing help
         */
        @Override
        public List<Object> handleExecutionException(ExecutionException ex,
                ParseResult parseResult)
        {
            throw ex;
        } 
    }
}
