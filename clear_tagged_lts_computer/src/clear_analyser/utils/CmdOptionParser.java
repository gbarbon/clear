package clear_analyser.utils;

import org.apache.commons.cli.*;


/**
 * Created by gbarbon.
 */
public class CmdOptionParser {
    private Options options;
    private CommandLineParser parser;
    private String[] args;

    public CmdOptionParser(String[] args) {
        options = optionParserGenerator();
        parser = new DefaultParser();
        this.args = args;
    }

    private static void printMan(Options options) {
        String syntax = "java GraphSimpMain [OPTIONS] DIR INPUT_COMPLETE_GRAPH [INPUT_BAD_GRAPH] " +
                "PROP";
        String syntax_liveness = "java GraphSimpMain [OPTIONS] DIR INPUT_COMPLETE_GRAPH " +
                "[PROP]";
        syntax = syntax + "\n" + syntax_liveness;
        String header = "\nOptions are :";
        String footer = "";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(80, syntax, header, options, footer);
    }

    public CommandLine parser() {
        try {
            CommandLine line = parser.parse(options, args);
            if (!checkingOptionsAndArguments(line))
                throw new ParseException("");
            return line;
        } catch (ParseException exp) {
            //System.err.println( "Command line option parsing failed: " + exp.getMessage() );
            printMan(options);
            return null;
        }
    }

    private Options optionParserGenerator() {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Show help");
        Option fastMatching = new Option("f", "fast", false, "Fast matching");
        // TODO: testMode triggers all the testing functions, not implemented
        // Option testMode = new Option("t", false, "Trigger Testing Mode");
        Option shortestCexp = new Option("s", "shortest-cexp", false, "Shortest counterexample " +
                "generation");
        Option neo4j = new Option("n", "neo4j", false, "Neo4J graph generation");
        Option sccTest = new Option("c", "scc", false, "Tarjan algortihm to test SCC");
        Option simpleInevitability = new Option("l", "simpleinev", false, "Simple " +
                "Inevitability Liveness Mode");
        //Option cascadeliveness = new Option("s", "cascadeliveness", false, "Cascade
        // Liveness Mode");
        Option nestedInevitabilities = new Option("p", "nestedinev", false, "Nested " +
                "Inevitabilities Liveness Mode");
        Option deadlocklivelock = new Option("d", "deadlocklivelock", false,
                "deadlocklivelock " +
                        "Mode");
        Option testNestedInevitabilities = new Option("t", "testnestedinev", false,
                "Test Nested Inevitabilities Liveness Mode");

        Option action = Option.builder("a")
                .longOpt("action")
                .desc("Inevitable action")
                .hasArg()
                .argName("action-name")
                .build();

        Option actions = Option.builder("x")
                .longOpt("actions")
                .desc("Inevitable actions in cascade")
                .hasArgs()
                .argName("actions-names")
                .valueSeparator(',')
                .build();

        options.addOption(help);
        options.addOption(fastMatching);
        options.addOption(shortestCexp);
        options.addOption(neo4j);
        options.addOption(sccTest);
        options.addOption(action);
        options.addOption(simpleInevitability);
        //options.addOption(cascadeliveness);
        options.addOption(actions);
        options.addOption(nestedInevitabilities);
        options.addOption(deadlocklivelock);
        options.addOption(testNestedInevitabilities);

        return options;
    }

    private boolean checkingOptionsAndArguments(CommandLine line) {

        // TODO: add for deadlock

        // checking arguments
        if (line.hasOption('c')) {
            if (line.getArgs().length < 1) {
                return false;
            }
        } else if (line.hasOption('t')) {
            if (line.getArgs().length != 1) {
                return false;
            }
        } else if (line.hasOption('d')) {
            if (line.getArgs().length != 2) {
                return false;
            }
        } else if (line.hasOption('p')) {  // liveness
            if (line.hasOption('s') && line.getArgs().length != 3) {
                // we also need the name of the property
                return false;
            } else if (line.getArgs().length < 2 || line.getArgs().length > 3) {
                return false;
            }
        }else if (line.hasOption('l') || line.hasOption('s')) { // liveness (old ones)
            if (line.getArgs().length < 2 || line.getArgs().length > 3)
                return false;
        } else { // safety
            // we can omit the bad graph name and have only the property:
            if (line.getArgs().length < 3 || line.getArgs().length > 4)
                return false;
        }
        if (line.hasOption('h'))
            return false;

        // TODO: add checking over combinations of options

        return true;
    }

}
