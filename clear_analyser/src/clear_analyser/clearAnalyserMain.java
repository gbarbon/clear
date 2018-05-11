package clear_analyser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import clear_analyser.cexpmatcher.CexpMatcher;
import clear_analyser.graph.LTS;
import clear_analyser.graph.LTSPruner;
import clear_analyser.graphdb.Heuristics;
import clear_analyser.livenessdebugger.LivenessDebugger;
import clear_analyser.livenessdebugger.MinMaxLivenessDebugger_DEPRECATED2;
import clear_analyser.livenessdebugger.NestedInevitabilitiesDebugger;
import clear_analyser.livenessdebugger.SimpleLivenessDebugger;
import clear_analyser.modelchecker.CADP;
import clear_analyser.nbfinder.NeighbourhoodFinder;
import clear_analyser.graphdb.GraphDB;
import clear_analyser.sccfinder.SCCtester;
import clear_analyser.utils.CmdOptionParser;
import clear_analyser.utils.LtsUtils;
import clear_analyser.utils.STDOut;
import org.apache.commons.cli.CommandLine;

import clear_analyser.matcher.BFSStackMatcherMinDiff;

public class clearAnalyserMain {

    public static void main(String[] args) throws Exception {

        CmdOptionParser rawOptions = new CmdOptionParser(args);
        CommandLine line = rawOptions.parser();

        /**
         Options options = new Options();
         CommandLineParser parser = new DefaultParser();

         options.addOption("h", false, "Show help");
         options.addOption("f", false, "Fast matching");
         options.addOption("t", false, "Trigger Testing Mode"); // TODO: implement me! (Triggers all the testing functions
         options.addOption("s", false, "Shortest counterexample generation");
         options.addOption("n", false, "Neo4J graph generation");
         options.addOption("c", "scc",false, "Tarjan algortihm to obtian SCC");
         options.addOption("a", false, "Inevitable action");
         // options.addOption("ver", false, "algorithm version choice");
         */
        //try {
        //CommandLine cmd = parser.parse(options, args);

        // we can omit the bad graph name and have only the property:
        //if (cmd.getArgs().length < 3 || cmd.getArgs().length > 4 || cmd.hasOption('h')) {
        //		printMan(options);
        //}
        //else

        if (line.hasOption('c')) {
            tarjanTester(line);
        }
        else if (line.hasOption('l')) {
            simpleLiveness(line);
        }
        /* else if (line.hasOption('s')) {
            cascadeLiveness(line);
        }*/
        else if (line.hasOption('p')) {
            if (line.hasOption('s'))
                nestedInevitabilities(line, true);
            else
                nestedInevitabilities(line, false);
        }
        else if (line.hasOption('t')) {
            nestedInevitabilitiesRegressionTest(line);
        }
        else if (line.hasOption('d')) {
            deadlockLivelockDetector(line);
        }
        else {
            boolean fast = false;
            boolean scexp = false;
            boolean neo4j = false;
            if (line.hasOption('f')) {
                fast = true;
            }
            if (line.hasOption('s')) {
                scexp = true;
            }
            if (line.hasOption('n')) {
                neo4j = true;
            }
            graphSimpCaller(line, fast, scexp, neo4j);
        }
        /*} catch (ParseException e) {
			printMan(options);
		}*/
    }

    /**
     private static void printMan(Options options) {
     String syntax = "java GraphSimpMain [OPTIONS] DIR INPUT_COMPLETE_GRAPH [INPUT_BAD_GRAPH] " +
     "PROP";
     //String syntax_liveness = "java GraphSimpMain [OPTIONS] DIR INPUT_COMPLETE_GRAPH " +
     //		"[PROP]";
     String header = "\nOptions are :";
     String footer = "";

     HelpFormatter formatter = new HelpFormatter();
     formatter.printHelp(80, syntax, header, options, footer);
     }*/

    /**
     * @param cmd
     * @param fast
     * @throws IOException
     * @throws Exception
     */
    private static void graphSimpCaller(CommandLine cmd, boolean fast, boolean scexp, boolean
            neo4j) throws IOException, Exception {
        long startTime = System.currentTimeMillis();
        String baseDir = cmd.getArgs()[0];
        String completeGraphName = cmd.getArgs()[1];
        String badGraphName;// = cmd.getArgs()[2];
        String prop; // = cmd.getArgs()[3];
        String resDumpName;

        if (cmd.getArgs().length == 3) {
            prop = cmd.getArgs()[2];
            badGraphName = "bad_" + prop + "_" + completeGraphName;
        } else {
            badGraphName = cmd.getArgs()[2];
            prop = cmd.getArgs()[3];
        }
        resDumpName = badGraphName;
        if (fast)
            badGraphName = badGraphName + "_unclean";

        STDOut writer = new STDOut(baseDir, badGraphName);  // std output handler

        long ltsLoadingTimeStart = System.currentTimeMillis();
        LTS specGraph = new LTS(writer);
        specGraph.autLoader(baseDir, completeGraphName, fast, true);  // complete spec

        LTS badGraph = new LTS(writer);
        boolean ltsCheck = badGraph.autLoader(baseDir, badGraphName, fast, true); // bad graph
        if (!ltsCheck) {
            writer.printComplete("The given property is always TRUE or is not correctly written.\n " +
                    "Terminating execution.", true, true);
            return;
        }
        badGraph.setAsCounterexampleLTS();
        long ltsLoadingTimeEnd = System.currentTimeMillis();

        if (fast) {
            badGraph.equivTableLoader(baseDir, badGraphName, specGraph);
            LTSPruner.execute(badGraph, writer);
            writer.printComplete("extracted product from table", true, true);
        } else {
            BFSStackMatcherMinDiff sm = new BFSStackMatcherMinDiff(writer);
            writer.printComplete("matchNode res: " + sm.matchNodes(specGraph, specGraph.getInitialNode(),
                    badGraph, badGraph.getInitialNode()), true, true);
        }

        // checking the correctness of the simulation relation between lts
        LtsUtils.equivalenceExistence(badGraph);
        //LtsUtils.printEquivalenceMap(badGraph);
        //LtsUtils.OLDfindDiff(specGraph, badGraph);  // old function usesd in FSEN17 to retrieve differences (only GREEN* neighbourhoods)

        System.out.println("\n");
        NeighbourhoodFinder finder = new NeighbourhoodFinder(writer);
        finder.executor(specGraph, badGraph);

        // boolean valid = tester.checkMatchingCorrect(specGraph, badGraph);
        // System.out.println("valid " + valid);

        // aut dump
        badGraph.autDump(baseDir, resDumpName);

        // shortest counterexample abstraction
        if (scexp) {
            CADP cadp = new CADP(completeGraphName, badGraphName, prop, baseDir, writer);
            CexpMatcher cexpMatcher = new CexpMatcher(100, baseDir, badGraph, cadp, writer);
            cexpMatcher.execute(true);
        }
        // time count
        long endTime = System.currentTimeMillis();
        //System.out.println("\nExecution time " + (endTime - startTime));

        // testing visualiser
        //Visualiser visualiser = new Visualiser();
        //visualiser.visualiser(specGraph);

        System.out.println("\n");

        if (neo4j) {
            GraphDB test = new GraphDB();
            test.executor(badGraph, baseDir);
            Heuristics.shortestPathInitNb(test.getGraphDB());
            Heuristics.shortestPathInitFinal(test.getGraphDB());
        }
        //Heuristics.tester(test.getGraphDB(), test.getInitialNode());
        //Heuristics.allNeighbourhood(test.getGraphDB());
        //Heuristics.allNeighbourhoodWhereClause(test.getGraphDB()); // alternative version of
        // allNeighbourhood
        //--Heuristics.lowestNOfNeighbourhoods(test.getGraphDB());
        //--Heuristics.highestNOfNeighbourhoods(test.getGraphDB());
        //Heuristics.initialNode(test.getGraphDB());
        //Heuristics.finalNode(test.getGraphDB());

        endTime = System.currentTimeMillis();
        writer.printComplete("\nLTSs loading time: " + (ltsLoadingTimeEnd - ltsLoadingTimeStart) +
                " ms" +
                ".", true, true);
        writer.printComplete("Total exec time: " + (endTime - startTime) + " ms.", true, true);
        writer.close();
    }

    private static void nestedInevitabilities(CommandLine cmd, boolean scexp) throws Exception {
        String baseDir = cmd.getArgs()[0];
        String completeGraphName = cmd.getArgs()[1];
        String prop= "";
        long startTime = System.currentTimeMillis();
        long endTime;
        LivenessDebugger minmaxLivenessDebugger = null;

        // init std output handler
        STDOut writer = new STDOut(baseDir, completeGraphName+"_nestedinev");

        long ltsLoadingTimeStart = System.currentTimeMillis();
        LTS specGraph = new LTS(writer);
        specGraph.autLoader(baseDir, completeGraphName, false, false);
        long ltsLoadingTimeEnd = System.currentTimeMillis();

        if (cmd.getArgs().length == 2 && cmd.hasOption("x")) {
            String[] argsList = cmd.getOptionValues("x");
            List<String> actions = Arrays.asList(argsList);
            // FIXME: problems in case the list of actions is not given properly
            minmaxLivenessDebugger = new NestedInevitabilitiesDebugger(actions, specGraph, writer);
        } else if (cmd.getArgs().length == 3) {
            prop = cmd.getArgs()[2];
            if (cmd.hasOption("x")) {
                String[] argsList = cmd.getOptionValues("x");
                List<String> actions = Arrays.asList(argsList);
                minmaxLivenessDebugger = new NestedInevitabilitiesDebugger(actions, specGraph, writer);
            }
            else
                minmaxLivenessDebugger = new NestedInevitabilitiesDebugger(prop, specGraph, baseDir,
                    writer);
        } else {
            //it should not be possible
            throw new AssertionError();
        }
        minmaxLivenessDebugger.executor();

        boolean regressionCheck = specGraph.regressionCheck(baseDir, completeGraphName);
        System.out.println("The result of the regression check is: "+ regressionCheck);

        // finding neighbourhoods
        NeighbourhoodFinder finder = new NeighbourhoodFinder(writer);
        finder.livenessExecutor(specGraph);

        // aut dump
        specGraph.autDump(baseDir, completeGraphName);

        // shortest counterexample abstraction
        if (scexp && !prop.equals("")) {
            CADP cadp = new CADP(completeGraphName, prop, baseDir, writer);
            CexpMatcher cexpMatcher = new CexpMatcher(100, baseDir, specGraph, cadp, writer);
            cexpMatcher.execute(false);
        }

        System.out.println("LTS printing\n" + specGraph.toSringAutPrefixSuffix());

        endTime = System.currentTimeMillis();
        writer.printComplete("\nLTSs loading time: " + (ltsLoadingTimeEnd - ltsLoadingTimeStart) +
                " ms" +
                ".", true, true);
        writer.printComplete("Total exec time: " + (endTime - startTime) + " ms.", true, true);
        writer.close();
    }

    private static void nestedInevitabilitiesRegressionTest(CommandLine cmd) throws Exception {
        String baseDir = cmd.getArgs()[0];
        //String completeGraphName = cmd.getArgs()[1];
        String prop;
        long startTime = System.currentTimeMillis();
        long endTime;

        File folder = new File(baseDir);
        File[] listOfFiles = folder.listFiles();
        int nOfTest = 0;
        int nOfCorrect = 0;
        List<String>  falseTests  = new ArrayList<>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String testName = file.getName();
                // checking the filename // TODO: improve me
                if (testName.toLowerCase().contains("scc-")
                        && testName.toLowerCase().contains(".aut")
                        && !testName.toLowerCase().contains("clr-res")
                        && !testName.toLowerCase().contains("clr-exp")) {
                    System.out.println("");
                    nOfTest++;
                    testName = testName.substring(0, testName.lastIndexOf('.')); // removing .aut
                    LivenessDebugger minmaxLivenessDebugger = null;

                    // init std output handler
                    STDOut writer = new STDOut(baseDir, testName+"_minmaxliveness");

                    LTS specGraph = new LTS(writer);
                    specGraph.autLoader(baseDir, testName, false, false);

                    if (cmd.getArgs().length == 1 && cmd.hasOption("x")) {
                        String[] argsList = cmd.getOptionValues("x");
                        List<String> actions = Arrays.asList(argsList);
                        minmaxLivenessDebugger = new NestedInevitabilitiesDebugger(actions,
                         specGraph, writer);
                    //} else if (cmd.getArgs().length == 3) {
                    //    prop = cmd.getArgs()[2];
                        // generation of the Liveness LTS here:
                    //    minmaxLivenessDebugger = new MinMaxLivenessDebugger(prop, specGraph,
                    //            baseDir, writer);
                    } else {
                        //it should not be possible
                        throw new AssertionError();
                    }
                    minmaxLivenessDebugger.executor();
                    System.out.println(specGraph.toSringAutPrefixSuffix());

                    boolean regressionCheck = specGraph.regressionCheck(baseDir, testName);
                    System.out.println("The result of the regression check is: "+ regressionCheck);
                    if (regressionCheck) {
                        nOfCorrect++;
                    } else {
                        falseTests.add(testName);
                    }
                    // finding neighbourhoods
                    // NeighbourhoodFinder finder = new NeighbourhoodFinder(writer);
                    writer.close();

                }
            }
            //else if (file.isDirectory())
        }
        int nOfFalse = nOfTest-nOfCorrect;
        int correctRatio = (nOfCorrect*100) / nOfTest;
        System.out.println("\nNumber of correct test over total tests: "+ nOfCorrect + " over "
                +nOfTest);
        System.out.println("Correct ratio: " + correctRatio + "%");
        if (nOfFalse!=0) {
            System.out.print("False tests: " + nOfFalse + " ");
            System.out.println("False test are: " + falseTests);
        }
    }

    /**
     * Livelock detection (not deadlock?)
     * @param cmd
     * @throws Exception
     */
    @Deprecated // TODO: not sure it is useful / it is the right implementation
    private static void deadlockLivelockDetector(CommandLine cmd) throws Exception {
        String baseDir = cmd.getArgs()[0];
        String completeGraphName = cmd.getArgs()[1];
        long startTime = System.currentTimeMillis();
        long endTime;
        LivenessDebugger minmaxLivenessDebugger;

        // init std output handler
        STDOut writer = new STDOut(baseDir, completeGraphName+"_deadlocklivelock");

        long ltsLoadingTimeStart = System.currentTimeMillis();
        LTS specGraph = new LTS(writer);
        specGraph.autLoader(baseDir, completeGraphName, false, false);
        long ltsLoadingTimeEnd = System.currentTimeMillis();

        // FIXME: substitute with new implementation
        minmaxLivenessDebugger = new MinMaxLivenessDebugger_DEPRECATED2(specGraph, writer);
        minmaxLivenessDebugger.executor();

        // finding neighbourhoods
        NeighbourhoodFinder finder = new NeighbourhoodFinder(writer);
        finder.livenessExecutor(specGraph);

        System.out.println("LTS printing\n" + specGraph.toSringAutPrefixSuffix());

        endTime = System.currentTimeMillis();
        writer.printComplete("\nLTSs loading time: " + (ltsLoadingTimeEnd - ltsLoadingTimeStart) +
                " ms" +
                ".", true, true);
        writer.printComplete("Total exec time: " + (endTime - startTime) + " ms.", true, true);
        writer.close();
    }

    /**
     * Old implementation of the single action inevitability
     * // FIXME: not sure it works, to remove
     * @param cmd
     * @throws Exception
     */
    @Deprecated
    private static void simpleLiveness(CommandLine cmd) throws Exception {
        String baseDir = cmd.getArgs()[0];
        String completeGraphName = cmd.getArgs()[1];
        String prop;
        String action;
        long startTime = System.currentTimeMillis();
        long endTime;
        SimpleLivenessDebugger livenessDebugger = null;

        // init std output handler
        STDOut writer = new STDOut(baseDir, completeGraphName+"_simpleliveness");

        long ltsLoadingTimeStart = System.currentTimeMillis();
        LTS specGraph = new LTS(writer);
        specGraph.autLoader(baseDir, completeGraphName, false, false);
        long ltsLoadingTimeEnd = System.currentTimeMillis();

        if (cmd.getArgs().length == 2 && cmd.hasOption("a")) {
            action = cmd.getOptionValue("a");
            livenessDebugger = new SimpleLivenessDebugger(action, specGraph, writer);
        } else if (cmd.getArgs().length == 3) {
            prop = cmd.getArgs()[2];
            // generation of the Liveness LTS here:
            livenessDebugger = new SimpleLivenessDebugger(prop, specGraph, baseDir, writer);
        } else {
            //it should not be possible
            throw new AssertionError();
        }
        livenessDebugger.executor();

        // finding neighbourhoods
        NeighbourhoodFinder finder = new NeighbourhoodFinder(writer);
        finder.livenessExecutor(specGraph);

        System.out.println("LTS printing\n" + specGraph.toSringAut());

        endTime = System.currentTimeMillis();
        writer.printComplete("\nLTSs loading time: " + (ltsLoadingTimeEnd - ltsLoadingTimeStart) +
                " ms" +
                ".", true, true);
        writer.printComplete("Total exec time: " + (endTime - startTime) + " ms.", true, true);
        writer.close();
    }

        /*
    // deprecated implementation
    // FIXME: to remove
    private static void cascadeLiveness(CommandLine cmd) throws Exception {
        String baseDir = cmd.getArgs()[0];
        String completeGraphName = cmd.getArgs()[1];
        String prop;
        long startTime = System.currentTimeMillis();
        long endTime;
        CascadeLivenessDebugger_DEPRECATED cascadeLivenessDebugger = null;

        // init std output handler
        STDOut writer = new STDOut(baseDir, completeGraphName+"_cascadeliveness");

        long ltsLoadingTimeStart = System.currentTimeMillis();
        LTS specGraph = new LTS(writer);
        specGraph.autLoader(baseDir, completeGraphName, false, false);
        long ltsLoadingTimeEnd = System.currentTimeMillis();

        if (cmd.getArgs().length == 2 && cmd.hasOption("x")) {
            String[] argsList = cmd.getOptionValues("x");
            List<String> actions = Arrays.asList(argsList);
            //System.out.println("ARGS are: ");
            //actions.parallelStream().forEach(System.out::println);
            cascadeLivenessDebugger = new CascadeLivenessDebugger_DEPRECATED(actions, specGraph, writer);
        } else if (cmd.getArgs().length == 3) {
            prop = cmd.getArgs()[2];
            // generation of the Liveness LTS here:
            cascadeLivenessDebugger = new CascadeLivenessDebugger_DEPRECATED(prop, specGraph, baseDir, writer);
        } else {
            //it should not be possible
            throw new AssertionError();
        }
        cascadeLivenessDebugger.executor();

        // finding neighbourhoods
        NeighbourhoodFinder finder = new NeighbourhoodFinder(writer);
        finder.livenessExecutor(specGraph);

        System.out.println("LTS printing\n" + specGraph.toSringAut());

        endTime = System.currentTimeMillis();
        writer.printComplete("\nLTSs loading time: " + (ltsLoadingTimeEnd - ltsLoadingTimeStart) +
                " ms" +
                ".", true, true);
        writer.printComplete("Total exec time: " + (endTime - startTime) + " ms.", true, true);
        writer.close();
    }*/

    /**
     * To test
     * @param cmd
     * @throws Exception
     * // FIXME: to remove, no more useful,
     * // since already tested and included in nestedInevitabilites
     */
    @Deprecated
    private static void tarjanTester(CommandLine cmd) throws Exception {
        String baseDir = cmd.getArgs()[0];
        String completeGraphName = cmd.getArgs()[1];
        long startTime = System.currentTimeMillis();
        long endTime;

        // std output handler
        STDOut writer = new STDOut(baseDir, completeGraphName+"_tarjan");

        long ltsLoadingTimeStart = System.currentTimeMillis();
        LTS specGraph = new LTS(writer);
        specGraph.autLoader(baseDir, completeGraphName, false, false);
        long ltsLoadingTimeEnd = System.currentTimeMillis();

        SCCtester scCtester = new SCCtester();
        scCtester.tester(specGraph);

        endTime = System.currentTimeMillis();
        writer.printComplete("\nLTSs loading time: " + (ltsLoadingTimeEnd - ltsLoadingTimeStart) +
                " ms.", true, true);
        writer.printComplete("Total exec time: " + (endTime - startTime) + " ms.", true, true);
        writer.close();
    }
}